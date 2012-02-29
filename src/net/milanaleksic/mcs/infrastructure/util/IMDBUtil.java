package net.milanaleksic.mcs.infrastructure.util;

import java.net.URI;

/**
 * User: Milan Aleksic
 * Date: 2/29/12
 * Time: 4:06 PM
 */
public class IMDBUtil {

    public static String createUrlBasedOnId(String id) {
        return "http://www.imdb.com/title/"+id+"/";
    }

    public static URI createUriBasedOnId(String id) {
        return URI.create("http://www.imdb.com/title/" + id + "/");
    }

}
