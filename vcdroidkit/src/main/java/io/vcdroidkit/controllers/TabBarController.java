package io.vcdroidkit.controllers;

import android.support.annotation.LayoutRes;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

import io.vcdroidkit.controllers.tabs.ControllerPagerAdapter;
import io.vcdroidkit.controllers.tabs.TabBarItem;

public class TabBarController extends ViewController
{
	private int tabLayoutId;
	private int viewPagerId;

	private TabLayout tabLayout;
	private ViewPager viewPager;

	private List<TabBarItem> items = new ArrayList<>();
	private List<ViewController> controllers = new ArrayList<>();

	private ControllerPagerAdapter pagerAdapter;

	public TabBarController(
			AppCompatActivity activity,
			@LayoutRes int tabLayoutId,
			@LayoutRes int viewPagerId
	)
	{
		super(activity);

		this.tabLayoutId = tabLayoutId;
		this.viewPagerId = viewPagerId;

		this.pagerAdapter = new ControllerPagerAdapter();
	}

	public TabLayout getTabLayout()
	{
		getView();
		return tabLayout;
	}

	public ViewPager getViewPager()
	{
		getView();
		return viewPager;
	}

	public List<TabBarItem> getItems()
	{
		return items;
	}

	@Override
	public List<ViewController> getControllers()
	{
		return controllers;
	}

	@Override
	public void updateMenu(Menu menu)
	{
		if(pagerAdapter.getPrimaryItem() == null)
			return;

		pagerAdapter.getPrimaryItem().updateMenu(menu);
	}

	@Override
	protected View onCreateView(LayoutInflater inflater, RootView rootView)
	{
		RelativeLayout view = new RelativeLayout(getActivity());

		tabLayout = (TabLayout) inflater.inflate(tabLayoutId, rootView, false);
		tabLayout.setId(tabLayoutId);
		//tabLayout.setBackgroundColor(0xFF0000AA);

		view.addView(
				tabLayout,
				new ViewGroup.LayoutParams(
						ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT)
		);

		viewPager = (ViewPager) inflater.inflate(viewPagerId, rootView, false);
		//viewPager.setBackgroundColor(0xFF00AA00);

		RelativeLayout.LayoutParams params =
				new RelativeLayout.LayoutParams(
						ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.MATCH_PARENT);
		params.addRule(RelativeLayout.BELOW, tabLayoutId);
		view.addView(viewPager, params);

		return view;
	}

	@Override
	protected void onCreate()
	{
		super.onCreate();

		viewPager.setAdapter(pagerAdapter);
		viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
		tabLayout.setOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager));

		refreshToolbar();
	} // onCreate

	@Override
	protected void onDestroy()
	{
		for(ViewController controller : controllers)
		{
			controller.destroy();
		}

		super.onDestroy();
	}

	@Override
	public void onLowMemory()
	{
		for(ViewController controller : controllers)
		{
			controller.onLowMemory();
		}

		super.onLowMemory();
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

		if(pagerAdapter.getPrimaryItem() != null
				&& pagerAdapter.getPrimaryItem().onBackPressed())
			return true;

		if(getPresentingController() != null)
		{
			dismissController(null, true, null);
			return true;
		}

		return false;
	}

	public TabBarItem getTabBarItem(ViewController controller)
	{
		if(!controllers.contains(controller))
			return null;

		return getTabBarItem(controllers.indexOf(controller));
	}

	public TabBarItem getTabBarItem(int position)
	{
		return items.get(position);
	}

	public void addTab(String title, ViewController controller)
	{
		addTab(new TabBarItem(title), controller);
	}

	public void addTab(TabBarItem item, ViewController controller)
	{
		addChildController(controller);
		TabLayout.Tab tab = getTabLayout().newTab();
		tab.setText(item.getTitle());
		getTabLayout().addTab(tab);

		pagerAdapter.addController(controller);
		items.add(item);

		controller.didMoveToParentController(this);
		pagerAdapter.notifyDataSetChanged();

		refreshToolbar();
	}

	public void removeTab(int position)
	{
		ViewController controller = controllers.get(position);

		controller.willMoveToParentController(null);
		items.remove(position);
		pagerAdapter.removeController(controller);
		controllers.remove(controller);
		controller.removeFromParentController();

		getTabLayout().removeTabAt(position);
		pagerAdapter.notifyDataSetChanged();

		refreshToolbar();
	}

} // TabBarController
