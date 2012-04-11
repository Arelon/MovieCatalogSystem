package net.milanaleksic.mcs.infrastructure.thumbnail.impl;

import com.google.common.base.Function;
import com.google.common.collect.*;
import net.milanaleksic.mcs.application.gui.helper.ShowImageComposite;
import net.milanaleksic.mcs.infrastructure.LifecycleListener;
import net.milanaleksic.mcs.infrastructure.config.ApplicationConfiguration;
import net.milanaleksic.mcs.infrastructure.config.UserConfiguration;
import net.milanaleksic.mcs.infrastructure.image.ImageRepository;
import net.milanaleksic.mcs.infrastructure.network.HttpClientFactoryService;
import net.milanaleksic.mcs.infrastructure.network.PersistentHttpContext;
import net.milanaleksic.mcs.infrastructure.thumbnail.ThumbnailManager;
import net.milanaleksic.mcs.infrastructure.tmdb.TmdbException;
import net.milanaleksic.mcs.infrastructure.tmdb.TmdbService;
import net.milanaleksic.mcs.infrastructure.tmdb.bean.ImageInfo;
import net.milanaleksic.mcs.infrastructure.tmdb.bean.ImageSearchResult;
import net.milanaleksic.mcs.infrastructure.util.IMDBUtil;
import net.milanaleksic.mcs.infrastructure.util.SWTUtil;
import net.milanaleksic.mcs.infrastructure.worker.WorkerManager;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TableItem;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

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
    private Map<String, ImageSearchResult> cachedImageSearches = new ConcurrentHashMap<>();

    private int thumbnailWidth = 138;
    private int thumbnailHeight = 92;

    private File cacheDirectory = null;

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
        final String imdbId = imageTargetWidget.getImdbId();
        if (!IMDBUtil.isValidImdbId(imdbId)) {
            imageTargetWidget.setImageFromResource(defaultImageResource);
            return;
        }
        String absoluteFileLocation = imdbIdToLocallyCachedImageMap.get(imdbId);
        //TODO: use image repository instead of setimagefromresource
        if (absoluteFileLocation == null) {
            imageTargetWidget.setImageFromResource(defaultImageResource);
            imdbIdToLocallyCachedImageMap.put(imdbId, MAGIC_VALUE_ENQUEUED);
            downloadingWaiters.put(imdbId, new Function<Image, Void>() {
                @Override
                public Void apply(@Nullable Image image) {
                    imageTargetWidget.safeSetImage(image, imdbId);
                    return null;
                }
            });
            startDownloadingWorker(imdbId);
        } else if (absoluteFileLocation.equals(MAGIC_VALUE_ENQUEUED)) {
            imageTargetWidget.setImageFromResource(defaultImageResource);
            downloadingWaiters.put(imdbId, new Function<Image, Void>() {
                @Override
                public Void apply(@Nullable Image image) {
                    imageTargetWidget.safeSetImage(image, imdbId);
                    return null;
                }
            });
        } else if (absoluteFileLocation.equals(MAGIC_VALUE_NON_EXISTING))
            imageTargetWidget.setImageFromResource(defaultImageResource);
        else
            imageTargetWidget.setImageFromExternalFile(absoluteFileLocation);
    }

    private Future<?> startDownloadingWorker(final String imdbId) {
        return workerManager.submitIoBoundWorker(new Runnable() {
            @Override
            public void run() {
                ImageSearchResult imagesForMovie;
                try {
                    imagesForMovie = getImagesForMovie(imdbId);
                    final String url = findAppropriateThumbnailImage(imagesForMovie);
                    if (url == null) {
                        if (logger.isDebugEnabled())
                            logger.debug("No suitable poster found for movie: " + imdbId); //NON-NLS
                        recordDummyResponseForMovie(imdbId);
                        return;
                    }
                    SWTUtil.createImageFromUrl(URI.create(url), persistentHttpContext, new Function<Image, Void>() {
                        @Override
                        public Void apply(@Nullable Image image) {
                            final Image finalImage;
                            if (image == null)
                                finalImage = null;
                            else if (image.getBounds().width != thumbnailWidth && image.getBounds().height != thumbnailHeight) {
                                finalImage = SWTUtil.resize(image, thumbnailWidth, thumbnailHeight);
                                image.dispose();
                            } else
                                finalImage = image;
                            applyImage(finalImage);
                            saveImageLocally(finalImage, imdbId);
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

    private ImageSearchResult getImagesForMovie(String imdbId) throws TmdbException {
        ImageSearchResult imageSearchResult = cachedImageSearches.get(imdbId);
        if (imageSearchResult != null)
            return imageSearchResult;
        cachedImageSearches.put(imdbId, imageSearchResult = tmdbService.getImagesForMovie(imdbId));
        return imageSearchResult;
    }

    private void recordDummyResponseForMovie(String imdbId) {
        imdbIdToLocallyCachedImageMap.put(imdbId, MAGIC_VALUE_NON_EXISTING);
        downloadingWaiters.removeAll(imdbId);
    }

    private String findAppropriateThumbnailImage(ImageSearchResult imagesForMovie) {
        if (imagesForMovie == null)
            return null;
        for (ImageInfo imageInfo : imagesForMovie.getPosters()) {
            net.milanaleksic.mcs.infrastructure.tmdb.bean.Image image = imageInfo.getImage();
            if (image.getWidth() == thumbnailWidth && image.getHeight() == thumbnailHeight)
                return image.getUrl();
            // in case we don't have exact match, we need first with both dimensions larget than exact match
            // for best rescaling we can get
            if (image.getWidth() > thumbnailWidth && image.getHeight() > thumbnailHeight)
                return image.getUrl();
        }
        return null;
    }

    private void saveImageLocally(Image image, String imdbId) {
        if (cacheDirectory == null)
            return;
        String imageLocation = getLocalImageLocation(imdbId);
        imdbIdToLocallyCachedImageMap.put(imdbId, imageLocation);
        ImageLoader imageLoader = new ImageLoader();
        imageLoader.data = new ImageData[]{image.getImageData()};
        imageLoader.save(imageLocation, SWT.IMAGE_JPEG);
    }

    private String getLocalImageLocation(String imdbId) {
        return new File(cacheDirectory, imdbId + ".jpg").getAbsolutePath(); //NON-NLS
    }

    @Override
    public void applicationStarted(ApplicationConfiguration configuration, UserConfiguration userConfiguration) {
        persistentHttpContext = httpClientFactoryService.createPersistentHttpContext();
        prepareLocallyCachedImages(configuration);
    }

    private void prepareLocallyCachedImages(ApplicationConfiguration configuration) {
        imdbIdToLocallyCachedImageMap = new HashMap<>();
        ApplicationConfiguration.CacheConfiguration cacheConfiguration = configuration.getCacheConfiguration();
        File location = new File(cacheConfiguration.getLocation());
        if ((!location.exists() && !location.mkdir()) || !location.canWrite()) {
            cacheDirectory = null;
            return;
        }
        cacheDirectory = location;
        File[] files = cacheDirectory.listFiles();
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
    public void precacheThumbnails() {
        Collection<String> values = imdbIdToLocallyCachedImageMap.values();
        for (String absolutePath : values) {
            imageRepository.cacheImageDataForImage(absolutePath);
        }
    }

    @Override
    public int getThumbnailHeight() {
        return thumbnailHeight;
    }
}
