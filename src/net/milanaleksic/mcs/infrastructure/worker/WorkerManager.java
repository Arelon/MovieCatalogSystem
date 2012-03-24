package net.milanaleksic.mcs.infrastructure.worker;

import com.google.common.base.Function;
import net.milanaleksic.mcs.domain.model.Film;

import java.util.List;
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

    <T> Future<T> submitIoBoundWorker(Callable<T> worker) ;

    Future<?> submitIoBoundWorker(Runnable runnable);

    <T> void submitLongTaskWithResultProcessingInSWTThread(Callable<T> longTask, Function<T, Void> operationOnResultOfLongTask);
}
