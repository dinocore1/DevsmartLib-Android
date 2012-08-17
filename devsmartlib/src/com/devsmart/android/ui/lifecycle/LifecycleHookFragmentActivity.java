package com.devsmart.android.ui.lifecycle;

import java.util.LinkedList;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class LifecycleHookFragmentActivity extends FragmentActivity {
	
	private LinkedList<LifecycleHook> mHooks = new LinkedList<LifecycleHook>();

	public void addLifecycleHook(LifecycleHook hook){
		mHooks.add(hook);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		for(LifecycleHook hook : mHooks){
			hook.onAttach(this);
		}
		
		for(LifecycleHook hook : mHooks){
			hook.onCreate(savedInstanceState);
		}
	}

	@Override
	public void onDestroy() {
		
		for(LifecycleHook hook : mHooks){
			hook.onDetach();
		}
		
		for(LifecycleHook hook : mHooks){
			hook.onDestroy();
		}
		
		super.onDestroy();
	}

	@Override
	public void onPause() {
		super.onPause();
		for(LifecycleHook hook : mHooks){
			hook.onPause();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		for(LifecycleHook hook : mHooks){
			hook.onResume();
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle onState){
		super.onSaveInstanceState(onState);
		for(LifecycleHook hook : mHooks){
			hook.onSaveInstanceState(onState);
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		for(LifecycleHook hook : mHooks){
			hook.onActivityResult(requestCode, resultCode, data);
		}
	}

}