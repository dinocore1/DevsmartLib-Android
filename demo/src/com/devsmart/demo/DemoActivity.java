package com.devsmart.demo;



import com.devsmart.android.activity.FragmentShellActivity;
import com.devsmart.demo.fragment.DemoFragment;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;

public class DemoActivity extends PreferenceActivity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        addPreferencesFromResource(R.xml.menu);
        
        setupOnClick(findPreference("horizontallistview"), new Intent(this, HorizontalListViewDemo.class));
        setupOnClick(findPreference("equalspace"), new Intent(this, EqualSpaceLayoutDemo.class));
        setupOnClick(findPreference("equalspace2"), new Intent(this, EqualSpaceLayoutDemo2.class));
        
        findPreference("fragmentshell").setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent i = FragmentShellActivity.createIntent(getApplicationContext(), DemoFragment.class, null);
				startActivity(i);
				return true;
			}
		});
    }
    
    private void setupOnClick(Preference pref, final Intent intent) {
    	pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				startActivity(intent);
				return true;
			}
		});
    }
    

}