package com.devsmart.android.ui.lifecycle;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public abstract class LifecycleHook {

	public void onAttach(Activity activity) {}
	
	public void onDetach() {}
	
	public void onCreate(Bundle savedInstanceState) {}
	
	public void onDestroy() {}
	
	public void onPause() {}
	
	public void onResume() {}
	
	public void onSaveInstanceState(Bundle outState) {}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {}
	
}