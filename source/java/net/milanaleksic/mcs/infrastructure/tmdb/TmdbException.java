package net.milanaleksic.mcs.infrastructure.tmdb;

/**
 * User: Milan Aleksic
 * Date: 10/7/11
 * Time: 10:20 PM
 */
public class TmdbException extends Exception {

    public TmdbException(String message) {
        super(message);
    }

    public TmdbException(String message, Throwable e) {
        super(message, e);
    }
}
