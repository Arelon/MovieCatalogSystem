package net.milanaleksic.mcs.application.gui.helper;

import net.milanaleksic.mcs.application.ApplicationManager;
import net.milanaleksic.mcs.infrastructure.IntegrationManager;
import net.milanaleksic.mcs.infrastructure.tmdb.TmdbException;
import net.milanaleksic.mcs.infrastructure.tmdb.TmdbService;
import net.milanaleksic.mcs.infrastructure.tmdb.bean.Movie;
import net.milanaleksic.mcs.infrastructure.util.SWTUtil;
import org.apache.log4j.Logger;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.*;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.ResourceBundle;
import java.util.concurrent.*;

/**
 * User: Milan Aleksic
 * Date: 10/8/11
 * Time: 8:26 PM
 */
public class OfferMovieList extends KeyAdapter implements IntegrationManager {

    public interface Receiver {
        void setCurrentQueryItems(String currentQuery, String message, @Nullable Movie[] movies);
    }

    public static final int MIN_DELAY_BETWEEN_REQUESTS = 3000;

    private static final Logger logger = Logger.getLogger(OfferMovieList.class);

    @Inject
    private TmdbService tmdbService;

    @Inject
    private ApplicationManager applicationManager;

    private Scrollable source;

    private volatile long previousTimeFired;
    private volatile String currentQuery;

    private Receiver receiver;

    private ScheduledThreadPoolExecutor executorService;
    private ResourceBundle bundle;

    public void setReceiver(Receiver receiver) {
        this.receiver = receiver;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        final String query = SWTUtil.getTextFrom(source);
        if (query == null || query.length() == 0) {
            receiver.setCurrentQueryItems(query, bundle.getString(bundle.getString("offerList.noSearchString")), null);
            return;
        }
        if (query.length() < 3)
            return;
        if (!Character.isLetterOrDigit(e.character)
                && java.awt.event.KeyEvent.VK_BACK_SPACE != e.keyCode)
            return;
        executeSearch(query);
    }

    private synchronized void executeSearch(String query) {
        Runnable fetchItem = new Runnable() {
            @Override
            public void run() {
                try {
                    previousTimeFired = System.currentTimeMillis();
                    String message;
                    Movie[] movies = tmdbService.searchForMovies(currentQuery);
                    if (movies == null || movies.length == 0)
                        message = bundle.getString("offerList.nothingFound");
                    else
                        message = null;
                    receiver.setCurrentQueryItems(currentQuery, message, movies);
                } catch (TmdbException e1) {
                    logger.error("Error while fetching movie information", e1); //NON-NLS
                    receiver.setCurrentQueryItems(currentQuery, bundle.getString("offerList.searchFailed"), null);
                } catch (Exception e) {
                    logger.error("Unexpected error while fetching movie information", e); //NON-NLS
                }
            }
        };
        long timePassedSinceLastFire = System.currentTimeMillis() - previousTimeFired;
        long delay = timePassedSinceLastFire > MIN_DELAY_BETWEEN_REQUESTS
                ? 0
                : MIN_DELAY_BETWEEN_REQUESTS - timePassedSinceLastFire;
        currentQuery = query;
        if (executorService.getQueue().size() == 0) {
            if (logger.isDebugEnabled())
                logger.debug("Scheduling search in " + delay + "ms"); //NON-NLS
            executorService.schedule(fetchItem, delay, TimeUnit.MILLISECONDS);
        }
    }

    public void startup() {
        executorService = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(3);
    }

    public void cleanup() {
        if (executorService != null && !executorService.isShutdown())
            executorService.shutdownNow();
    }

    @Override
    public void applicationStarted() {
        this.bundle = applicationManager.getMessagesBundle();
    }

    @Override
    public void applicationShutdown() {
        cleanup();
    }

    public void prepareFor(Combo movieName) {
        startup();
        this.source = movieName;
        movieName.addKeyListener(this);
    }

    public void prepareFor(Text movieName) {
        startup();
        this.source = movieName;
        movieName.addKeyListener(this);
    }
}
