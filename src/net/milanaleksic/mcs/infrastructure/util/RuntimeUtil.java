package net.milanaleksic.mcs.infrastructure.util;

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

}
