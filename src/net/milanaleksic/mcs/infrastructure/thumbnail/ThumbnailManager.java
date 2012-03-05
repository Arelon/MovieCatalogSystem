package net.milanaleksic.mcs.infrastructure.thumbnail;

import net.milanaleksic.mcs.infrastructure.LifecycleListener;
import net.milanaleksic.mcs.infrastructure.network.HttpClientFactoryService;
import net.milanaleksic.mcs.infrastructure.network.PersistentHttpContext;
import net.milanaleksic.mcs.infrastructure.tmdb.TmdbException;
import net.milanaleksic.mcs.infrastructure.tmdb.TmdbService;
import net.milanaleksic.mcs.infrastructure.tmdb.bean.ImageSearchResult;
import net.milanaleksic.mcs.infrastructure.util.SWTUtil;
import net.milanaleksic.mcs.infrastructure.worker.WorkerManager;
import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Item;

import javax.inject.Inject;
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

    private PersistentHttpContext persistentHttpContext;

    private String defaultImageResource;

    public void setDefaultImageResource(String defaultImageResource) {
        this.defaultImageResource = defaultImageResource;
    }

    public void setThumbnailForItem(Item item, String imdbId) {
        String absoluteFileLocation = locallyCachedImages.get(imdbId);
        if (absoluteFileLocation != null) {
            SWTUtil.setImageOnTargetFromExternalFile(item, absoluteFileLocation);
            return;
        }
        SWTUtil.setImageOnTarget(item, defaultImageResource);
        startDownloadingWorkerForItem(item, imdbId);
    }

    private void startDownloadingWorkerForItem(final Item targetItem, final String imdbId) {
        workerManager.submitWorker(new Runnable() {
            @Override
            public void run() {
                try {
                    ImageSearchResult imagesForMovie = tmdbService.getImagesForMovie(imdbId);
                    logger.info(imagesForMovie);
                } catch (TmdbException exception) {
                    throw new RuntimeException("Unexpected error when downloading images for movie " + imdbId, exception);
                }
            }
        });
    }

    @Override
    public void applicationStarted() {
        //TODO: implement reading current info from the local cache
        locallyCachedImages = new HashMap<>();
        persistentHttpContext = httpClientFactoryService.createPersistentHttpContext();
    }

    @Override
    public void applicationShutdown() {
    }

}
