package com.haeram.tools.android.debug;

import android.os.SystemClock;
import android.text.format.DateFormat;
import android.util.Log;

import com.haeram.tools.com.debug.Logger;
import com.haeram.tools.com.debug.Tracer;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * File logger, which catches logs into files.
 * How to use: set this {@Link Logger} at {@Link Tracer} somewhere init phase on your application.
 * e.g.> Tracer.setLogger(new FileLogger());
 */
public class FileLogger implements Logger, Closeable, Runnable {
    private static final String TAG = "FileLogger";

    /**
     * Log file extension.
     */
    private static final String LOG_FILE_EXTENSION = ".log";

    /**
     * Expected Log file size (the exact size of each log file can between LOG_FILE_SIZE t0 2 * LOG_FILE_SIZE) .
     */
    private static final long LOG_FILE_SIZE = 1 * 1024 * 1024L; // 1MB

    /**
     * Log buffer size.
     */
    private static final int LOG_BUFFER_SIZE = 10 * 1024; // 10KB

    /**
     * Interval for two log file flushing operations.
     */
    private static final long LOG_FLUSHING_INTERVAL = 5000L; // 5 seconds

    /**
     * Log name for each level.
     */
    private static final String[] LEVEL_NAME = new String[]{"V", "D", "I", "W", "E", "A"};

    /**
     * The log file directory.
     */
    private final String mLogDir;

    /**
     * The log file name prefix.
     */
    private final String mLogFile;

    /**
     * Number of log files to be kept.
     */
    private final int mMaxLogFiles;

    /**
     * Use to
     */
    private final Object mSync = new Object();

    /**
     * Indicates whether this writer has been closed.
     */
    private volatile boolean mClosed = false;

    /**
     * To cache the log.
     */
    private StringBuffer mBuffer;


    /**
     * Constructor function.
     *
     * @param dir  the folder to save log files.
     * @param file the log file name prefix.
     * @param num  the maximum number of log files that can be kept.
     */
    public FileLogger(String dir, String file, int num) {
        mLogDir = dir;
        mLogFile = file;
        mMaxLogFiles = num > 2 ? num : 2;
    }

    @Override
    public void close() {
        synchronized (mSync) {
            mClosed = true;
            mSync.notifyAll();
        }
    }

    @Override
    public boolean isLoggable(String tag, int level) {
        return true;
    }

    @Override
    public void v(String tag, String msg) {
        log(Log.VERBOSE, tag, msg, null);
    }

    @Override
    public void v(String tag, String msg, Throwable tr) {
        log(Log.VERBOSE, tag, msg, tr);
    }

    @Override
    public void d(String tag, String msg) {
        log(Log.DEBUG, tag, msg, null);
    }

    @Override
    public void d(String tag, String msg, Throwable tr) {
        log(Log.DEBUG, tag, msg, tr);
    }

    @Override
    public void i(String tag, String msg) {
        log(Log.INFO, tag, msg, null);
    }

    @Override
    public void i(String tag, String msg, Throwable tr) {
        log(Log.INFO, tag, msg, tr);
    }

    @Override
    public void w(String tag, String msg) {
        log(Log.WARN, tag, msg, null);
    }

    @Override
    public void w(String tag, String msg, Throwable tr) {
        log(Log.WARN, tag, msg, tr);
    }

    @Override
    public void e(String tag, String msg) {
        log(Log.ERROR, tag, msg, null);
    }

    @Override
    public void e(String tag, String msg, Throwable tr) {
        log(Log.ERROR, tag, msg, tr);
    }

    @Override
    public void enable(boolean enabled) {
        // Do nothing, not supported.
    }

    @Override
    public void config(String name, String value) {
        // Do nothing, not supported.
    }

    @Override
    public void run() {
        long lastFlushTime = 0;

        // Runs on a background thread to save logs to file.
        while (true) {
            synchronized (mSync) {
                int len = mBuffer.length();
                long rest = LOG_FLUSHING_INTERVAL + lastFlushTime - SystemClock.elapsedRealtime();

                // Waits until the log is closed, or the buffer is full, any log needs to write.
                // Also makes sure the interval between two flushing operations is not less then
                // LOG_FLUSHING_INTERVAL.
                while (!mClosed && LOG_FILE_SIZE > len && (0 == len || 0 < rest)) {
                    try {
                        mSync.wait(0 == len ? Long.MAX_VALUE : rest);
                    } catch (InterruptedException unused) {
                    }

                    len = mBuffer.length();
                    rest = LOG_FLUSHING_INTERVAL + lastFlushTime - SystemClock.elapsedRealtime();
                }

                if (mClosed) {
                    break;
                }
            }

            // Flushes the logs.
            flush();

            lastFlushTime = SystemClock.elapsedRealtime();
        }

        // Flushes the logs before exit.
        flush();

        mBuffer = null;
    }

    /**
     * Logs a message.
     *
     * @param level log level.
     * @param tag   log tag.
     * @param msg   detail description.
     * @param tr    optional error.
     */
    protected void log(int level, String tag, String msg, Throwable tr) {
        if (mClosed) {
            return;
        }

        String log = getLogMessage(level, tag, msg, tr);

        // Writes the log to buffer.
        synchronized (mSync) {
            if (!mClosed) {
                ensureFlushThreadLocked();

                try {
                    mBuffer.append(log);
                    mSync.notifyAll();
                } catch (Throwable unused) {
                }
            }
        }
    }

    /**
     * Ensures that the flush thread has been created.
     */
    private void ensureFlushThreadLocked() {
        if (null == mBuffer) {
            mBuffer = new StringBuffer(LOG_BUFFER_SIZE);
            Thread thread = new Thread(this, TAG);
            thread.setDaemon(true);
            thread.setPriority(Thread.NORM_PRIORITY - 1);
            thread.start();
        }
    }

    /**
     * Flushes the logs into log to file.
     */
    private void flush() {
        Writer writer = null;
        try {
            writer = getLogWriter();

            // Copy the message from buffer.
            String msg;
            synchronized (mSync) {
                msg = mBuffer.toString();
                freeBufferLocked();
            }

            // Writes to the log file.
            writer.write(msg);
            writer.flush();
        } catch (Throwable unused) {
            // Reset the buffer in case that the buffer is overflow.
            synchronized (mSync) {
                if (mBuffer.length() > LOG_BUFFER_SIZE) {
                    freeBufferLocked();
                }
            }
        } finally {
            if (null != writer) {
                try {
                    writer.close();
                } catch (Exception unused) {
                }
            }
        }
    }

    /**
     * Called to free the log buffer {@link #mBuffer}.
     */
    private void freeBufferLocked() {
        mBuffer.setLength(0);
        if (mBuffer.capacity() > 8 * LOG_BUFFER_SIZE) {
            mBuffer.trimToSize();
        }
    }

    /**
     * Returns a writer to the log file.
     *
     * @return a writer.
     * @throws Exception
     */
    //@FindBugsSuppressWarnings("RV_RETURN_VALUE_IGNORED_BAD_PRACTICE")
    private Writer getLogWriter() throws Exception {
        File dir = new File(mLogDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        /* Check if the current file is over the the per file size threshold.
         * If the file is above the threshold then it is renamed to filenameX.ext.
         * Where X is a number from 1 to MAX_LOG_FILE_NUM. All existing log files
         * are renamed to filename(X+1).log and the filename10.log is deleted if
         * it exists.
         */
        File file = new File(mLogDir, mLogFile + LOG_FILE_EXTENSION);
        if (file.exists() && file.length() >= LOG_FILE_SIZE) {
            int maxId = mMaxLogFiles - 1;
            File oldest = new File(mLogDir, mLogFile + maxId + LOG_FILE_EXTENSION);
            oldest.delete();

            for (int i = maxId - 1; i > 0; i--) {
                File old = new File(mLogDir, mLogFile + i + LOG_FILE_EXTENSION);
                if (old.exists()) {
                    old.renameTo(new File(mLogDir, mLogFile + (i + 1) + LOG_FILE_EXTENSION));
                }
            }

            file.renameTo(new File(mLogDir, mLogFile + "1" + LOG_FILE_EXTENSION));
        }

        return new OutputStreamWriter(new FileOutputStream(mLogDir + File.separator
                + mLogFile + LOG_FILE_EXTENSION, true), "UTF-8");
    }

    /**
     * Builds the message for a log.
     *
     * @param level the log level.
     * @param tag   the log tag.
     * @param msg   the detail message.
     * @param tr    an optional error.
     * @return a human readable log message.
     */
    private static String getLogMessage(int level, String tag, String msg, Throwable tr) {
        StringBuilder builder = new StringBuilder();
        builder.append(DateFormat.format("MM/dd/yyyy kk:mm:ss", System.currentTimeMillis()));
        builder.append('\t');
        builder.append(getLogLevel(level));
        builder.append('\t');
        builder.append(tag);
        builder.append('\t');
        builder.append("Thread: ");
        builder.append(Thread.currentThread().getId());
        builder.append('\t');
        builder.append(msg);
        if (null != tr) {
            builder.append('\n');
            builder.append(Log.getStackTraceString(tr));
        }
        builder.append('\n');

        return builder.toString();
    }

    /**
     * Returns the string of a given level.
     *
     * @param level the log level.
     * @return a string represents the specified log level.
     */
    private static String getLogLevel(int level) {
        int pos = level - Log.VERBOSE;
        return pos >= 0 && pos < LEVEL_NAME.length ? LEVEL_NAME[pos] : String.valueOf(level);
    }
}
