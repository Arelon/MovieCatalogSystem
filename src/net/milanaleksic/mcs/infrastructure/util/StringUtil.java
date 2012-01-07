package net.milanaleksic.mcs.infrastructure.util;

/**
 * User: Milan Aleksic
 * Date: 1/7/12
 * Time: 10:28 AM
 */
public class StringUtil {

    public static String emptyIfNull(String str) {
        return str == null ? "" : str;
    }

    public static String emptyIfNullOtherwiseConvert(int val) {
        return val == 0 ? "" : String.valueOf(val);
    }

}
