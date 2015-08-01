package io.vcdroidkit.transitions;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.RelativeLayout;

public class ModalTransitionAnimator implements TransitionAnimator
{
	private AppCompatActivity activity;
	private long duration;
	private boolean dismiss;

	public ModalTransitionAnimator(AppCompatActivity activity, long duration, boolean dismiss)
	{
		this.activity = activity;
		this.duration = duration;
		this.dismiss = dismiss;
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
					dismiss ? fromIndex : containerView.getChildCount(),
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

		params = new RelativeLayout.LayoutParams(
						ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.MATCH_PARENT);

		int bglight = getStatusBarBackgroundLightValue();
		bglight = 0xff000000 + (bglight << 0) + (bglight << 8) + (bglight << 16);
		final View backgroundView = new View(activity);
		backgroundView.setBackgroundColor(bglight);
		containerView.addView(
				backgroundView,
				fromIndex,
				params
		);

		AnimatorSet set = new AnimatorSet();

		float translationStart = 1;
		//float translationStart = 2 / 3f;
		float scaleMin = 0.98f;
		float scaleDuration = 0.8f;
		float alphaMin = 0.75f;
		float alphaDuration = 0.8f;

		if(!dismiss)
		{
			ObjectAnimator animator;

			toView.setTranslationY(fromView.getHeight() * translationStart);
			animator = ObjectAnimator.ofFloat(toView, "translationY", (float) fromView.getHeight(), 0);
			animator.setInterpolator(new DecelerateInterpolator());
			animator.setDuration(duration);
			AnimatorSet.Builder builder = set.play(animator);

			/*toView.setAlpha(0);
			animator = ObjectAnimator.ofFloat(toView, "alpha", 0, 1);
			animator.setInterpolator(new AccelerateDecelerateInterpolator());
			animator.setDuration((long) (duration));
			builder.with(animator);*/

			fromView.setAlpha(1);
			animator = ObjectAnimator.ofFloat(fromView, "alpha", 1, alphaMin);
			animator.setInterpolator(new AccelerateDecelerateInterpolator());
			animator.setDuration((long) (duration * alphaDuration));
			builder.with(animator);

			fromView.setScaleX(1);
			animator = ObjectAnimator.ofFloat(fromView, "scaleX", 1, scaleMin);
			animator.setInterpolator(new DecelerateInterpolator());
			animator.setDuration((long) (duration * scaleDuration));
			builder.with(animator);
			fromView.setScaleY(1);
			animator = ObjectAnimator.ofFloat(fromView, "scaleY", 1, scaleMin);
			animator.setInterpolator(new DecelerateInterpolator());
			animator.setDuration((long) (duration * scaleDuration));
			builder.with(animator);
		}
		else
		{
			ObjectAnimator animator;

			fromView.setTranslationY(0);
			animator = ObjectAnimator.ofFloat(fromView, "translationY", 0, fromView.getHeight() * translationStart);
			animator.setInterpolator(new AccelerateInterpolator());
			animator.setDuration(duration);
			AnimatorSet.Builder builder = set.play(animator);

			/*fromView.setAlpha(1);
			animator = ObjectAnimator.ofFloat(fromView, "alpha", 1, 0);
			animator.setInterpolator(new AccelerateInterpolator());
			animator.setDuration((long) (duration));
			builder.with(animator);*/

			toView.setAlpha(alphaMin);
			animator = ObjectAnimator.ofFloat(toView, "alpha", alphaMin, 1);
			animator.setInterpolator(new AccelerateDecelerateInterpolator());
			animator.setDuration((long) (duration * alphaDuration));
			builder.with(animator);

			toView.setScaleX(scaleMin);
			animator = ObjectAnimator.ofFloat(toView, "scaleX", scaleMin, 1);
			animator.setInterpolator(new LinearInterpolator());
			animator.setDuration((long) (duration * 1.0f));
			builder.with(animator);
			toView.setScaleY(scaleMin);
			animator = ObjectAnimator.ofFloat(toView, "scaleY", scaleMin, 1);
			animator.setInterpolator(new LinearInterpolator());
			animator.setDuration((long) (duration * 1.0f));
			builder.with(animator);
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
						containerView.removeView(backgroundView);
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

	private static int statusBarLightLevel = -1;

	private int getStatusBarBackgroundLightValue()
	{
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
			return 30;

		if(statusBarLightLevel >= 0)
			return statusBarLightLevel;

		Drawable bg = activity.getResources().getDrawable(android.R.drawable.status_bar_item_background);
		if(bg == null)
			return 30;

		int height = Math.max(1, bg.getIntrinsicHeight());
		int width = Math.max(1, bg.getIntrinsicWidth());
		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		bg.setBounds(0, 0, width, height);
		bg.draw(canvas);

		long sum = 0;
		for (int x=0; x<width; x++){
			for (int y=0; y<height; y++){
				int color = bitmap.getPixel(x, y);
				int r = (color >> 16) & 0xFF;
				int g = (color >> 8) & 0xFF;
				int b = (color) & 0xFF;
				int max = Math.max(r, Math.max(g, b));
				int min = Math.min(r, Math.min(g, b));
				int l = (min + max)/2;
				sum = sum + l;
			}
		}
		bitmap.recycle();
		bitmap = null;
		canvas = null;
		bg = null;
		sum = sum / (width * height);
		// should be [0..255]
		statusBarLightLevel = (int)Math.min(255, Math.max(sum, 0));

		return statusBarLightLevel;
	}
} // ModalTransitionAnimator
