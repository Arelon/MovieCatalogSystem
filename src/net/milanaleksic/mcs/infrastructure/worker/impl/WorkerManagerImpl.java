package net.milanaleksic.mcs.infrastructure.worker.impl;

import net.milanaleksic.mcs.application.LifecycleListener;
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

    @Override public <T> Future<T> submitWorker(Callable<T> worker) {
        return pool.submit(worker);
    }

    @Override public Future<?> submitWorker(Runnable runnable) {
        return pool.submit(runnable);
    }

    @Override public void applicationStarted() { }

    @Override public void applicationShutdown() {
        Thread lateShutdown = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(timeAllowedForThreadPoolToLiveAfterShutdown);
                    logger.warn("Allowed time to live time has passed. Proceeding with immediate pool shutdown.");
                    pool.shutdownNow();
                } catch (InterruptedException ignored) { }
            }
        });
        lateShutdown.setDaemon(true);
        lateShutdown.start();
        pool.shutdown();
    }

    public void setTimeAllowedForThreadPoolToLiveAfterShutdown(int timeAllowedForThreadPoolToLiveAfterShutdown) {
        this.timeAllowedForThreadPoolToLiveAfterShutdown = timeAllowedForThreadPoolToLiveAfterShutdown;
    }
}
