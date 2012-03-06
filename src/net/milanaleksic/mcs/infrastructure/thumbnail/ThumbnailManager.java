package net.milanaleksic.mcs.infrastructure.thumbnail;

import com.google.common.base.Function;
import net.milanaleksic.mcs.application.ApplicationManager;
import net.milanaleksic.mcs.application.config.ApplicationConfiguration;
import net.milanaleksic.mcs.infrastructure.LifecycleListener;
import net.milanaleksic.mcs.infrastructure.network.HttpClientFactoryService;
import net.milanaleksic.mcs.infrastructure.network.PersistentHttpContext;
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
import org.eclipse.swt.widgets.*;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class ThumbnailManager implements LifecycleListener {

    // this is tmdbID - to - absolute file location object mapping
    private Map<String, String> locallyCachedImages;

    private static final Logger logger = Logger.getLogger(ThumbnailManager.class);

    @Inject
    private WorkerManager workerManager;

    @Inject
    private TmdbService tmdbService;

    @Inject
    private HttpClientFactoryService httpClientFactoryService;

    @Inject
    private ApplicationManager applicationManager;

    private String defaultImageResource;

    private PersistentHttpContext persistentHttpContext;

    private int thumbnailWidth = 138;
    private int thumbnailHeight = 92;

    private File cacheDirectory = null;

    private static final java.util.regex.Pattern PATTERN_CACHED_IMAGE = java.util.regex.Pattern.compile("tt\\d{7}\\.jpg"); //NON-NLS

    public void setThumbnailHeight(int thumbnailHeight) {
        this.thumbnailHeight = thumbnailHeight;
    }

    public void setThumbnailWidth(int thumbnailWidth) {
        this.thumbnailWidth = thumbnailWidth;
    }

    public void setDefaultImageResource(String defaultImageResource) {
        this.defaultImageResource = defaultImageResource;
    }

    public void setThumbnailForItem(TableItem item, String imdbId) {
        String absoluteFileLocation = locallyCachedImages.get(imdbId);
        if (absoluteFileLocation != null) {
            SWTUtil.setImageOnTargetFromExternalFile(item, absoluteFileLocation);
            return;
        }
        SWTUtil.setImageOnTarget(item, defaultImageResource);
        if (imdbId == null || !IMDBUtil.isValidImdbId(imdbId))
            return;
        startDownloadingWorkerForItem(item, imdbId);
    }

    private void startDownloadingWorkerForItem(final TableItem targetItem, final String imdbId) {
        workerManager.submitWorker(new Runnable() {
            @Override
            public void run() {
                ImageSearchResult imagesForMovie;
                try {
                    imagesForMovie = tmdbService.getImagesForMovie(imdbId);
                    final String url = findAppropriateThumbnailImage(imagesForMovie);
                    if (url == null) {
                        SWTUtil.setImageOnTarget(targetItem, defaultImageResource);
                        return;
                    }
                    SWTUtil.createImageFromUrl(URI.create(url), persistentHttpContext, new Function<Image, Void>() {

                        @Override
                        public Void apply(@Nullable final Image image) {
                            Display.getDefault().asyncExec(new Runnable() {

                                @Override
                                public void run() {
                                    if (targetItem.isDisposed())
                                        return;
                                    //TODO: test if the target item has data of the same movie as this image
                                    targetItem.setImage(image);
                                }

                            });
                            saveImageLocally(imdbId, image);
                            return null;
                        }
                    });
                } catch (TmdbException e) {
                    logger.error("Error while downloading imageInfo", e); //NON-NLS
                }
            }
        });
    }

    private String findAppropriateThumbnailImage(ImageSearchResult imagesForMovie) {
        if (imagesForMovie == null)
            return null;
        for (ImageInfo imageInfo : imagesForMovie.getPosters()) {
            net.milanaleksic.mcs.infrastructure.tmdb.bean.Image image = imageInfo.getImage();
            if (image.getWidth() == thumbnailWidth && image.getHeight() == thumbnailHeight)
                return image.getUrl();
        }
        return null;
    }

    private void saveImageLocally(String imdbId, Image image) {
        if (cacheDirectory == null)
            return;
        String imageLocation = getLocalImageLocation(imdbId);
        locallyCachedImages.put(imdbId, imageLocation);
        ImageLoader imageLoader = new ImageLoader();
        imageLoader.data = new ImageData[] { image.getImageData() };
        imageLoader.save(imageLocation, SWT.IMAGE_JPEG);
    }

    private String getLocalImageLocation(String imdbId) {
        return new File(cacheDirectory, imdbId+".jpg").getAbsolutePath(); //NON-NLS
    }

    @Override
    public void applicationStarted() {
        persistentHttpContext = httpClientFactoryService.createPersistentHttpContext();
        prepareLocallyCachedImages();
    }

    private void prepareLocallyCachedImages() {
        locallyCachedImages = new HashMap<>();
        ApplicationConfiguration.CacheConfiguration cacheConfiguration = applicationManager.getApplicationConfiguration().getCacheConfiguration();
        File location = new File(cacheConfiguration.getLocation());
        if ((!location.exists() && !location.mkdir()) || !location.canWrite()) {
            cacheDirectory = null;
            return;
        }
        cacheDirectory = location;
        File[] files = cacheDirectory.listFiles();
        for (File file : files) {
            if (!PATTERN_CACHED_IMAGE.matcher(file.getName()).matches()) {
                logger.warn("File not detected as a proper cached image: "+file); //NON-NLS
                continue;
            }
            locallyCachedImages.put(
                    file.getName().substring(0, file.getName().lastIndexOf(".")),
                    file.getAbsolutePath()
            );
        }
    }

    @Override
    public void applicationShutdown() {
    }

}
