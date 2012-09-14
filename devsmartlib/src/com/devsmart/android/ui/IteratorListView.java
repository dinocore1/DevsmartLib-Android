package com.devsmart.android.ui;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Queue;

import android.content.Context;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

public class IteratorListView extends ViewGroup {
	
	public static abstract class ViewAdapter<T extends Object> {

		abstract public View getView(T obj, View poll, IteratorListView iteratorListView);
		
		public void onTap(View view, int x, int y) {}
		public void onLongPress(View view, int x, int y) {}
		public void onDoubleTap(View view, int x, int y) {}
		
	}
	
	private Queue<View> mRemovedViewQueue = new LinkedList<View>();
	private Scroller mScroller;
	private GestureDetector mScrollGestureDetector;
	//number of pixes to scroll on the next onLayout
	private int mdY = 0;
	private int mLastY = 0;
	private int mYOffset = 0;
	private boolean mIsFingerDown;
	private int mTopItem = 0;
	private int mBottomItem = 0;
	private ListIterator<?> mIterator;
	private ViewAdapter<Object> mAdapter;

	public IteratorListView(Context context) {
		super(context);
		init();
	}

	public IteratorListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	private void init() {
		mScroller = new Scroller(getContext());
		mScrollGestureDetector = new GestureDetector(getContext(), mOnGesture);
	}
	
	public <T> void setIteratorAdapter(ListIterator<T> iterator, ViewAdapter<T> adapter) {
		mIterator = iterator;
		mAdapter = (ViewAdapter<Object>) adapter;
		
		//seek to the top of the list
		while(mIterator.hasPrevious()){
			mIterator.previous();
		}
		mTopItem = 0;
		mBottomItem = -1;
		removeAllViews();
	}
	
	private GestureDetector.SimpleOnGestureListener mOnGesture = new GestureDetector.SimpleOnGestureListener() {

		@Override
		public boolean onDown(MotionEvent e) {
			mIsFingerDown = true;
			mScroller.forceFinished(true);
			return true;
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

			final int maxScrollDistance = getMeasuredHeight()*1;
			mLastY = 0;
			mScroller.fling(0, mdY, 0, Math.round(velocityY), 0, 0, -maxScrollDistance, maxScrollDistance);
			requestLayout();
			
			
			return true;
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			mdY -= Math.round(distanceY);
			requestLayout();
			return true;

		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			if(mAdapter != null){
				RectF r = new RectF();
				for(int i=0;i<getChildCount();i++){
					View child = getChildAt(i);
					r.set(child.getLeft(), child.getTop(), child.getRight(), child.getBottom());
					if(r.contains(e.getX(), e.getY())){
						mAdapter.onTap(child, Math.round(e.getX() - child.getLeft()), Math.round(e.getY() - child.getTop()));
					}
				}
			}
			
			return super.onSingleTapConfirmed(e);
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			return super.onSingleTapUp(e);
		}

		@Override
		public boolean onDoubleTap(MotionEvent e) {
			if(mAdapter != null){
				RectF r = new RectF();
				for(int i=0;i<getChildCount();i++){
					View child = getChildAt(i);
					r.set(child.getLeft(), child.getTop(), child.getRight(), child.getBottom());
					if(r.contains(e.getX(), e.getY())){
						mAdapter.onDoubleTap(child, Math.round(e.getX() - child.getLeft()), Math.round(e.getY() - child.getTop()));
					}
				}
			}
			return super.onDoubleTap(e);
		}

		@Override
		public void onLongPress(MotionEvent e) {
			if(mAdapter != null){
				RectF r = new RectF();
				for(int i=0;i<getChildCount();i++){
					View child = getChildAt(i);
					r.set(child.getLeft(), child.getTop(), child.getRight(), child.getBottom());
					if(r.contains(e.getX(), e.getY())){
						mAdapter.onLongPress(child, Math.round(e.getX() - child.getLeft()), Math.round(e.getY() - child.getTop()));
					}
				}
			}
			super.onLongPress(e);
		}
		
		

	};
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean retval = mScrollGestureDetector.onTouchEvent(event);
		
		if(event.getAction() == MotionEvent.ACTION_UP){
			mIsFingerDown = false;
			if(getChildCount() > 0){
				int topOfFirstChild = getChildAt(0).getTop();
				if(topOfFirstChild + mdY > 0){
					mScroller.forceFinished(true);
					mLastY = 0;
					mScroller.startScroll(0, 0, mdY, -mYOffset);
					requestLayout();
				}
			}
		}
		
		return retval;
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		for(int i = 0;i<getChildCount();i++){
			View child = getChildAt(i);
			int oldHeight = child.getMeasuredHeight();
			child.measure(MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.EXACTLY), 
					MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.UNSPECIFIED));

			int childDiff = child.getMeasuredHeight() - oldHeight;
			if(childDiff > 0 && child.getBottom() < getHeight()/2){
				mYOffset -= childDiff;
			}
		}
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {

		Log.d(IteratorListView.class.getName(), String.format("t:%d b:%d", mTopItem, mBottomItem));
		
		if(mAdapter == null){
			return;
		}

		if(mScroller.computeScrollOffset()){
			int y = mScroller.getCurrY();
			mdY += y - mLastY;
			mLastY = y;
		}

		mYOffset += mdY;

		fillDown();
		fillUp();
		removeNonVisibleItems();
		
		positionItems();

		mdY = 0;

		if(!mScroller.isFinished()){
			post(new Runnable(){
				@Override
				public void run() {
					requestLayout();
				}
			});

		} else if(mYOffset > 0 && !mIsFingerDown){
			mLastY = 0;
			mScroller.startScroll(0, 0, mdY, -mYOffset);
			post(new Runnable(){
				@Override
				public void run() {
					requestLayout();
				}
			});
		}
	}

	private void removeNonVisibleItems(){


		final int height = getHeight();
		View child = getChildAt(0);

		//remove from top
		while(child != null && child.getBottom() + mdY < 0){
			removeViewsInLayout(0, 1);
			mRemovedViewQueue.offer(child);
			mYOffset += child.getMeasuredHeight();
			child = getChildAt(0);
			mTopItem++;
		}

		//remove from bottom
		child = getChildAt(getChildCount()-1);
		while(child != null && child.getTop() + mdY > height){
			removeViewsInLayout(getChildCount()-1, 1);
			mRemovedViewQueue.offer(child);
			child = getChildAt(getChildCount()-1);
			mBottomItem--;
		}

	}
	
	private void addAndMeasureChild(View child, int index){
		LayoutParams layoutparams = child.getLayoutParams();
		if(layoutparams == null){
			layoutparams = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		}
		addViewInLayout(child, index, layoutparams, true);
		child.measure(MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.EXACTLY), 
				MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.UNSPECIFIED));
	}

	private View getView(Object obj) {
		View child = mAdapter.getView(obj, mRemovedViewQueue.poll(), this);
		return child;
	}
	
	private void seekTo(int index){
		while(mIterator.nextIndex() <= index && mIterator.hasNext()){
			mIterator.next();
		}
		
		while(mIterator.previousIndex() >= index && mIterator.hasPrevious()){
			mIterator.previous();
		}
	}
	
	private void fillDown() {

		if(mAdapter == null){
			return;
		}

		final int windowHeight = getHeight();

		int bottomOfLastChild = 0;
		if(getChildCount() > 0){
			bottomOfLastChild = getChildAt(getChildCount()-1).getBottom();
		}

		while(bottomOfLastChild + mdY < windowHeight) {
			
			seekTo(mBottomItem);
			if(mIterator.hasNext()){
				View child = getView(mIterator.next());
				addAndMeasureChild(child, -1);
				bottomOfLastChild += child.getMeasuredHeight();
				mBottomItem++;
			} else {
				break;
			}
			
			
		}
	}

	private void fillUp() {

		if(mAdapter == null){
			return;
		}

		int topOfFirstChild = 0;
		if(getChildCount() > 0){
			topOfFirstChild = getChildAt(0).getTop();
		}

		while(topOfFirstChild + mdY > 0) {
			
			seekTo(mTopItem);
			if(mIterator.hasPrevious()){
				View child = getView(mIterator.previous());
				addAndMeasureChild(child, 0);
				topOfFirstChild -= child.getMeasuredHeight();
				mYOffset -= child.getMeasuredHeight();
				mTopItem--;
			} else {
				break;
			}
			
			
		}
	}

	private void positionItems() {
		int top = mYOffset;
		for(int i=0;i<getChildCount();i++){
			View child = getChildAt(i);
			int childHeight = child.getMeasuredHeight();
			child.layout(0, top, child.getMeasuredWidth(), top + childHeight);
			top += childHeight;
		}
	}

}
