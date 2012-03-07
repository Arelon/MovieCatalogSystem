package net.milanaleksic.mcs.infrastructure.worker.impl;

import net.milanaleksic.mcs.infrastructure.config.UserConfiguration;
import net.milanaleksic.mcs.infrastructure.LifecycleListener;
import net.milanaleksic.mcs.infrastructure.config.ApplicationConfiguration;
import net.milanaleksic.mcs.infrastructure.worker.WorkerManager;
import org.apache.log4j.Logger;

import java.util.concurrent.*;

/**
 * User: Milan Aleksic
 * Date: 2/29/12
 * Time: 8:47 AM
 */
public class WorkerManagerImpl implements WorkerManager, LifecycleListener {

    protected final Logger logger = Logger.getLogger(this.getClass());

    public static final int DEFAULT_POOL_TTL = 5000;

    private int timeAllowedForThreadPoolToLiveAfterShutdown = DEFAULT_POOL_TTL;

    private ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private ExecutorService ioBoundPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()*2);

    @Override public <T> Future<T> submitWorker(Callable<T> worker) {
        return pool.submit(worker);
    }

    @Override public Future<?> submitWorker(Runnable runnable) {
        return pool.submit(runnable);
    }

    @Override public <T> Future<T> submitIoBoundWorker(Callable<T> worker) {
        return ioBoundPool.submit(worker);
    }

    @Override public Future<?> submitIoBoundWorker(Runnable runnable) {
        return ioBoundPool.submit(runnable);
    }

    @Override public void applicationStarted(ApplicationConfiguration configuration, UserConfiguration userConfiguration) {
        pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        ioBoundPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()*2);
    }

    @Override public void applicationShutdown(ApplicationConfiguration applicationConfiguration, UserConfiguration userConfiguration) {
        Thread lateShutdown = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(timeAllowedForThreadPoolToLiveAfterShutdown);
                    logger.warn("Allowed time to live time has passed. Proceeding with immediate pool shutdown."); //NON-NLS
                    try { pool.shutdownNow(); } catch(Exception ignored) {}
                    try { ioBoundPool.shutdownNow(); } catch(Exception ignored) {}
                } catch (InterruptedException ignored) { }
            }
        });
        lateShutdown.setDaemon(true);
        lateShutdown.start();
        try { pool.shutdown(); } catch(Exception ignored) {}
        try { ioBoundPool.shutdown(); } catch(Exception ignored) {}
    }

    public void setTimeAllowedForThreadPoolToLiveAfterShutdown(int timeAllowedForThreadPoolToLiveAfterShutdown) {
        this.timeAllowedForThreadPoolToLiveAfterShutdown = timeAllowedForThreadPoolToLiveAfterShutdown;
    }
}
