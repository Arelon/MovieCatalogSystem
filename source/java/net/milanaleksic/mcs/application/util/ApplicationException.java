package net.milanaleksic.mcs.application.util;

/**
 * User: Milan Aleksic
 * Date: 8/19/11
 * Time: 3:21 PM
 */
public class ApplicationException extends Exception {

    public ApplicationException(String message) {
        super(message);
    }

    public ApplicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
