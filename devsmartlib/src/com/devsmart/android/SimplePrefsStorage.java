package com.devsmart.android;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

public class SimplePrefsStorage {
	
	public static final String PREFS_NAME = "simpleprefs";
	private static final String KEY_NAME = "jsonprefs";
	
	private SharedPreferences mPrefs;
	
	public SimplePrefsStorage(Context context) {
		mPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
	}
	
	public void clear() {
		Editor e = mPrefs.edit();
		e.clear();
		e.commit();
	}
	
	
	public JSONObject getData() {
		JSONObject retval = new JSONObject();
		String jsonstr = mPrefs.getString(KEY_NAME, null);
		if(jsonstr != null){
			try {
				retval = new JSONObject(jsonstr);
			} catch (JSONException e) {
				Log.e(SimplePrefsStorage.class.getName(), "", e);
			}
		}
		return retval;
	}
	
	public void save(JSONObject data) {
		Editor e = mPrefs.edit();
		e.putString(KEY_NAME, data.toString());
		e.commit();
	}

}
