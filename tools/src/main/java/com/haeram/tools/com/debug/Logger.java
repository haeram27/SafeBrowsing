package com.haeram.tools.com.debug;

/**
 * Implements this interface to catch the log output.
 * v VERBOSE
 * d DEBUG
 * i INFO
 * w WARN
 * e ERROR
 */

public interface Logger {
    /**
     * Checks to see whether or not a log for the specified tag is loggable at the specified level.
     *
     * @param tag   The tag to check.
     * @param level The level to check.
     * @return Whether or not that this is allowed to be logged.
     */
    boolean isLoggable(String tag, int level);

    /**
     * Send a VERBOSE log message.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    void v(String tag, String msg);

    /**
     * Send a VERBOSE log message and log the exception.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr  An exception to log
     */
    void v(String tag, String msg, Throwable tr);

    /**
     * Send a DEBUG log message.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    void d(String tag, String msg);

    /**
     * Send a DEBUG log message and log the exception.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr  An exception to log
     */
    void d(String tag, String msg, Throwable tr);

    /**
     * Send an INFO log message.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    void i(String tag, String msg);

    /**
     * Send a INFO log message and log the exception.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr  An exception to log
     */
    void i(String tag, String msg, Throwable tr);

    /**
     * Send a WARN log message.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    void w(String tag, String msg);

    /**
     * Send a WARN log message and log the exception.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr  An exception to log
     */
    void w(String tag, String msg, Throwable tr);

    /**
     * Send an ERROR log message.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    void e(String tag, String msg);

    /**
     * Send a ERROR log message and log the exception.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr  An exception to log
     */
    void e(String tag, String msg, Throwable tr);

    /**
     * Called to enable/disable logging.
     *
     * @param enabled true to enable.
     */
    void enable(boolean enabled);

    /**
     * Called to config the logger behaviors.
     *
     * @param name  the name of the property
     * @param value the value of the config.
     */
    void config(String name, String value);
}
