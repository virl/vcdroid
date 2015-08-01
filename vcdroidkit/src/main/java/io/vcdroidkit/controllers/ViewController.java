package io.vcdroidkit.controllers;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.internal.view.menu.MenuBuilder;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Stack;

import io.vcdroidkit.transitions.ModalTransitionAnimator;
import io.vcdroidkit.transitions.TransitionAnimator;
import io.vcdroidkit.transitions.TransitionContextImpl;
import io.vcdroidkit.transitions.TransitionListener;
import io.vcdroidkit.util.Logger;

public class ViewController
{
	private enum State
	{
		NONE,
		CREATED,
		DESTROYED
	}

	private enum ViewState
	{
		DID_DISAPPEAR,
		WILL_APPEAR,
		DID_APPEAR,
		WILL_DISAPPEAR
	}

	private AppCompatActivity activity;
	private RootView view;
	private MenuBuilder menu;
	protected Stack<ViewController> controllers = new Stack<>();

	private String title = "";

	private boolean opaque = true;
	private State state = State.NONE;
	private ViewState viewState = ViewState.DID_DISAPPEAR;

	private ViewController parentController;
	private ViewController presentingController;
	private ViewController presentedController;
	private ViewController sourceController; // who called presentController()

	private TransitionListener transitionListener;

	public ViewController(AppCompatActivity activity)
	{
		this.activity = activity;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;

		if(state == State.CREATED && this.getParentController() != null)
			invalidateToolbar();
	}

	/**
	 * @return if true, presenting controller's view will be removed from parent view
	 * after this controller is presented. Default is true.
	 */
	public boolean isOpaque()
	{
		return opaque;
	}

	/**
	 * @param opaque if true, presenting controller's view will be removed from parent view
	 * after this controller is presented.
	 */
	public void setOpaque(boolean opaque)
	{
		this.opaque = opaque;
	}

	protected boolean isCreated()
	{
		return state == State.CREATED;
	}

	public boolean isInteractionEnabled()
	{
		return getView().isInteractionEnabled();
	}

	private long lastClickTime = 0;

	public boolean shouldHandleClick()
	{
		if(!isInteractionEnabled())
			return false;

		if(new Date().getTime() - lastClickTime < 1000)
			return false;

		lastClickTime = new Date().getTime();

		return true;
	}

	public void destroy()
	{
		if(state == State.CREATED)
		{
			state = State.DESTROYED;
			onDestroy();
		}
	}

	public AppCompatActivity getActivity()
	{
		return activity;
	}

	protected ViewGroup getActivityView()
	{
		return (ViewGroup) getActivity().findViewById(android.R.id.content);
	}

	public RootView getView()
	{
		if(state == State.NONE)
		{
			this.view = new RootView(getActivity());

			View createdView = onCreateView(
					getActivity().getLayoutInflater(),
					this.view
			);

			if(createdView != null)
			{
				RelativeLayout.LayoutParams params =
						new RelativeLayout.LayoutParams(
								ViewGroup.LayoutParams.MATCH_PARENT,
								ViewGroup.LayoutParams.MATCH_PARENT);
				this.view.addView(createdView, params);
			}

			state = State.CREATED;
			onCreate();
		}

		return view;
	}

	public void setView(RootView view)
	{
		boolean existed = (this.view != null);
		this.view = view;
	}

	public void invalidateToolbar()
	{
		NavigationController navigationController = getNavigationController();
		if(navigationController == null)
			return;

		navigationController.refreshToolbar();
	}

	public void updateMenu(Menu menu)
	{
	}

	protected View onCreateView(LayoutInflater inflater, RootView rootView)
	{
		return new View(getActivity());
	}

	protected void onCreate()
	{
		Logger.log();
	}

	protected void onDestroy()
	{
		Logger.log();

		for (ViewController controller : new ArrayList<>(controllers))
		{
			controller.destroy();
		}

		if(presentedController != null)
		{
			presentedController.destroy();
		}
	}

	public void onLowMemory()
	{
		if(getPresentedController() != null)
			getPresentedController().onLowMemory();
	}

	public void onViewWillAppear(boolean animated)
	{
		Logger.log();
		if(viewState != ViewState.DID_DISAPPEAR)
			throw new IllegalStateException("viewState != DID_DISAPPEAR");

		viewState = ViewState.WILL_APPEAR;
		getView();
	}

	public void onViewDidAppear(boolean animated)
	{
		Logger.log();
		if(viewState != ViewState.WILL_APPEAR)
			throw new IllegalStateException("viewState != WILL_APPEAR");

		viewState = ViewState.DID_APPEAR;
		getView();
	}

	public void onViewWillDisappear(boolean animated)
	{
		Logger.log();
		if(viewState != ViewState.DID_APPEAR)
			throw new IllegalStateException("viewState != DID_APPEAR");

		viewState = ViewState.WILL_DISAPPEAR;
		getView();
	}

	public void onViewDidDisappear(boolean animated)
	{
		Logger.log();
		if(viewState != ViewState.WILL_DISAPPEAR)
			throw new IllegalStateException("viewState != WILL_DISAPPEAR");

		viewState = ViewState.DID_DISAPPEAR;
		getView();
	}

	public void onPresentedDismissed(Object result, boolean animated)
	{
	}

	public boolean onBackPressed()
	{
		if(!getView().isInteractionEnabled())
			return true;

		if(getPresentedController() != null)
		{
			return getPresentedController().onBackPressed();
		}

		if(getPresentingController() != null)
		{
			dismissController(null, true, null);
			return true;
		}

		return false;
	}

	public void startActivityForResult(Intent intent, int requestCode)
	{
		getActivity().startActivityForResult(intent, requestCode);
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if(getPresentedController() != null)
			getPresentedController().onActivityResult(requestCode, resultCode, data);

		for(ViewController controller : new ArrayList<>(this.controllers))
			controller.onActivityResult(requestCode, resultCode, data);
	}

	public ViewController getParentController()
	{
		return parentController;
	}

	public ViewController getRootParentController()
	{
		ViewController controller = this;
		while(true)
		{
			if(controller.getParentController() == null)
				break;

			controller = controller.getParentController();
		}

		return controller;
	}

	public ViewController getPresentingController()
	{
		return presentingController;
	}

	public ViewController getPresentedController()
	{
		return presentedController;
	}

	public ViewController getSourceController()
	{
		ViewController controller = getRootParentController();
		return controller.sourceController;
	}

	public NavigationController getNavigationController()
	{
		ViewController controller = this.parentController;
		while(controller != null)
		{
			if(controller instanceof NavigationController)
				break;

			controller = controller.getParentController();
		}

		return (NavigationController) controller;
	}

	public TabBarController getTabController()
	{
		ViewController controller = this.parentController;
		while(controller != null)
		{
			if(controller instanceof TabBarController)
				return (TabBarController) controller;

			controller = controller.getParentController();
		}

		return null;
	}

	public List<ViewController> getControllers()
	{
		return new ArrayList<>();
	}

	public void addChildController(ViewController controller)
	{
		controller.willMoveToParentController(this);
		controllers.push(controller);
	}

	public void removeFromParentController()
	{
		this.didMoveToParentController(null);
	}

	public void willMoveToParentController(@Nullable ViewController parent)
	{
	}

	public void didMoveToParentController(@Nullable ViewController parent)
	{
		this.parentController = parent;
		if(parent == null)
		{
			destroy();
		}
		else
		{
		}
	}

	public TransitionListener getTransitionListener()
	{
		return transitionListener;
	}

	public void setTransitionListener(TransitionListener transitionListener)
	{
		this.transitionListener = transitionListener;
	}

	private TransitionAnimator getTransitionAnimator(
			boolean dismissing,
			ViewController presentedController,
			ViewController presentingController,
	        ViewController source
	)
	{
		if(transitionListener == null)
			return new ModalTransitionAnimator(getActivity(), 300, dismissing);

		if(dismissing)
			return transitionListener.getAnimatorForDismissal(presentedController);

		return transitionListener.getAnimatorForPresentation(presentedController, presentingController, source);
	}

	public void presentController(final ViewController presented, final boolean animated, @Nullable final Runnable completion)
	{
		final ViewController presenting = getRootParentController();
		presenting.presentedController = presented;
		presented.presentingController = presenting;
		presented.sourceController = this;

		final ViewGroup contentView = (ViewGroup) presenting.getView().getParent();

		presenting.onViewWillDisappear(animated);
		presented.onViewWillAppear(animated);

		final TransitionAnimator animator = getTransitionAnimator(false, presented, presenting, this);

		TransitionContextImpl.Callback callback = new TransitionContextImpl.Callback()
		{
			@Override
			public void complete(TransitionContextImpl transitionContext)
			{
				presenting.getView().enableInteraction();
				presented.getView().enableInteraction();
				animator.onAnimationEnded(transitionContext.isCompleted());

				if(presented.isOpaque())
					contentView.removeView(presenting.getView());

				presenting.onViewDidDisappear(animated);

				presented.onViewDidAppear(animated);

				if(completion != null)
					completion.run();
			}
		};

		TransitionContextImpl transitionContext =
				new TransitionContextImpl(
						contentView,
						presenting,
						presented,
						presenting.getView(),
						presented.getView(),
						animated,
						true,
						callback
				);

		presenting.getView().disableInteraction();
		presented.getView().disableInteraction();
		animator.animateTransition(transitionContext);
	}

	public void dismissController(final Object result, final boolean animated, @Nullable final Runnable completion)
	{
		final ViewController presenting = getRootParentController().getPresentingController();
		final ViewController presented = getRootParentController();
		final ViewGroup contentView = (ViewGroup) presented.getView().getParent();

		if(presenting == null)
			return;

		presented.onViewWillDisappear(animated);
		presenting.onViewWillAppear(animated);

		presented.presentingController = null;
		presenting.presentedController = null;

		final TransitionAnimator animator = getTransitionAnimator(true, presented, presenting, this);

		TransitionContextImpl.Callback callback = new TransitionContextImpl.Callback()
		{
			@Override
			public void complete(TransitionContextImpl transitionContext)
			{
				presenting.getView().enableInteraction();
				presented.getView().enableInteraction();
				animator.onAnimationEnded(transitionContext.isCompleted());

				contentView.removeView(presented.getView());
				presented.onViewDidDisappear(animated);
				presented.destroy();

				presenting.onViewDidAppear(animated);
				if(completion != null)
					completion.run();

				if(presented.sourceController != null)
					presented.sourceController.onPresentedDismissed(result, animated);
			}
		};

		final TransitionContextImpl transitionContext =
				new TransitionContextImpl(
						contentView,
						presented,
						presenting,
						presented.getView(),
						presenting.getView(),
						animated,
						presented.isOpaque(),
						callback
				);

		presenting.getView().disableInteraction();
		presented.getView().disableInteraction();
		animator.animateTransition(transitionContext);
	} // dismissController

} // ViewController
