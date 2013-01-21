package com.devsmart.android.ui.lifecycle;

import java.util.LinkedList;
import java.util.Queue;


import android.app.Activity;
import android.os.Bundle;

public class FragmentStateManager extends LifecycleHook {

	public static enum FragmentState {
		PREATTACHED,
		ATTACHED,
		CREATED,
		ACTIVE,
		PAUSED,
		DETACHED,
		DESTROYED
	}
	
	private FragmentState mState = FragmentState.PREATTACHED;
	private Queue<Runnable> mRunWhenActiveQueue = new LinkedList<Runnable>();
	
	public FragmentState getFragmentState() {
		return mState;
	}
	
	@Override
	public void onAttach(Activity activity) {
		mState = FragmentState.ATTACHED;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		mState = FragmentState.CREATED;
	}
	
	@Override
	public void onResume() {
		mState = FragmentState.ACTIVE;
		while(!mRunWhenActiveQueue.isEmpty()){
			mRunWhenActiveQueue.poll().run();
		}
	}
	
	@Override
	public void onPause() {
		mState = FragmentState.PAUSED;
	}
	
	@Override
	public void onDetach() {
		mState = FragmentState.DETACHED;
	}
	
	@Override
	public void onDestroy() {
		mState = FragmentState.DESTROYED;
	}

	public void runWhenActive(Runnable r) {
		mRunWhenActiveQueue.offer(r);
		if(mState == FragmentState.ACTIVE){
			while(!mRunWhenActiveQueue.isEmpty()){
				mRunWhenActiveQueue.poll().run();
			}
		}
	}
	
	
	
}