package org.dreamwork.i18n;

/**
 * Created by IntelliJ IDEA.
 * User: seth.yang
 * Date: 12-10-19
 * Time: 下午4:38
 */
public class MissingResourceException extends RuntimeException {
    public MissingResourceException () {
        super ();
    }

    public MissingResourceException (String message) {
        super (message);
    }

    public MissingResourceException (String message, Throwable cause) {
        super (message, cause);
    }

    public MissingResourceException (Throwable cause) {
        super (cause);
    }
}