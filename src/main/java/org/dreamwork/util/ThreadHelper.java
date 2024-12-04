package org.dreamwork.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadHelper {
    private static final Logger logger = LoggerFactory.getLogger (ThreadHelper.class);

    public static void delay (long duration) {
        try {
            Thread.sleep (duration);
        } catch (InterruptedException ex) {
            if (logger.isTraceEnabled ()) {
                logger.warn (ex.getMessage (), ex);
            }
        }
    }
}
