package net.milanaleksic.mcs.infrastructure.util;

import com.google.common.base.Supplier;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * User: Milan Aleksic
 * Date: 4/11/12
 * Time: 1:35 PM
 */
public class RuntimeUtil {

    public static String getStackTrace() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        StringBuilder builder = new StringBuilder();
        // starting the count from 2 instead of 0 to avoid printing of this and Thread.getStackTrace methods
        for (int i = 2, stackTraceLength = stackTrace.length; i < stackTraceLength; i++) {
            StackTraceElement stackTraceElement = stackTrace[i];
            builder.append(stackTraceElement.getClassName()).append(".").append(stackTraceElement.getMethodName());
            builder.append('\r').append('\n');
        }
        return builder.toString();
    }

    public static <T> T promoteReadLockToWriteLockAndProcess(ReentrantReadWriteLock lock, Supplier<T> function) {
        final int holdCount = lock.getReadHoldCount();
        for (int i = 0; i < holdCount; i++) {
            lock.readLock().unlock();
        }
        Lock writeLock = lock.writeLock();
        writeLock.lock();
        try {
            return function.get();
        } finally {
            for (int i = 0; i < holdCount; i++) {
                lock.readLock().lock();
            }
            writeLock.unlock();
        }
    }

}
