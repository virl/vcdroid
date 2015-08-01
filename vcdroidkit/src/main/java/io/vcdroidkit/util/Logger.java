package io.vcdroidkit.util;

import android.util.Log;

public class Logger
{
	private static final boolean enabled = false;
	private static final String tag = "vcdroid";

	public static void log()
	{
		if(!enabled)
			return;

		StackTraceElement[] st = Thread.getAllStackTraces().get(Thread.currentThread());
		if(st == null || st.length < 5)
			return;

		StackTraceElement e = st[4];

		StringBuilder sb = new StringBuilder()
				.append(e.getFileName())
				.append(" ")
				.append(e.getMethodName())
				.append(":")
				.append(e.getLineNumber());

		Log.i(tag, sb.toString());
	} // log
} // Logger
