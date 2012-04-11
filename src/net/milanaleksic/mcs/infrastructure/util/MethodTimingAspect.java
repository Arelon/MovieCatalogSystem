package net.milanaleksic.mcs.infrastructure.util;

import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;

import java.text.NumberFormat;

/**
 * User: Milan Aleksic
 * Date: 8/19/11
 * Time: 9:18 PM
 */
@Aspect
public class MethodTimingAspect {

    protected final Logger log = Logger.getLogger(this.getClass());
    private static final long warningTimeInMs = 100;

    @SuppressWarnings({"EmptyMethod"})
    @Pointcut(value = "execution(* net.milanaleksic.mcs.infrastructure.persistence.jpa.*Repository.*(..))")
    private void timedMethod() {
    }

    @Around("timedMethod()")
    public Object doTimeMethod(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        long begin = System.nanoTime();
        Object result = proceedingJoinPoint.proceed();
        double periodInMs = (System.nanoTime() - begin) / 1000000.0;

        String periodAsString = NumberFormat.getInstance().format(periodInMs);
        if (periodInMs >= warningTimeInMs)
            log.warn("MethodTiming (long) [" + proceedingJoinPoint.getSignature().toShortString() + "] - " + periodAsString + "ms"); //NON-NLS
        else {
            if (log.isDebugEnabled())
                log.debug("MethodTiming [" + proceedingJoinPoint.getSignature().toShortString() + "] - " + periodAsString + "ms"); //NON-NLS
        }
        return result;
    }

    @SuppressWarnings({"EmptyMethod"})
    @Pointcut(value = "(execution(@net.milanaleksic.mcs.infrastructure.util.MethodTiming * * ()) ||" +
            "execution(@net.milanaleksic.mcs.infrastructure.util.MethodTiming * * (*))) && @annotation(methodTiming)")
    private void timedWithAnnotationMethod(MethodTiming methodTiming) {
    }

    @Around(value = "timedWithAnnotationMethod(methodTiming)")
    public Object doTimeMethodWithAnnotation(ProceedingJoinPoint proceedingJoinPoint, MethodTiming methodTiming) throws Throwable {
        long begin = System.nanoTime();
        Object result = proceedingJoinPoint.proceed();
        double periodInMs = (System.nanoTime() - begin) / 1000000.0;

        String periodAsString = NumberFormat.getInstance().format(periodInMs);
        String title = methodTiming.name().isEmpty() ? proceedingJoinPoint.getSignature().toShortString() : "<named> "+methodTiming.name();
        if (periodInMs >= warningTimeInMs)
            log.warn("MethodTiming (long) [" + title + "] - " + periodAsString + "ms"); //NON-NLS
        else {
            if (log.isDebugEnabled())
                log.debug("MethodTiming [" + title + "] - " + periodAsString + "ms"); //NON-NLS
        }
        return result;
    }
}
