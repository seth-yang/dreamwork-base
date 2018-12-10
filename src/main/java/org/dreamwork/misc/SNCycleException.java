package org.dreamwork.misc;

/**
 * Created by IntelliJ IDEA.
 * User: seth.yang
 * Date: 2014/12/15
 * Time: 23:51
 */
public class SNCycleException extends RuntimeException {
    public SNCycleException () {
        super ();
    }

    public SNCycleException (String message) {
        super (message);
    }

    public SNCycleException (String message, Throwable cause) {
        super (message, cause);
    }

    public SNCycleException (Throwable cause) {
        super (cause);
    }
}