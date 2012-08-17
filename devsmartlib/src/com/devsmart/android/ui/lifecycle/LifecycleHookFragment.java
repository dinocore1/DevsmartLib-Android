package com.devsmart.android.ui.lifecycle;

import java.util.LinkedList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

public class LifecycleHookFragment extends Fragment {

	private LinkedList<LifecycleHook> mHooks = new LinkedList<LifecycleHook>();

	public void addLifecycleHook(LifecycleHook hook){
		mHooks.add(hook);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		for(LifecycleHook hook : mHooks){
			hook.onAttach(activity);
		}
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		for(LifecycleHook hook : mHooks){
			hook.onDetach();
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		for(LifecycleHook hook : mHooks){
			hook.onCreate(savedInstanceState);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		for(LifecycleHook hook : mHooks){
			hook.onDestroy();
		}
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