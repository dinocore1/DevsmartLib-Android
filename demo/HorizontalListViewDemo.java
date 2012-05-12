package com.devsmart.android.test;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.devsmart.android.ui.HorizontalListView;

public class HorizontalListViewDemo extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.listviewdemo);
		
		HorizontalListView listview = (HorizontalListView) findViewById(R.id.listview);
		listview.setAdapter(mAdapter);
		
	}
	
	private static String[] dataObjects = new String[]{ "Text #1",
		"Text #2",
		"Text #3" }; 
	
	private BaseAdapter mAdapter = new BaseAdapter() {

		@Override
		public int getCount() {
			return dataObjects.length;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View retval = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewitem, null);
			TextView title = (TextView) retval.findViewById(R.id.title);
			title.setText(dataObjects[position]);
			
			return retval;
		}
		
	};

}
