package net.milanaleksic.mcs.infrastructure.worker;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * User: Milan Aleksic
 * Date: 2/29/12
 * Time: 8:46 AM
 */
public interface WorkerManager {

    <T> Future<T> submitWorker(Callable<T> worker) ;

    Future<?> submitWorker(Runnable runnable);

}
