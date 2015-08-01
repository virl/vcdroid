package io.vcdroidkit.transitions;

import android.view.ViewGroup;
import android.widget.RelativeLayout;

public class DumbTransitionAnimator implements TransitionAnimator
{
	@Override
	public void animateTransition(TransitionContext context)
	{
		ViewGroup containerView = context.getContainerView();

		RelativeLayout.LayoutParams params =
				new RelativeLayout.LayoutParams(
						ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.MATCH_PARENT);

		if(context.shouldAddToView())
		{
			containerView.addView(
					context.getToView(),
					params
			);
		}

		context.completeTransition(true);
	}

	@Override
	public void onAnimationEnded(boolean transitionCompleted)
	{
	}

	@Override
	public long getTransitionDuration(TransitionContext transitionContext)
	{
		return 0;
	}
} // DumbTransitionAnimator
