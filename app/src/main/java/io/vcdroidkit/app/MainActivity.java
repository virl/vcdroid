package io.vcdroidkit.app;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.util.Date;

import io.vcdroidkit.controllers.NavigationController;
import io.vcdroidkit.controllers.TabBarController;
import io.vcdroidkit.controllers.ViewController;
import io.vcdroidkit.app.controllers.FooController;

public class MainActivity extends AppCompatActivity
{
	NavigationController rootController;
	TabBarController tabBarController;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		configureController();
	}

	@Override
	protected void onDestroy()
	{
		if(rootController != null)
			rootController.destroy();

		super.onDestroy();
	}

	private void configureController()
	{
		ViewController controller;

		ViewGroup activityView = (ViewGroup) findViewById(android.R.id.content);

		RelativeLayout.LayoutParams params =
				new RelativeLayout.LayoutParams(
						ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.MATCH_PARENT);

		tabBarController = new TabBarController(this, R.layout.tablayout_main, R.layout.viewpager_main);
		tabBarController.setTitle(getString(R.string.app_name));

		controller = new FooController(this);
		controller.setTitle("1");
		tabBarController.addTab("First", controller);

		controller = new FooController(this);
		controller.setTitle("2");
		tabBarController.addTab("Second", controller);

		controller = new FooController(this);
		controller.setTitle("3");
		tabBarController.addTab("Third", controller);

		rootController = new NavigationController(
				this,
				tabBarController,
				R.layout.toolbar_main,
				android.support.v7.appcompat.R.drawable.abc_ic_ab_back_mtrl_am_alpha
				);
		rootController.setTitle("Navigation");

		activityView.addView(rootController.getView(), params);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		return false;

		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.menu_main, menu);
		//return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings)
		{
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed()
	{
		if(rootController.onBackPressed())
			return;

		super.onBackPressed();
	}
}
