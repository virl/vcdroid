package io.vcdroidkit.transitions;

import android.support.annotation.Nullable;

import io.vcdroidkit.controllers.ViewController;

public interface TransitionListener
{
	/**
	 * Asks your listener for the transition animator object to use when presenting a view controller.
	 * @param presentedController The view controller object that is about to be presented onscreen.
	 * @param presentingController The view controller that is presenting the view controller
	 * in the presented parameter.
	 * @param source The view controller whose presentController() method was called.
	 * @return The animator object to use when presenting the view controller or null
	 * if you do not want to present the view controller using a custom transition.
	 * The object you return should be capable of performing a fixed-length animation that is not interactive.
	 */
	@Nullable
	TransitionAnimator getAnimatorForPresentation(
			ViewController presentedController,
			ViewController presentingController,
			ViewController source
			);

	/**
	 * Asks your listener for the transition animator object to use when dismissing a view controller.
	 * @param dismissed The view controller object that is about to be dismissed.
	 * @return The animator object to use when dismissing the view controller
	 * or null if you do not want to dismiss the view controller using a custom transition.
	 * The object you return should be capable of performing a fixed-length animation that is not interactive.
	 */
	@Nullable
	TransitionAnimator getAnimatorForDismissal(ViewController dismissed);

} // TransitionListener
