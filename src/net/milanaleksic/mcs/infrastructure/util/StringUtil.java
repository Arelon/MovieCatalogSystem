package net.milanaleksic.mcs.infrastructure.util;

/**
 * User: Milan Aleksic
 * Date: 1/7/12
 * Time: 10:28 AM
 */
public class StringUtil {

    public static String emptyIfNullOtherwiseConvert(int val) {
        return val == 0 ? "" : String.valueOf(val);
    }

    public static String showMillisIntervalAsString(long period) {
        int minutes = period > 60000 ? ((int)period / 1000) / 60 : 0;
        int seconds = period > 1000 ? ((int)period / 1000) % 60 : 0;
        int milliseconds = (int) period % 1000;
        return String.format("%02d:%02d.%03d", minutes, seconds, milliseconds);
    }

}
