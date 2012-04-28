package net.milanaleksic.mcs.infrastructure.worker;

import com.google.common.base.Function;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.Callable;

/**
 * User: Milan Aleksic
 * Date: 2/29/12
 * Time: 8:46 AM
 */
public interface WorkerManager {

    <T> ListenableFuture<T> submitWorker(Callable<T> worker) ;

    ListenableFuture<?> submitWorker(Runnable runnable);

    <T> ListenableFuture<T> submitIoBoundWorker(Callable<T> worker) ;

    ListenableFuture<?> submitIoBoundWorker(Runnable runnable);

    <T> ListenableFuture<?> submitLongTaskWithResultProcessingInSWTThread(Callable<T> longTask, Function<T, Void> operationOnResultOfLongTask);
}
