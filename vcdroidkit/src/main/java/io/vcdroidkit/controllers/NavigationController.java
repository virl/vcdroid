package io.vcdroidkit.controllers;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.annotation.LayoutRes;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.util.Stack;

import io.vcdroidkit.transitions.SlideTransitionAnimator;
import io.vcdroidkit.transitions.TransitionAnimator;
import io.vcdroidkit.transitions.TransitionContextImpl;

public class NavigationController extends ViewController
{
	public enum Operation
	{
		NONE,
		PUSH,
		POP
	}

	public interface Listener
	{
		TransitionAnimator getNavigationControllerAnimator(
				NavigationController navigationController,
				Operation operation,
		        ViewController fromController,
		        ViewController toController
		);
	}

	private Listener listener;
	private int toolbarId;
	private int buttonUpId;

	private Stack<ViewController> controllers = new Stack<>();

	private Toolbar toolbar;
	private RelativeLayout contentView;

	public NavigationController(
			AppCompatActivity activity,
			ViewController rootController,
	        @LayoutRes int toolbarId,
	        @DrawableRes int buttonUpId
	)
	{
		super(activity);
		this.toolbarId = toolbarId;
		this.buttonUpId = buttonUpId;

		addChildController(rootController);
		rootController.didMoveToParentController(this);
	}

	public Listener getListener()
	{
		return listener;
	}

	public void setListener(Listener listener)
	{
		this.listener = listener;
	}

	@Override
	public String getTitle()
	{
		return current().getTitle();
	}

	@Override
	protected View onCreateView(LayoutInflater inflater, RootView rootView)
	{
		RelativeLayout view = new RelativeLayout(getActivity());

		toolbar = (Toolbar) inflater.inflate(toolbarId, rootView, false);
		toolbar.setId(toolbarId);

		view.addView(
				toolbar,
				new ViewGroup.LayoutParams(
						ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT)
		);

		contentView = new RelativeLayout(getActivity());

		RelativeLayout.LayoutParams params =
				new RelativeLayout.LayoutParams(
						ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.MATCH_PARENT);
		params.addRule(RelativeLayout.BELOW, toolbarId);
		view.addView(contentView, params);

		return view;
	}

	@Override
	protected void onCreate()
	{
		super.onCreate();

		contentView.addView(
				current().getView(),
				new ViewGroup.LayoutParams(
						ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.MATCH_PARENT)
		);

		refreshToolbar();

		getToolbar().setNavigationOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				onBackPressed();
			}
		});
	} // onCreate

	@Override
	protected void onDestroy()
	{
		for (ViewController controller : controllers)
		{
			controller.destroy();
		}

		super.onDestroy();
	}

	private ViewController current()
	{
		if(controllers.isEmpty())
			return null;

		return controllers.peek();
	}

	public Toolbar getToolbar()
	{
		getView();
		return toolbar;
	}

	public ViewController getCurrentController()
	{
		return current();
	}

	@Override
	public void refreshToolbar()
	{
		if(!isCreated())
			return;

		//Title
		getToolbar().setTitle(this.getTitle());

		// Menu
		getToolbar().getMenu().clear();
		current().updateMenu(getToolbar().getMenu());

		// UP button

		//getActivity().setSupportActionBar(toolbar);
		//getActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		if((controllers.size() <= 1))
		{
			getToolbar().setNavigationIcon(null);
		}
		else
		{
			//int buttonBackId = android.support.v7.appcompat.R.drawable.abc_ic_ab_back_mtrl_am_alpha;
			//int buttonBackId = getActivity().getResources().getIdentifier(
			//		"abc_ic_ab_back_mtrl_am_alpha", "drawable", "android.support.v7.appcompat");

			if(buttonUpId == 0)
				return;

			Drawable buttonBackIcon;

			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				buttonBackIcon = getActivity().getResources().getDrawable(buttonUpId, getActivity().getTheme());
			} else {
				buttonBackIcon = getActivity().getResources().getDrawable(buttonUpId);
			}

			getToolbar().setNavigationIcon(buttonUpId);
		}
	} // refreshToolbar

	private TransitionAnimator getTransitionAnimator(
			Operation operation,
	        ViewController fromController,
	        ViewController toController
	)
	{
		if(listener == null)
			return new SlideTransitionAnimator(300, operation == Operation.PUSH);

		return listener.getNavigationControllerAnimator(
				this,
				operation,
				fromController,
				toController
		);
	}

	public void pushController(
			final ViewController controller,
			final boolean animated)
	{
		final ViewController previous = current();
		addChildController(controller);

		previous.onViewWillDisappear(animated);
		current().onViewWillAppear(animated);

		final TransitionAnimator animator = getTransitionAnimator(Operation.PUSH, previous, current());

		TransitionContextImpl.Callback callback = new TransitionContextImpl.Callback()
		{
			@Override
			public void complete(TransitionContextImpl transitionContext)
			{
				getView().enableInteraction();
				animator.onAnimationEnded(transitionContext.isCompleted());

				contentView.removeView(previous.getView());
				previous.onViewDidDisappear(animated);

				controller.didMoveToParentController(NavigationController.this);
				refreshToolbar();
				controller.onViewDidAppear(animated);
			}
		};

		TransitionContextImpl transitionContext =
				new TransitionContextImpl(
						this.contentView,
						previous,
						current(),
						previous.getView(),
						current().getView(),
						animated,
						callback
				);

		getView().disableInteraction();
		animator.animateTransition(transitionContext);
	} // pushController

	public boolean popController(final boolean animated)
	{
		if(controllers.size() <= 1)
			return false;

		final ViewController previous = current();
		previous.willMoveToParentController(null);
		previous.onViewWillDisappear(animated);
		controllers.remove(previous);

		current().onViewWillAppear(animated);

		final TransitionAnimator animator = getTransitionAnimator(Operation.POP, previous, current());

		TransitionContextImpl.Callback callback = new TransitionContextImpl.Callback()
		{
			@Override
			public void complete(TransitionContextImpl transitionContext)
			{
				getView().enableInteraction();
				animator.onAnimationEnded(transitionContext.isCompleted());

				contentView.removeView(previous.getView());
				previous.onViewDidDisappear(animated);
				previous.removeFromParentController();
				previous.destroy();

				refreshToolbar();
				current().onViewDidAppear(animated);
			}
		};

		final TransitionContextImpl transitionContext =
				new TransitionContextImpl(
						this.contentView,
						previous,
						current(),
						previous.getView(),
						current().getView(),
						animated,
						callback
				);

		getView().disableInteraction();
		animator.animateTransition(transitionContext);

		return true;
	} // popController

	@Override
	public void addChildController(ViewController controller)
	{
		super.addChildController(controller);
		controllers.push(controller);
	}

	@Override
	public boolean onBackPressed()
	{
		// NOT calling super() here.
		if(!getView().isInteractionEnabled())
			return true;

		if(getPresentedController() != null)
		{
			return getPresentedController().onBackPressed();
		}

		if(current().onBackPressed())
			return true;

		if(controllers.size() > 1)
		{
			popController(true);
			return true;
		}

		if(getPresentingController() != null)
		{
			dismissController(true, null);
			return true;
		}

		return false;
	}
} // NavigationController
