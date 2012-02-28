package net.milanaleksic.mcs.application.gui.helper;

import net.milanaleksic.mcs.application.ApplicationManager;
import net.milanaleksic.mcs.application.IntegrationManager;
import net.milanaleksic.mcs.application.gui.NewOrEditMovieDialogForm;
import net.milanaleksic.mcs.infrastructure.tmdb.TmdbException;
import net.milanaleksic.mcs.infrastructure.tmdb.TmdbService;
import net.milanaleksic.mcs.infrastructure.tmdb.bean.Movie;
import org.apache.log4j.Logger;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Combo;

import javax.inject.Inject;
import java.util.ResourceBundle;
import java.util.concurrent.*;

/**
 * User: Milan Aleksic
 * Date: 10/8/11
 * Time: 8:26 PM
 */
public class OfferMovieList extends KeyAdapter implements IntegrationManager {

    public static final int MIN_DELAY_BETWEEN_REQUESTS = 3000;

    private static final Logger logger = Logger.getLogger(OfferMovieList.class);

    @Inject private TmdbService tmdbService;

    @Inject private NewOrEditMovieDialogForm newOrEditMovieDialogForm;

    @Inject private ApplicationManager applicationManager;

    private Combo queryField;

    private volatile long previousTimeFired;
    private volatile String currentQuery;

    private ScheduledThreadPoolExecutor executorService;
    private ResourceBundle bundle;

    public void setQueryField(Combo queryField) {
        this.queryField = queryField;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        final String query = queryField.getText();
        if (query == null || query.length() == 0) {
            queryField.setItems(new String[]{});
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
                    String[] newItems;
                    Movie[] movies = tmdbService.searchForMovies(currentQuery);
                    if (movies == null || movies.length == 0) {
                        newItems = new String[] {
                                bundle.getString("offerList.nothingFound")
                        };
                    } else {
                        newItems = new String[movies.length <= 10 ? movies.length : 10];
                        for (int i = 0; i < newItems.length; i++) {
                            newItems[i] = String.format("%s (%s)", movies[i].getName(), movies[i].getReleasedYear());
                        }
                    }
                    newOrEditMovieDialogForm.setCurrentQueryItems(currentQuery, newItems, movies);
                } catch (TmdbException e1) {
                    newOrEditMovieDialogForm.setCurrentQueryItems(currentQuery, new String[] {
                            bundle.getString("offerList.searchFailed")
                    }, null);
                    logger.error("Error while fetching movie information", e1);
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
                logger.debug("Scheduling search in " + delay + "ms");
            executorService.schedule(fetchItem, delay, TimeUnit.MILLISECONDS);
        }
    }

    public void startup() {
        executorService = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(3);
    }

    public void cleanup() {
        executorService.shutdownNow();
    }

    @Override
    public void applicationStarted() {
        this.bundle = applicationManager.getMessagesBundle();
    }

    @Override
    public void applicationShutdown() {
    }
}
