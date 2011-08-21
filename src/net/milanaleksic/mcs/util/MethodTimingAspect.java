package net.milanaleksic.mcs.util;

import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * User: Milan Aleksic
 * Date: 8/19/11
 * Time: 9:18 PM
 */
public class MethodTimingAspect {

    protected final Logger log = Logger.getLogger(this.getClass());
    private long warningTime = 100;

    private AtomicBoolean thisIsFirstQuery = new AtomicBoolean(true);

    public void setWarningTime(long warningTime) {
        this.warningTime = warningTime;
    }

    public Object timeMethod(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        if (thisIsFirstQuery.getAndSet(false))
            return proceedingJoinPoint.proceed();
        long begin = System.currentTimeMillis();
        Object result = proceedingJoinPoint.proceed();
        long period = System.currentTimeMillis() - begin;
        if (period >= warningTime)
            log.warn("MethodTiming (long) [" + proceedingJoinPoint.getSignature().toShortString() + "] - " + period + "ms");
        else
            log.debug("MethodTiming [" + proceedingJoinPoint.getSignature().toShortString() + "] - " + period + "ms");
        return result;
    }
}
