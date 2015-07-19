package io.vcdroidkit.transitions;

import android.view.View;
import android.view.ViewGroup;

import io.vcdroidkit.controllers.ViewController;

public interface TransitionContext
{
	/**
	 * Notifies the animation system that the transition animation is done.
	 * @param didComplete true if the transition to the presented view controller completed
	 * successfully or false if the original view controller is still being displayed.
	 */
	void completeTransition(boolean didComplete);

	/**
	 * The view that acts as the superview for the views involved in the transition.
	 * @return The view that contains both views involved in the transition.
	 */
	ViewGroup getContainerView();

	/**
	 * Identifies the view controller that is visible at the beginning of the transition
	 * (and at the end of a canceled transition).
	 * This view controller is typically the one presenting the "to” view controller
	 * or is the one being replaced by the "to” view controller.
	 * @return ViewController
	 */
	ViewController getFromController();

	/**
	 * Identifies the view controller that is visible at the end of a completed transition. This view controller is the one being presented.
	 * @return ViewController
	 */
	ViewController getToController();

	/**
	 * Identifies the view that is shown at the beginning of the transition
	 * (and at the end of a canceled transition).
	 * This view is typically the presenting view controller’s view.
	 * @return View
	 */
	View getFromView();

	/**
	 * Identifies the view that is shown at the end of a completed transition.
	 * This view is typically the presented view controller’s view but may also be an ancestor of that view.
	 * @return View
	 */
	View getToView();

	/**
	 * @return A boolean value indicating, in the case of a modal presentation style,
	 * whether the transition should be animated.
	 */
	boolean isAnimated();
}
