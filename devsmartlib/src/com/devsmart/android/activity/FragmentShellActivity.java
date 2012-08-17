package com.devsmart.android.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Window;

public class FragmentShellActivity extends FragmentActivity implements NavigationDelegate {

	public static final String EXTRA_FRAGMENTNAME = "fragname";
	public static final String EXTRA_FRAGMENTARGS = "fragargs";
	public static final String EXTRA_ORIENTATION = "orient";
	
	private Fragment mBaseFragment;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		Intent launchIntent = getIntent();
		final String fragclassname = launchIntent.getStringExtra(EXTRA_FRAGMENTNAME);
		final Bundle fragargs = launchIntent.getBundleExtra(EXTRA_FRAGMENTARGS);
		final int orient = launchIntent.getIntExtra(EXTRA_ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
		setRequestedOrientation(orient);
		
		try {
			Class<?> fragmentClass = getClassLoader().loadClass(fragclassname);
			mBaseFragment = (Fragment) fragmentClass.newInstance();
			mBaseFragment.setArguments(fragargs);
			
			FragmentTransaction tr = getSupportFragmentManager().beginTransaction();
			tr.add(android.R.id.content, mBaseFragment);
			tr.commit();
			
		} catch(Exception e) {
			Log.e(FragmentShellActivity.class.getName(), "", e);
			finish();
		}
		
	}
	
	public static Intent createIntent(Context context, Class<?> fragmentClass, Bundle fragmentArgs){
		return createIntent(context, fragmentClass, fragmentArgs, ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
	}
	
	public static Intent createIntent(Context context, Class<?> fragmentClass, Bundle fragmentArgs, int screenOrientationPortrait) {
		
		Intent retval = new Intent(context, FragmentShellActivity.class);
		retval.putExtra(EXTRA_FRAGMENTNAME, fragmentClass.getName());
		retval.putExtra(EXTRA_FRAGMENTARGS, fragmentArgs);
		retval.putExtra(EXTRA_ORIENTATION, screenOrientationPortrait);
		
		return retval;
	}

	@Override
	public void onNavigateBack() {
		finish();
	}

	

}