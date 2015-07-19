package io.vcdroidkit.transitions;

import android.view.ViewGroup;
import android.widget.RelativeLayout;

public class DumbTransitionAnimator implements TransitionAnimator
{
	@Override
	public void animateTransition(TransitionContext transitionContext)
	{
		ViewGroup containerView = transitionContext.getContainerView();

		RelativeLayout.LayoutParams params =
				new RelativeLayout.LayoutParams(
						ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.MATCH_PARENT);

		containerView.addView(
				transitionContext.getToView(),
				params
		);

		transitionContext.completeTransition(true);
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
