package com.haeram.tools.com.debug;


/**
 * Provides unified entries for sending Logger output.
 * This class doesn't handle the log output itself, in fact it delegates all log output
 * to a underlying {@link Logger} object, which can be set via {@link #setLogger(Logger)}.
 */
public final class Tracer {
    private static volatile Logger sLogger = null;

    /**
     * Checks to see whether or not a log for the specified tag is loggable at the specified level.
     *
     * @param tag   The tag to check.
     * @param level The level to check.
     * @return Whether or not that this is allowed to be logged.
     */
    public static boolean isLoggable(String tag, int level) {
        final Logger logger = getLogger();
        return (null != logger && logger.isLoggable(tag, level));
    }

    /**
     * Send a VERBOSE Logger message.
     *
     * @param tag Used to identify the source of a Logger message.  It usually identifies
     *            the class or activity where the Logger call occurs.
     * @param msg The message you would like Loggerged.
     */
    public static void v(String tag, String msg) {
        final Logger logger = getLogger();
        if (null != logger) {
            logger.v(tag, msg);
        }
    }

    /**
     * Send a VERBOSE Logger message and Logger the exception.
     *
     * @param tag Used to identify the source of a Logger message.  It usually identifies
     *            the class or activity where the Logger call occurs.
     * @param msg The message you would like Loggerged.
     * @param tr  An exception to Logger
     */
    public static void v(String tag, String msg, Throwable tr) {
        final Logger logger = getLogger();
        if (null != logger) {
            logger.v(tag, msg, tr);
        }
    }

    /**
     * Send a DEBUG Logger message.
     *
     * @param tag Used to identify the source of a Logger message.  It usually identifies
     *            the class or activity where the Logger call occurs.
     * @param msg The message you would like Loggerged.
     */
    public static void d(String tag, String msg) {
        final Logger logger = getLogger();
        if (null != logger) {
            logger.d(tag, msg);
        }
    }

    /**
     * Send a DEBUG Logger message and Logger the exception.
     *
     * @param tag Used to identify the source of a Logger message.  It usually identifies
     *            the class or activity where the Logger call occurs.
     * @param msg The message you would like Loggerged.
     * @param tr  An exception to Logger
     */
    public static void d(String tag, String msg, Throwable tr) {
        final Logger logger = getLogger();
        if (null != logger) {
            logger.d(tag, msg, tr);
        }
    }

    /**
     * Send an INFO Logger message.
     *
     * @param tag Used to identify the source of a Logger message.  It usually identifies
     *            the class or activity where the Logger call occurs.
     * @param msg The message you would like Loggerged.
     */
    public static void i(String tag, String msg) {
        final Logger logger = getLogger();
        if (null != logger) {
            logger.i(tag, msg);
        }
    }

    /**
     * Send a INFO Logger message and Logger the exception.
     *
     * @param tag Used to identify the source of a Logger message.  It usually identifies
     *            the class or activity where the Logger call occurs.
     * @param msg The message you would like Loggerged.
     * @param tr  An exception to Logger
     */
    public static void i(String tag, String msg, Throwable tr) {
        final Logger logger = getLogger();
        if (null != logger) {
            logger.i(tag, msg, tr);
        }
    }

    /**
     * Send a WARN Logger message.
     *
     * @param tag Used to identify the source of a Logger message.  It usually identifies
     *            the class or activity where the Logger call occurs.
     * @param msg The message you would like Loggerged.
     */
    public static void w(String tag, String msg) {
        final Logger logger = getLogger();
        if (null != logger) {
            logger.w(tag, msg);
        }
    }

    /**
     * Send a WARN Logger message and Logger the exception.
     *
     * @param tag Used to identify the source of a Logger message.  It usually identifies
     *            the class or activity where the Logger call occurs.
     * @param msg The message you would like Loggerged.
     * @param tr  An exception to Logger
     */
    public static void w(String tag, String msg, Throwable tr) {
        final Logger logger = getLogger();
        if (null != logger) {
            logger.w(tag, msg, tr);
        }
    }

    /**
     * Send an ERROR Logger message.
     *
     * @param tag Used to identify the source of a Logger message.  It usually identifies
     *            the class or activity where the Logger call occurs.
     * @param msg The message you would like Loggerged.
     */
    public static void e(String tag, String msg) {
        final Logger logger = getLogger();
        if (null != logger) {
            logger.e(tag, msg);
        }
    }

    /**
     * Send a ERROR Logger message and Logger the exception.
     *
     * @param tag Used to identify the source of a Logger message.  It usually identifies
     *            the class or activity where the Logger call occurs.
     * @param msg The message you would like Loggerged.
     * @param tr  An exception to Logger
     */
    public static void e(String tag, String msg, Throwable tr) {
        final Logger logger = getLogger();
        if (null != logger) {
            logger.e(tag, msg, tr);
        }
    }

    /**
     * Sets the Logger implement.
     *
     * @param logger the underlying logger.
     */
    public static synchronized void setLogger(Logger logger) {
        sLogger = logger;
    }

    /**
     * Returns the Logger implement.
     *
     * @return the underlying logger.
     */
    private static synchronized Logger getLogger() {
        return sLogger;
    }
}
