package io.vcdroidkit.controllers.tabs;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.vcdroidkit.controllers.ViewController;

public class ControllerPagerAdapter extends PagerAdapter
{
	private List<ViewController> items = new ArrayList<>();
	private Set<ViewController> itemsMoved = new HashSet<>();
	private ViewController primaryItem = null;

	public ControllerPagerAdapter()
	{
	}

	public void addController(ViewController controller)
	{
		items.add(controller);
		itemsMoved.add(controller);
	}

	public void addController(int position, ViewController controller)
	{
		items.add(position, controller);

		for(int i = position; i < items.size(); ++i)
			itemsMoved.add(items.get(i));
	}

	public void removeController(ViewController controller)
	{
		if(!items.contains(controller))
			return;

		int position = items.indexOf(controller);
		items.remove(controller);
		itemsMoved.remove(controller);

		for(int i = position; i < items.size(); ++i)
			itemsMoved.add(items.get(i));

		if(primaryItem == controller)
		{
			primaryItem.onViewWillDisappear(true);
			primaryItem.onViewDidDisappear(true);
			primaryItem = null;
		}
	}

	public ViewController getPrimaryItem()
	{
		return primaryItem;
	}

	public void setPrimaryItem(ViewGroup container, int position, Object object)
	{
		if(primaryItem == object)
			return;
		if(primaryItem != null)
		{
			primaryItem.onViewWillDisappear(true);
			primaryItem.onViewDidDisappear(true);
		}

		primaryItem = (ViewController) object;

		if(primaryItem != null)
		{
			primaryItem.onViewWillAppear(true);
			primaryItem.onViewDidAppear(true);
			primaryItem.refreshToolbar();
		}
	}

	@Override
	public void startUpdate(ViewGroup container)
	{
	}

	@Override
	public void finishUpdate(ViewGroup container)
	{
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position)
	{
		ViewController controller = items.get(position);

		ViewGroup.LayoutParams params =
				new ViewGroup.LayoutParams(
						ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.MATCH_PARENT);

		container.addView(controller.getView(), params);

		return controller;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object)
	{
		ViewController controller = items.get(position);
		container.removeView(controller.getView());
	}

	@Override
	public int getCount()
	{
		return items.size();
	}

	public List<ViewController> getItems()
	{
		return items;
	}

	public int getItemPosition(Object object)
	{
		if(!items.contains(object))
			return POSITION_NONE;

		if(!itemsMoved.contains(object))
			return POSITION_UNCHANGED;

		itemsMoved.remove(object);
		return items.indexOf(object);
	}

	@Override
	public CharSequence getPageTitle(int position)
	{
		return items.get(position).getTitle();
	}

	@Override
	public boolean isViewFromObject(View view, Object object)
	{
		return ((ViewController)object).getView() == view;
	}
} // ControllerPagerAdapter
