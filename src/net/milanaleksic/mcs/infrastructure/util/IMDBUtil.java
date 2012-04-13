package net.milanaleksic.mcs.infrastructure.util;

import com.google.common.base.Strings;

import java.net.URI;
import java.util.regex.Pattern;

/**
 * User: Milan Aleksic
 * Date: 2/29/12
 * Time: 4:06 PM
 */
public class IMDBUtil {

    private static final Pattern PATTERN_IMDB_ID = Pattern.compile("tt\\d{7}"); //NON-NLS

    private static final String IMDB_URL_TITLE = "http://www.imdb.com/title/%s/"; //NON-NLS

    public static String createUrlBasedOnId(String id) {
        return String.format(IMDB_URL_TITLE, id);
    }

    public static URI createUriBasedOnId(String id) {
        return URI.create(createUrlBasedOnId(id));
    }

    public static boolean isValidImdbId(String imdbId) {
        return !Strings.isNullOrEmpty(imdbId) && PATTERN_IMDB_ID.matcher(imdbId).matches();
    }
}
