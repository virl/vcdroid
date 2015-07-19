package io.vcdroidkit.transitions;

public interface TransitionAnimator
{
	/**
	 * Tells your animator object to perform the transition animations.
	 * Library calls this method when presenting or dismissing a view controller.
	 * Use this method to configure the animations associated with your custom transition.
	 *
	 * All animations must take place in the view specified by the containerView
	 * property of transitionContext. Add the view being presented
	 * (or revealed if the transition involves dismissing a view controller)
	 * to the container view’s hierarchy and set up any animations you want
	 * to make that view move into position.
	 *
	 * You can retrieve the view controllers involved in the transition
	 * from the methods of transitionContext.
	 *
	 * @param transitionContext The context object containing information about the transition.
	 */
	void animateTransition(TransitionContext transitionContext);

	/**
	 * Tells your animator object that the transition animations have finished.
	 *
	 * Library calls this method at the end of a transition to let you know the results.
	 * Use this method to perform any final cleanup operations required by your transition animator
	 * when the transition finishes.
	 *
	 * @param transitionCompleted Contains the value true if the transition completed successfully
	 * and the new view controller is now displayed or false if the transition was canceled
	 * and the original view controller is still visible.
	 */
	void onAnimationEnded(boolean transitionCompleted);

	/**
	 * Asks your animator object for the duration (in milliseconds) of the transition animation.
	 *
	 * Library calls this method to obtain the timing information for your animations.
	 * The value you provide should be the same value that you use when configuring the animations
	 * in your animateTransition() method. Library uses the value to synchronize the actions
	 * of other objects that might be involved in the transition. For example, a navigation controller
	 * uses the value to synchronize changes to the navigation bar.

	 * When determining the value to return, assume there will be no user interaction
	 * during the transition — even if you plan to support user interactions at runtime.
	 *
	 * @param transitionContext The context object containing information to use during the transition.
	 * @return The duration, in milliseconds, of your custom transition animation.
	 */
	long getTransitionDuration(TransitionContext transitionContext);
}
