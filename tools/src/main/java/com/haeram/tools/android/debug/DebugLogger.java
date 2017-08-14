package com.haeram.tools.android.debug;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.RequiresPermission;
import android.text.TextUtils;
import android.util.Log;

import com.haeram.tools.com.debug.Logger;
import com.haeram.tools.com.debug.Tracer;

import java.io.File;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Debug logger, catches logs into files under '{external storage}/{package name}' folder.
 * How to use: set this {@link Logger} at {@link Tracer} somewhere init phase on your application.
 * e.g.> Tracer.setLogger(new FileLogger());
 */
public class DebugLogger extends FileLogger
{
	/**
	 * Configurable on build time.
	 */
	private static final String DEBUG_LOG_FILENAME = "dbl";

	/**
	 * Number of debug log files to be kept.
	 */
	private static final int RETAINING_LOG_FILES = 10;

	/**
	 * Control the log level of production.
	 */
	private static final int LEVEL_PROD_LOG = Log.ERROR + 1;

    /**
     * Runtime configurations.
     */
    private static final String CONFIG_LEVEL = "LEVEL";
    private static final String CONFIG_TAG = "TAG";


	/**
	 * Indicates whether the holder application is debuggable.
	 */
	private volatile boolean mDebuggable = false;

	/**
	 * Control the log level of debug.
	 */
	private volatile int mDebugLevel = Log.VERBOSE;

	/**
	 * Controls the allowed tags of debug
	 */
	private final Set<String> mDebugTags = new CopyOnWriteArraySet<>();


	/**
	 * Constructor function.
	 * @param context application context.
	 */
	@RequiresPermission(value = android.Manifest.permission.WRITE_EXTERNAL_STORAGE, conditional = true)
	public DebugLogger (Context context)
	{
		super(getLogDir(context), DEBUG_LOG_FILENAME, RETAINING_LOG_FILES);
	}

	@Override
	public void enable (boolean enabled)
	{
		mDebuggable = enabled;
	}

	@Override
	public void config (String name, String value)
	{
        if (CONFIG_LEVEL.equalsIgnoreCase(name))
        {
            try
            {
                mDebugLevel = Integer.parseInt(value);
            }
            catch (Exception unused)
            {

            }
        }
        else if (CONFIG_TAG.equals(name))
        {
            if (TextUtils.isEmpty(value))
            {
                mDebugTags.clear();
            }
            else
            {
                mDebugTags.add(value);
            }
        }
	}

	@Override
	public boolean isLoggable (String tag, int level)
	{
        if (LEVEL_PROD_LOG <= level)
        {
            return true;
        }

        return mDebuggable && level >= mDebugLevel
                && (mDebugTags.isEmpty() || mDebugTags.contains(tag));
	}

	@Override
	protected void log (int level, String tag, String msg, Throwable tr)
	{
		if (isLoggable(tag, level))
		{
			Log.println(level, tag, null == tr ? msg
					: msg + "\n" + Log.getStackTraceString(tr));

			super.log(level, tag, msg, tr);
		}
	}

	/**
	 * Returns the folder that to be used to save log files.
	 * @return the log folder.
	 */
	private static String getLogDir (Context context)
	{
		File dir = null;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
		{
			dir = context.getExternalFilesDir("log");
		}

		if (null == dir)
		{
			dir = new File(Environment.getExternalStorageDirectory(),
                    context.getPackageName());
		}

		return dir.getAbsolutePath();
	}
}
