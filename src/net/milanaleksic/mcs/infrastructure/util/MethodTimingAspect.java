package net.milanaleksic.mcs.infrastructure.util;

import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;

import java.text.NumberFormat;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * User: Milan Aleksic
 * Date: 8/19/11
 * Time: 9:18 PM
 */
@Aspect
public class MethodTimingAspect {

    protected final Logger log = Logger.getLogger(this.getClass());
    private static final long warningTime = 100;

    private AtomicBoolean thisIsFirstQuery = new AtomicBoolean(true);

    @Pointcut(value="execution(* net.milanaleksic.mcs.infrastructure.persistence.jpa.*Repository.*(..)) ||" +
            "execution(@net.milanaleksic.mcs.infrastructure.util.MethodTiming * * ())")
    private void timedMethod() {}


    @Around("timedMethod()")
    public Object doTimeMethod(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        if (thisIsFirstQuery.getAndSet(false) || !log.isDebugEnabled())
            return proceedingJoinPoint.proceed();
        long begin = System.nanoTime();
        Object result = proceedingJoinPoint.proceed();
        double periodUs = (System.nanoTime() - begin) / 1000000.0;
        String periodAsString = NumberFormat.getInstance().format(periodUs);
        if (periodUs >= warningTime)
            log.warn("MethodTiming (long) [" + proceedingJoinPoint.getSignature().toShortString() + "] - "
                    + periodAsString + "ms");
        else
            log.debug("MethodTiming [" + proceedingJoinPoint.getSignature().toShortString() + "] - "
                    + periodAsString + "ms");
        return result;
    }
}
