package io.vcdroidkit.controllers;

import android.content.Context;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

public class RootView extends RelativeLayout
{
	private int enabledCounter = 1;

	public RootView(Context context)
	{
		super(context);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev)
	{
		if(this.isInteractionEnabled())
			return super.onInterceptTouchEvent(ev);

		return true;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		if(this.isInteractionEnabled())
			return super.onTouchEvent(event);

		return true;
	}

	public boolean isInteractionEnabled()
	{
		return enabledCounter >= 1;
	}

	public void enableInteraction()
	{
		++enabledCounter;
	}

	public void disableInteraction()
	{
		--enabledCounter;
	}
} // RootView
