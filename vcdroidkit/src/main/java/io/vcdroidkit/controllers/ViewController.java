package io.vcdroidkit.controllers;

import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.internal.view.menu.MenuBuilder;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

import io.vcdroidkit.transitions.ModalTransitionAnimator;
import io.vcdroidkit.transitions.TransitionAnimator;
import io.vcdroidkit.transitions.TransitionContextImpl;
import io.vcdroidkit.transitions.TransitionListener;

public class ViewController
{
	private enum State
	{
		NONE,
		CREATED,
		DESTROYED
	}

	private AppCompatActivity activity;
	private RootView view;
	private MenuBuilder menu;

	private String title = "";

	private State state = State.NONE;
	private ViewController parentController;
	private ViewController presentingController;
	private ViewController presentedController;
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
		refreshToolbar();
	}

	protected boolean isCreated()
	{
		return state == State.CREATED;
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

	public void refreshToolbar()
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
	}

	protected void onDestroy()
	{
		if(presentedController != null)
		{
			presentedController.destroy();
		}
	}

	public void onViewWillAppear(boolean animated)
	{
	}

	public void onViewDidAppear(boolean animated)
	{
	}

	public void onViewWillDisappear(boolean animated)
	{
	}

	public void onViewDidDisappear(boolean animated)
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
			dismissController(true, null);
			return true;
		}

		return false;
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

	private ViewController getRootPresentingController()
	{
		ViewController controller = getRootParentController();
		while(true)
		{
			if(controller.getPresentingController() == null)
				break;

			controller = getPresentingController();
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
			destroy();
		else
		{
			refreshToolbar();
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
						callback
				);

		presenting.getView().disableInteraction();
		presented.getView().disableInteraction();
		animator.animateTransition(transitionContext);
	}

	public void dismissController(final boolean animated, @Nullable final Runnable completion)
	{
		if(getPresentingController() == null)
			return;

		final ViewController presenting = this.presentingController;
		final ViewController presented = this;
		final ViewGroup contentView = (ViewGroup) presented.getView().getParent();

		this.onViewWillDisappear(animated);
		presenting.onViewWillAppear(animated);

		this.presentingController = null;
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
						callback
				);

		presenting.getView().disableInteraction();
		presented.getView().disableInteraction();
		animator.animateTransition(transitionContext);
	} // dismissController

} // ViewController
