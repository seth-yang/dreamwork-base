package org.dreamwork.compilation;

/**
 * Created by seth.yang on 2017/7/25
 */
public class NotSupportedException extends RuntimeException {
    public NotSupportedException () {
    }

    public NotSupportedException (String message) {
        super (message);
    }

    public NotSupportedException (String message, Throwable cause) {
        super (message, cause);
    }

    public NotSupportedException (Throwable cause) {
        super (cause);
    }

    public NotSupportedException (String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super (message, cause, enableSuppression, writableStackTrace);
    }
}