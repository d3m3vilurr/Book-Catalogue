/*
 * @copyright 2010 Evan Leybourn
 * @license GNU General Public License
 * 
 * This file is part of Book Catalogue.
 *
 * Book Catalogue is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Book Catalogue is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Book Catalogue.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.eleybourn.bookcatalogue;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * 
 * This is the Search Source page. It contains a list of all fields and a 
 * checkbox to enable or disable the search source on the main edit book screen.
 * 
 * @author Evan Leybourn
 */
public class SearchSource extends Activity {
	private CatalogueDBAdapter mDbHelper = new CatalogueDBAdapter(this);
	public final static String prefix = "search_source_";
	
	/**
	 * Called when the activity is first created. 
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		try {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.search_source);
			mDbHelper.open();
			setupSearchSources();
		} catch (Exception e) {
			Logger.logError(e);
		}
	}
	
	/**
	 * This function builds the manage field visibility by adding onClick events
	 * to each field checkbox
	 */
	public void setupSearchSources() {
		ArrayList<String> sources = mDbHelper.fetchAllSearchSourcesArray();
		SharedPreferences mPrefs = getSharedPreferences("bookCatalogue", MODE_PRIVATE);
		
		// Display the list of fields
		LinearLayout parent = (LinearLayout) findViewById(R.id.manage_search_sources_scrollview);
		for (int i = 0; i<sources.size(); i++) {
			String key = sources.get(i);
			final String prefs_name = prefix + key;
			//Create the LinearLayout to hold each row
			LinearLayout ll = new LinearLayout(this);
			ll.setPadding(5, 0, 0, 0);

			OnClickListener listener = new OnClickListener() {
				@Override
				public void onClick(View v) {
					SharedPreferences mPrefs = getSharedPreferences("bookCatalogue", MODE_PRIVATE);
					SharedPreferences.Editor ed = mPrefs.edit();
					boolean enabled = mPrefs.getBoolean(prefs_name, true);
					ed.putBoolean(prefs_name, !enabled);
					ed.commit();
					return;
				}
			};

			//Create the checkbox
			boolean enabled = mPrefs.getBoolean(prefs_name, true);
			CheckBox cb = new CheckBox(this);
			cb.setChecked(enabled);
			cb.setOnClickListener(listener);
			ll.addView(cb);
			
			cb.setTextAppearance(this, android.R.attr.textAppearanceLarge);
			cb.setText(key);
			parent.addView(ll);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mDbHelper.close();
	} 

	public static boolean getSearchSource(SharedPreferences prefs, String key) {
		String prefs_name = prefix + key;
		return prefs.getBoolean(prefs_name, true);
	}
}