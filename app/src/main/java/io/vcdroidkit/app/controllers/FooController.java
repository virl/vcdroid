package io.vcdroidkit.app.controllers;

import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Random;

import io.vcdroidkit.controllers.ImagePickerController;
import io.vcdroidkit.controllers.NavigationController;
import io.vcdroidkit.controllers.RootView;
import io.vcdroidkit.controllers.ViewController;
import io.vcdroidkit.app.R;

public class FooController extends ViewController
{
	public FooController(AppCompatActivity activity)
	{
		super(activity);
	}

	@Override
	protected void onCreate()
	{
		super.onCreate();
	}

	@Override
	protected View onCreateView(LayoutInflater inflater, RootView rootView)
	{
		RelativeLayout view = new RelativeLayout(getActivity());
		view.setId(new Random().nextInt());
		view.setBackgroundColor(new Random().nextInt(0x01000000) + 0xFF000000);

		Button button = new Button(getActivity());
		button.setText("Controller " + this.getTitle());

		button.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				image();
			}
		});

		RelativeLayout.LayoutParams params =
				new RelativeLayout.LayoutParams(
						ViewGroup.LayoutParams.WRAP_CONTENT,
						ViewGroup.LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.CENTER_IN_PARENT, view.getId());
		view.addView(button, params);

		return view;
	}

	@Override
	public void updateMenu(Menu menu)
	{
		super.updateMenu(menu);

		MenuItem item;

		item = menu.add("Modal");
		MenuItemCompat.setShowAsAction(item, MenuItem.SHOW_AS_ACTION_ALWAYS);
		item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
		{
			@Override
			public boolean onMenuItemClick(MenuItem item)
			{
				modal();
				return true;
			}
		});

		item = menu.add("Forward");
		MenuItemCompat.setShowAsAction(item, MenuItem.SHOW_AS_ACTION_ALWAYS);
		item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
		{
			@Override
			public boolean onMenuItemClick(MenuItem item)
			{
				forward();
				return true;
			}
		});
	}

	private void modal()
	{
		/*FooController controller = new FooController(getActivity());
		controller.setTitle("Modal");
		this.getRootController().presentController(controller, true, null);*/

		FooController controller = new FooController(getActivity());
		controller.setTitle("1");

		NavigationController navigationController =
				new NavigationController(
						getActivity(),
						controller,
						R.layout.toolbar_main,
						android.support.v7.appcompat.R.drawable.abc_ic_ab_back_mtrl_am_alpha
						);
		navigationController.setTitle("Navigation");

		presentController(navigationController, true, null);
	}

	private void forward()
	{
		int number = 1;

		try
		{
			number = Integer.parseInt(this.getTitle());
		}
		catch (NumberFormatException ex)
		{
		}

		++number;

		FooController controller = new FooController(getActivity());
		controller.setTitle(Integer.toString(number));

		getNavigationController().pushController(controller, true);
	}

	private void image()
	{
		ImagePickerController controller = new ImagePickerController(
				getActivity(),
				new ImagePickerController.Listener()
				{
					@Override
					public void onPickedImage(ImagePickerController.ImageInfo imageInfo)
					{

					}

					@Override
					public void onCancel()
					{

					}
				});

		presentController(controller, false, null);
	}
} // FooController
