package net.milanaleksic.mcs.infrastructure.worker.impl;

import com.google.common.base.Function;
import net.milanaleksic.mcs.infrastructure.config.UserConfiguration;
import net.milanaleksic.mcs.infrastructure.LifecycleListener;
import net.milanaleksic.mcs.infrastructure.config.ApplicationConfiguration;
import net.milanaleksic.mcs.infrastructure.worker.WorkerManager;
import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Display;
import org.springframework.jmx.export.annotation.*;

import java.util.concurrent.*;

/**
 * User: Milan Aleksic
 * Date: 2/29/12
 * Time: 8:47 AM
 */
@ManagedResource(
        objectName = "net.milanaleksic.mcs:name=workerManager",
        description = "Application work manager",
        currencyTimeLimit = -1
)
public class WorkerManagerImpl implements WorkerManager, LifecycleListener {

    protected final Logger logger = Logger.getLogger(this.getClass());

    public static final int DEFAULT_POOL_TTL = 5000;

    private int timeAllowedForThreadPoolToLiveAfterShutdown = DEFAULT_POOL_TTL;

    private ExecutorService cpuBoundPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private ExecutorService ioBoundPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);

    @Override
    public <T> Future<T> submitWorker(Callable<T> worker) {
        return cpuBoundPool.submit(worker);
    }

    @Override
    public Future<?> submitWorker(Runnable runnable) {
        return cpuBoundPool.submit(runnable);
    }

    @Override
    public <T> Future<T> submitIoBoundWorker(Callable<T> worker) {
        return ioBoundPool.submit(worker);
    }

    @Override
    public Future<?> submitIoBoundWorker(Runnable runnable) {
        return ioBoundPool.submit(runnable);
    }

    @Override
    public <T> Future<?> submitLongTaskWithResultProcessingInSWTThread(final Callable<T> longTask, final Function<T, Void> operationOnResultOfLongTask) {
        return cpuBoundPool.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    final T longTaskResult = longTask.call();
                    Display.getDefault().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            operationOnResultOfLongTask.apply(longTaskResult);
                        }
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    logger.error("Exception occurred in task processing", e); //NON-NLS
                }
            }

        });
    }

    @Override
    public void applicationStarted(ApplicationConfiguration configuration, UserConfiguration userConfiguration) {
        cpuBoundPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        ioBoundPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
    }

    @Override
    public void applicationShutdown(ApplicationConfiguration applicationConfiguration, UserConfiguration userConfiguration) {
        Thread lateShutdown = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(timeAllowedForThreadPoolToLiveAfterShutdown);
                    logger.warn("Allowed time to live time has passed. Proceeding with immediate pool shutdown."); //NON-NLS
                    try {
                        cpuBoundPool.shutdownNow();
                    } catch (Exception ignored) {
                    }
                    try {
                        ioBoundPool.shutdownNow();
                    } catch (Exception ignored) {
                    }
                } catch (InterruptedException ignored) {
                }
            }
        });
        lateShutdown.setDaemon(true);
        lateShutdown.start();
        try {
            cpuBoundPool.shutdown();
        } catch (Exception ignored) {
        }
        try {
            ioBoundPool.shutdown();
        } catch (Exception ignored) {
        }
    }

    public void setTimeAllowedForThreadPoolToLiveAfterShutdown(int timeAllowedForThreadPoolToLiveAfterShutdown) {
        this.timeAllowedForThreadPoolToLiveAfterShutdown = timeAllowedForThreadPoolToLiveAfterShutdown;
    }

    @ManagedAttribute(description = "Pool size for CPU-bound pool")
    public int getCpuBoundPoolSize() {
        return ((ThreadPoolExecutor) cpuBoundPool).getPoolSize();
    }

    @ManagedAttribute(description = "Pool size for IO-bound pool")
    public int getIoBoundPoolSize() {
        return ((ThreadPoolExecutor)ioBoundPool).getPoolSize();
    }

    @ManagedAttribute(description = "Current active thread count for CPU-bound pool")
    public int getCpuBoundPoolActiveThreadCount() {
        return ((ThreadPoolExecutor) cpuBoundPool).getActiveCount();
    }

    @ManagedAttribute(description = "Current active thread count for IO-bound pool")
    public int getIoBoundPoolActiveThreadCount() {
        return ((ThreadPoolExecutor)ioBoundPool).getActiveCount();
    }

}
