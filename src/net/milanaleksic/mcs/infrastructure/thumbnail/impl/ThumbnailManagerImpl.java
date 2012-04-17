package net.milanaleksic.mcs.infrastructure.thumbnail.impl;

import com.google.common.base.*;
import com.google.common.collect.*;
import net.milanaleksic.mcs.application.gui.helper.ShowImageComposite;
import net.milanaleksic.mcs.infrastructure.LifecycleListener;
import net.milanaleksic.mcs.infrastructure.config.*;
import net.milanaleksic.mcs.infrastructure.image.ImageRepository;
import net.milanaleksic.mcs.infrastructure.network.*;
import net.milanaleksic.mcs.infrastructure.thumbnail.ThumbnailManager;
import net.milanaleksic.mcs.infrastructure.tmdb.*;
import net.milanaleksic.mcs.infrastructure.tmdb.bean.*;
import net.milanaleksic.mcs.infrastructure.util.*;
import net.milanaleksic.mcs.infrastructure.worker.WorkerManager;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.File;
import java.net.URI;
import java.util.Map;

public class ThumbnailManagerImpl implements ThumbnailManager, LifecycleListener {

    private static final Logger logger = Logger.getLogger(ThumbnailManagerImpl.class);

    private Map<String, String> imdbIdToLocallyCachedImageMap;

    @Inject
    private WorkerManager workerManager;

    @Inject
    private TmdbService tmdbService;

    @Inject
    private HttpClientFactoryService httpClientFactoryService;

    @Inject
    private ImageRepository imageRepository;

    private String defaultImageResource;

    private PersistentHttpContext persistentHttpContext;

    // various concurrent optimizations
    private Multimap<String, Function<Image, Void>> downloadingWaiters = ArrayListMultimap.create();
    private Map<String, ImageSearchResult> cachedImageSearches = Maps.newConcurrentMap();

    private int thumbnailWidth = 138;
    private int thumbnailHeight = 92;

    private Optional<File> cacheDirectory = Optional.absent();

    private static final java.util.regex.Pattern PATTERN_CACHED_IMAGE = java.util.regex.Pattern.compile("tt\\d{7}\\.jpg"); //NON-NLS
    private static final String MAGIC_VALUE_NON_EXISTING = "<nonexisting>"; //NON-NLS
    private static final String MAGIC_VALUE_ENQUEUED = "<downloading>"; //NON-NLS

    public void setThumbnailHeight(int thumbnailHeight) {
        this.thumbnailHeight = thumbnailHeight;
    }

    public void setThumbnailWidth(int thumbnailWidth) {
        this.thumbnailWidth = thumbnailWidth;
    }

    public void setDefaultImageResource(String defaultImageResource) {
        this.defaultImageResource = defaultImageResource;
    }

    @Override
    public void setThumbnailForItem(TableItem item) {
        setThumbnailOnWidget(ImageTargetWidgetFactory.createTableItemImageTarget(item));
    }

    @Override
    public void setThumbnailForShowImageComposite(ShowImageComposite composite, String imdbId) {
        setThumbnailOnWidget(ImageTargetWidgetFactory.createCompositeImageTarget(composite, imdbId));
    }

    @Override
    public void setThumbnailOnWidget(final ImageTargetWidget imageTargetWidget) {
        Optional<String> imdbIdOptional = imageTargetWidget.getImdbId();
        if (!IMDBUtil.isValidImdbId(imdbIdOptional.orNull())) {
            imageTargetWidget.setImage(imageRepository.getResourceImage(defaultImageResource));
            return;
        }
        final String imdbId = imdbIdOptional.get();
        Optional<String> absoluteFileLocation = Optional.fromNullable(imdbIdToLocallyCachedImageMap.get(imdbId));
        //TODO: use image repository instead of setimagefromresource
        if (!absoluteFileLocation.isPresent()) {
            imageTargetWidget.setImage(imageRepository.getResourceImage(defaultImageResource));
            imdbIdToLocallyCachedImageMap.put(imdbId, MAGIC_VALUE_ENQUEUED);
            downloadingWaiters.put(imdbId, new Function<Image, Void>() {
                @Override
                public Void apply(@Nullable Image image) {
                    imageTargetWidget.safeSetImage(Optional.fromNullable(image), imdbId);
                    return null;
                }
            });
            startDownloadingWorker(imdbId);
        } else if (absoluteFileLocation.get().equals(MAGIC_VALUE_ENQUEUED)) {
            imageTargetWidget.setImage(imageRepository.getResourceImage(defaultImageResource));
            downloadingWaiters.put(imdbId, new Function<Image, Void>() {
                @Override
                public Void apply(@Nullable Image image) {
                    imageTargetWidget.safeSetImage(Optional.fromNullable(image), imdbId);
                    return null;
                }
            });
        } else if (absoluteFileLocation.get().equals(MAGIC_VALUE_NON_EXISTING))
            imageTargetWidget.setImage(imageRepository.getResourceImage(defaultImageResource));
        else
            imageTargetWidget.setImage(imageRepository.getImageByPath(absoluteFileLocation.get()).orNull());
    }

    private void startDownloadingWorker(final String imdbId) {
        workerManager.submitIoBoundWorker(new Runnable() {
            @Override
            public void run() {
                try {
                    Optional<ImageSearchResult> imagesForMovieOptional = getImagesForMovie(imdbId);
                    final Optional<String> url = imagesForMovieOptional.isPresent()
                            ? findAppropriateThumbnailImage(imagesForMovieOptional.get())
                            : Optional.<String>absent();
                    if (!url.isPresent()) {
                        if (logger.isDebugEnabled())
                            logger.debug("No suitable poster found for movie: " + imdbId); //NON-NLS
                        recordDummyResponseForMovie(imdbId);
                        return;
                    }
                    SWTUtil.createImageFromUrl(URI.create(url.get()), persistentHttpContext, new Function<Image, Void>() {
                        @Override
                        public Void apply(Image image) {
                            if (image.getBounds().width != thumbnailWidth && image.getBounds().height != thumbnailHeight) {
                                Image imageToDispose = image;
                                image = SWTUtil.resize(image, thumbnailWidth, thumbnailHeight);
                                imageToDispose.dispose();
                            }
                            applyImage(image);
                            saveImageLocally(image, imdbId);
                            return null;
                        }

                        private void applyImage(final Image finalImage) {
                            Display.getDefault().asyncExec(new Runnable() {
                                @Override
                                public void run() {
                                    for (Function<Image, Void> function : downloadingWaiters.get(imdbId)) {
                                        function.apply(finalImage);
                                    }
                                    downloadingWaiters.removeAll(imdbId);
                                }
                            });
                        }
                    });
                } catch (TmdbException e) {
                    logger.error("Error while downloading imageInfo", e); //NON-NLS
                }
            }
        });
    }

    private Optional<ImageSearchResult> getImagesForMovie(String imdbId) throws TmdbException {
        Optional<ImageSearchResult> imageSearchResult = Optional.fromNullable(cachedImageSearches.get(imdbId));
        if (imageSearchResult.isPresent())
            return imageSearchResult;
        imageSearchResult = tmdbService.getImagesForMovie(imdbId);
        if (imageSearchResult.isPresent())
            cachedImageSearches.put(imdbId, imageSearchResult.get());
        return imageSearchResult;
    }

    private void recordDummyResponseForMovie(String imdbId) {
        imdbIdToLocallyCachedImageMap.put(imdbId, MAGIC_VALUE_NON_EXISTING);
        downloadingWaiters.removeAll(imdbId);
    }

    private Optional<String> findAppropriateThumbnailImage(ImageSearchResult imagesForMovie) {
        for (ImageInfo imageInfo : imagesForMovie.getPosters()) {
            net.milanaleksic.mcs.infrastructure.tmdb.bean.Image image = imageInfo.getImage();
            if (image.getWidth() == thumbnailWidth && image.getHeight() == thumbnailHeight)
                return Optional.of(image.getUrl());
            // in case we don't have exact match, we need first with both dimensions larget than exact match
            // for best rescaling we can get
            if (image.getWidth() > thumbnailWidth && image.getHeight() > thumbnailHeight)
                return Optional.of(image.getUrl());
        }
        return Optional.absent();
    }

    private void saveImageLocally(Image image, String imdbId) {
        String imageLocation = getLocalImageLocation(imdbId);
        imdbIdToLocallyCachedImageMap.put(imdbId, imageLocation);
        ImageLoader imageLoader = new ImageLoader();
        imageLoader.data = new ImageData[]{image.getImageData()};
        imageLoader.save(imageLocation, SWT.IMAGE_JPEG);
    }

    private String getLocalImageLocation(String imdbId) {
        return new File(cacheDirectory.get(), imdbId + ".jpg").getAbsolutePath(); //NON-NLS
    }

    @Override
    public void applicationStarted(ApplicationConfiguration configuration, UserConfiguration userConfiguration) {
        persistentHttpContext = httpClientFactoryService.createPersistentHttpContext();
        prepareLocallyCachedImages(configuration);
    }

    private void prepareLocallyCachedImages(ApplicationConfiguration configuration) {
        imdbIdToLocallyCachedImageMap = Maps.newHashMap();
        ApplicationConfiguration.CacheConfiguration cacheConfiguration = configuration.getCacheConfiguration();
        File location = new File(cacheConfiguration.getLocation());
        if ((!location.exists() && !location.mkdir()) || !location.canWrite()) {
            throw new IllegalStateException("Cache directory was not created and/or it was not writable");
        }
        cacheDirectory = Optional.of(location);
        File[] files = cacheDirectory.get().listFiles();
        for (File file : files) {
            if (!PATTERN_CACHED_IMAGE.matcher(file.getName()).matches()) {
                logger.warn("File not detected as a proper cached image: " + file); //NON-NLS
                continue;
            }
            imdbIdToLocallyCachedImageMap.put(
                    file.getName().substring(0, file.getName().lastIndexOf(".")),
                    file.getAbsolutePath()
            );
        }
    }

    @Override
    public void applicationShutdown(ApplicationConfiguration applicationConfiguration, UserConfiguration userConfiguration) {
    }

    @Override
    public int getThumbnailWidth() {
        return thumbnailWidth;
    }

    @Override
    public void preCacheThumbnails() {
        for (String absolutePath : ImmutableList.copyOf(imdbIdToLocallyCachedImageMap.values())) {
            if (MAGIC_VALUE_NON_EXISTING.equals(absolutePath) || MAGIC_VALUE_ENQUEUED.equals(absolutePath))
                continue;
            if (imageRepository.cacheExternalImageFile(absolutePath)) {
                if (logger.isInfoEnabled())
                    logger.info("Giving up of further image caching because cache started to evict"); //NON-NLS
                break;
            }
        }
    }

    @Override
    public int getThumbnailHeight() {
        return thumbnailHeight;
    }
}
