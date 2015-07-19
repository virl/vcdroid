package io.vcdroidkit.transitions;

import android.view.View;
import android.view.ViewGroup;

import io.vcdroidkit.controllers.ViewController;

public class TransitionContextImpl implements TransitionContext
{
	public interface Callback
	{
		void complete(TransitionContextImpl transitionContext);
	}

	private ViewGroup containerView;
	private ViewController fromController;
	private ViewController toController;
	private View fromView;
	private View toView;
	private boolean animated;
	private boolean completed;

	private Callback callback;

	public TransitionContextImpl(
			ViewGroup containerView,
	        ViewController fromController,
	        ViewController toController,
	        View fromView,
	        View toView,
	        boolean animated,
			Callback callback
	)
	{
		this.containerView = containerView;
		this.fromController = fromController;
		this.toController = toController;
		this.fromView = fromView;
		this.toView = toView;
		this.animated = animated;

		this.callback = callback;
	}

	public boolean isCompleted()
	{
		return completed;
	}

	@Override
	public void completeTransition(boolean didComplete)
	{
		this.completed = didComplete;
		callback.complete(this);
	}

	@Override
	public ViewGroup getContainerView()
	{
		return containerView;
	}

	@Override
	public ViewController getFromController()
	{
		return fromController;
	}

	@Override
	public ViewController getToController()
	{
		return toController;
	}

	@Override
	public View getFromView()
	{
		return fromView;
	}

	@Override
	public View getToView()
	{
		return toView;
	}

	@Override
	public boolean isAnimated()
	{
		return animated;
	}
} // TransitionContextImpl
