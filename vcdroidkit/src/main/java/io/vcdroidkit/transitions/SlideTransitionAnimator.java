package io.vcdroidkit.transitions;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.RelativeLayout;

public class SlideTransitionAnimator implements TransitionAnimator
{
	private long duration;
	private boolean push;

	public SlideTransitionAnimator(long duration, boolean push)
	{
		this.push = push;
		this.duration = duration;
	}

	@Override
	public void animateTransition(final TransitionContext context)
	{
		final ViewGroup containerView = context.getContainerView();

		RelativeLayout.LayoutParams params =
				new RelativeLayout.LayoutParams(
						ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.MATCH_PARENT);

		int fromIndex = containerView.indexOfChild(context.getFromView());

		if(context.shouldAddToView())
		{
			containerView.addView(
					context.getToView(),
					push ? containerView.getChildCount() : fromIndex,
					params
			);
		}

		if(!context.isAnimated())
		{
			context.completeTransition(true);
			return;
		}

		final View fromView = context.getFromView();
		final View toView = context.getToView();

		final int fromViewLayerType = fromView.getLayerType();
		final int toViewLayerType = toView.getLayerType();
		fromView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		toView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

		AnimatorSet set = new AnimatorSet();

		if(push)
		{
			ObjectAnimator animator;

			toView.setTranslationX(fromView.getWidth());
			animator = ObjectAnimator.ofFloat(toView, "translationX", (float) fromView.getWidth(), 0f);
			animator.setInterpolator(new DecelerateInterpolator());
			animator.setDuration(duration);
			AnimatorSet.Builder builder = set.play(animator);
		}
		else
		{
			ObjectAnimator animator;

			fromView.setTranslationX(0);
			animator = ObjectAnimator.ofFloat(fromView, "translationX", 0f, (float) fromView.getWidth());
			animator.setInterpolator(new DecelerateInterpolator());
			animator.setDuration(duration);
			AnimatorSet.Builder builder = set.play(animator);
		}

		// Disappearing views: http://stackoverflow.com/a/5397465/1449965

		set.addListener(new Animator.AnimatorListener()
		{
			@Override
			public void onAnimationStart(Animator animation)
			{
			}

			@Override
			public void onAnimationEnd(Animator animation)
			{
				fromView.setLayerType(fromViewLayerType, null);
				toView.setLayerType(toViewLayerType, null);

				new Handler(Looper.getMainLooper()).post(new Runnable()
				{
					@Override
					public void run()
					{
						context.completeTransition(true);
						containerView.clearDisappearingChildren();
					}
				});
			}

			@Override
			public void onAnimationCancel(Animator animation)
			{
				new Handler(Looper.getMainLooper()).post(new Runnable()
				{
					@Override
					public void run()
					{
						context.completeTransition(false);
					}
				});
			}

			@Override
			public void onAnimationRepeat(Animator animation)
			{
			}
		});

		set.start();
	} // animateTransition

	@Override
	public void onAnimationEnded(boolean transitionCompleted)
	{
	}

	@Override
	public long getTransitionDuration(TransitionContext transitionContext)
	{
		return duration;
	}
} // SlideTransitionAnimator
