package net.milanaleksic.mcs.application.gui.helper;

import com.google.common.base.Optional;
import net.milanaleksic.mcs.application.ApplicationManager;
import net.milanaleksic.mcs.infrastructure.IntegrationManager;
import net.milanaleksic.mcs.infrastructure.config.ApplicationConfiguration;
import net.milanaleksic.mcs.infrastructure.config.UserConfiguration;
import net.milanaleksic.mcs.infrastructure.tmdb.TmdbException;
import net.milanaleksic.mcs.infrastructure.tmdb.TmdbService;
import net.milanaleksic.mcs.infrastructure.tmdb.bean.Movie;
import net.milanaleksic.mcs.infrastructure.util.SWTUtil;
import org.apache.log4j.Logger;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.*;

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
        void setCurrentQueryItems(String currentQuery, Optional<String> message, Optional<Movie[]> movies);
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

    private Optional<ScheduledThreadPoolExecutor> executorService = Optional.absent();
    private ResourceBundle bundle;

    public void setReceiver(Receiver receiver) {
        this.receiver = receiver;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (!Character.isLetterOrDigit(e.character)
                && java.awt.event.KeyEvent.VK_BACK_SPACE != e.keyCode
                && java.awt.event.KeyEvent.VK_DELETE != e.keyCode
                && java.awt.event.KeyEvent.VK_SPACE != e.keyCode
                && java.awt.event.KeyEvent.VK_PASTE != e.keyCode
                && java.awt.event.KeyEvent.VK_CUT != e.keyCode)
            return;
        refreshRecommendations();
    }

    private synchronized void executeSearch(String query) {
        Runnable fetchItem = new Runnable() {
            @Override
            public void run() {
                try {
                    previousTimeFired = System.currentTimeMillis();
                    Optional<String> message = Optional.absent();
                    Optional<Movie[]> moviesOptional = tmdbService.searchForMovies(currentQuery);
                    if (!moviesOptional.isPresent() || moviesOptional.get().length == 0)
                        message = Optional.of(bundle.getString("offerList.nothingFound"));
                    receiver.setCurrentQueryItems(currentQuery, message, moviesOptional);
                } catch (TmdbException e1) {
                    logger.error("Error while fetching movie information", e1); //NON-NLS
                    receiver.setCurrentQueryItems(currentQuery, Optional.of(bundle.getString("offerList.searchFailed")), Optional.<Movie[]>absent());
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
        if (executorService.get().getQueue().size() == 0) {
            if (logger.isDebugEnabled())
                logger.debug("Scheduling search in " + delay + "ms"); //NON-NLS
            executorService.get().schedule(fetchItem, delay, TimeUnit.MILLISECONDS);
        }
    }

    public void startup() {
        executorService = Optional.of((ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(3));
    }

    public void cleanup() {
        if (executorService.isPresent() && !executorService.get().isShutdown())
            executorService.get().shutdownNow();
    }

    @Override
    public void applicationStarted(ApplicationConfiguration configuration, UserConfiguration userConfiguration) {
        this.bundle = applicationManager.getMessagesBundle();
    }

    @Override
    public void applicationShutdown(ApplicationConfiguration applicationConfiguration, UserConfiguration userConfiguration) {
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

    public void refreshRecommendations() {
        final String query = SWTUtil.getTextFrom(source);
        if (query.isEmpty()) {
            receiver.setCurrentQueryItems(query, Optional.of(bundle.getString("offerList.noSearchString")), Optional.<Movie[]>absent());
            return;
        }
        executeSearch(query);
    }
}
