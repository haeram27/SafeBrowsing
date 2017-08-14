package com.haeram.tools.android.debug;

import android.util.Log;

import com.haeram.tools.com.debug.Logger;
import com.haeram.tools.com.debug.Tracer;

/**
 * A {@link Logger} to output logs to logcat.
 * How to use: set this {@Link Logger} at {@Link Tracer} somewhere init phase on your application.
 * e.g.> Tracer.setLogger(new SystemLogger());
 */
public class SystemLogger implements Logger
{
	/**
	 * Construct function.
	 */
	public SystemLogger ()
	{	
	}

	@Override
	public boolean isLoggable (String tag, int level)
	{
		return true;
	}

	@Override
	public void v (String tag, String msg)
	{
		Log.v(tag, msg);
	}

	@Override
	public void v (String tag, String msg, Throwable tr)
	{
		Log.v(tag, msg, tr);
	}

	@Override
	public void d (String tag, String msg)
	{
		Log.d(tag, msg);
	}

	@Override
	public void d (String tag, String msg, Throwable tr)
	{
		Log.d(tag, msg, tr);
	}

	@Override
	public void i (String tag, String msg)
	{
		Log.i(tag, msg);
	}

	@Override
	public void i (String tag, String msg, Throwable tr)
	{
		Log.i(tag, msg, tr);
	}

	@Override
	public void w (String tag, String msg)
	{
		Log.w(tag, msg);
	}

	@Override
	public void w (String tag, String msg, Throwable tr)
	{
		Log.w(tag, msg, tr);
	}

	@Override
	public void e (String tag, String msg)
	{
		Log.e(tag, msg);
	}

	@Override
	public void e (String tag, String msg, Throwable tr)
	{
		Log.e(tag, msg, tr);
	}

	@Override
	public void enable (boolean enabled)
	{
		// Do nothing, system log can't be turned on or off.
	}

	@Override
    public void config (String name, String value)
	{
		// Do nothing, system log is not configurable.
	}
}
