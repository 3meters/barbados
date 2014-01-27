/*
 * Copyright 2013 Chris Banes
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aircandi.barbados.ui.components;

import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.aircandi.Aircandi;
import com.aircandi.R;
import com.aircandi.barbados.Constants;
import com.aircandi.barbados.queries.ActivitiesQuery;
import com.aircandi.barbados.ui.ShortcutFragment;
import com.aircandi.monitors.EntitiesMonitor;
import com.aircandi.monitors.ShortcutsMonitor;
import com.aircandi.queries.ShortcutsQuery;
import com.aircandi.ui.ActivityFragment;
import com.aircandi.ui.base.BaseFragment;
import com.aircandi.utilities.Integers;

public class FragmentTabPager extends com.aircandi.ui.components.FragmentTabPager  {

	public FragmentTabPager(SherlockFragmentActivity activity, ViewPager pager) {
		super(activity, pager);
	}

	@Override
	public Fragment getItem(int position) {
		/*
		 * Parent adapter works with fragment manager to attach/detach
		 * if a fragment already exists. This only gets called the first
		 * time a fragment is needed.
		 */
		TabInfo info = mTabs.get(position);
		Fragment fragment = Fragment.instantiate(mContext, info.clss.getName(), info.extras);		
		/*
		 * Fragment menu items are in addition to any menu items added by the parent activity.
		 */
		if (info.extras.getString(Constants.EXTRA_FRAGMENT_TYPE).equals(Constants.FRAGMENT_TYPE_RADAR)) {
			((BaseFragment)fragment).getMenuResIds().add(R.menu.menu_beacons);
			((BaseFragment)fragment).getMenuResIds().add(R.menu.menu_refresh_special);						
			((BaseFragment)fragment).getMenuResIds().add(R.menu.menu_help);
		}
		else if (info.extras.getString(Constants.EXTRA_FRAGMENT_TYPE).equals(Constants.FRAGMENT_TYPE_WATCH)) {

			ShortcutsMonitor monitor = new ShortcutsMonitor();
			ShortcutsQuery query = new ShortcutsQuery().setEntityId(Aircandi.getInstance().getCurrentUser().id);

			((ShortcutFragment) fragment)
					.setQuery(query)
					.setMonitor(monitor)
					.setShortcutType(Constants.TYPE_LINK_WATCH)
					.setEmptyMessageResId(R.string.label_watching_empty);

			((BaseFragment) fragment).getMenuResIds().add(R.menu.menu_refresh);
		}
		else if (info.extras.getString(Constants.EXTRA_FRAGMENT_TYPE).equals(Constants.FRAGMENT_TYPE_CREATE)) {

			ShortcutsMonitor monitor = new ShortcutsMonitor();
			ShortcutsQuery query = new ShortcutsQuery().setEntityId(Aircandi.getInstance().getCurrentUser().id);

			((ShortcutFragment) fragment)
					.setQuery(query)
					.setMonitor(monitor)
					.setShortcutType(Constants.TYPE_LINK_CREATE)
					.setEmptyMessageResId(R.string.label_created_empty);

			((BaseFragment) fragment).getMenuResIds().add(R.menu.menu_refresh);
		}
		else if (info.extras.getString(Constants.EXTRA_FRAGMENT_TYPE).equals(Constants.FRAGMENT_TYPE_ACTIVITY)) {
			
			EntitiesMonitor monitor = new EntitiesMonitor(Aircandi.getInstance().getCurrentUser().id);
			ActivitiesQuery query = new ActivitiesQuery()
					.setEntityId(Aircandi.getInstance().getCurrentUser().id)
					.setPageSize(Integers.getInteger(R.integer.page_size_activities));

			((ActivityFragment) fragment)
					.setMonitor(monitor)
					.setQuery(query)
					.setActivityStream(true)
					.setSelfBindingEnabled(true);

			((BaseFragment)fragment).getMenuResIds().add(R.menu.menu_refresh);
		}

		return fragment;
	}
}