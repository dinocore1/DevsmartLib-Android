/*
 * HorizontalListView.java v1.5
 *
 * 
 * The MIT License
 * Copyright (c) 2011 Paul Soucy (paul@dev-smart.com)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */

package com.devsmart.android.ui;

import java.util.LinkedList;
import java.util.Queue;

import android.R.bool;
import android.annotation.SuppressLint;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.Scroller;

public class HorizontalListView extends AbsListView {
	private ContextMenuInfo mContextMenuInfo;
	public boolean mAlwaysOverrideTouch = true;
	protected ListAdapter mAdapter;
	private int mLeftViewIndex = -1;
	private int mRightViewIndex = 0;
	protected int mCurrentX;
	protected int mNextX;
	private int mMaxX = Integer.MAX_VALUE;
	private int mDisplayOffset = 0;
	protected Scroller mScroller;
	private GestureDetector mGesture;
	private SparseArray<Queue<View>> mRemovedViewQueue = new SparseArray<Queue<View>>();
	private OnItemSelectedListener mOnItemSelected;
	private OnItemLongClickListener mOnItemLongClickListener;
	private OnItemUpdateListener mOnItemUpdateListener;
	private boolean mDataChanged = false;
	/**
	 * Acts upon click
	 */
	protected PerformClick mPerformClick;
	/**
	 * Delayed action for touch mode.
	 */
	private Runnable mTouchModeReset;

	private enum DownloadType {
		CACHE, DOWNLOAD, NONE;
	}

	public HorizontalListView(Context context) {
		this(context, null);
		initView();
	}

	public HorizontalListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
	}

	private synchronized void initView() {
		mLeftViewIndex = -1;
		mRightViewIndex = 0;
		mDisplayOffset = 0;
		mCurrentX = 0;
		mNextX = 0;
		mMaxX = Integer.MAX_VALUE;
		mScroller = new Scroller(getContext());
		mGesture = new GestureDetector(getContext(), mOnGesture);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
		int parentHeight = MeasureSpec.getSize(heightMeasureSpec);

		setMeasuredDimension(MeasureSpec.makeMeasureSpec(parentWidth, MeasureSpec.EXACTLY),
				MeasureSpec.makeMeasureSpec(parentHeight, MeasureSpec.EXACTLY));
	}

	public int findMotionRow(int y) {
		return 0;
	}

	public void fillGap(boolean down) {

	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();

		if (mPerformClick != null) {
			removeCallbacks(mPerformClick);
		}
		if (mTouchModeReset != null) {
			removeCallbacks(mTouchModeReset);
			mTouchModeReset = null;
		}
	}

	@Override
	public void setOnItemSelectedListener(AdapterView.OnItemSelectedListener listener) {
		mOnItemSelected = listener;
	}

	private DataSetObserver mDataObserver = new DataSetObserver() {

		@Override
		public void onChanged() {
			synchronized (HorizontalListView.this) {
				mDataChanged = true;
			}
			setEmptyView(getEmptyView());
			invalidate();
			requestLayout();
		}

		@Override
		public void onInvalidated() {
			reset();
			invalidate();
			requestLayout();
		}

	};
	protected PerformLongPress mPerformLongPress;

	@Override
	public ListAdapter getAdapter() {
		return mAdapter;
	}

	@Override
	public View getSelectedView() {
		// TODO: implement
		return null;
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		if (mAdapter != null) {
			mAdapter.unregisterDataSetObserver(mDataObserver);
		}
		mAdapter = adapter;
		mAdapter.registerDataSetObserver(mDataObserver);
		reset();
	}

	private synchronized void reset() {
		initView();
		removeAllViewsInLayout();
		requestLayout();
	}

	@Override
	public void setSelection(int position) {
		// TODO: implement
	}

	private void addAndMeasureChild(final View child, int viewPos) {
		LayoutParams params = (AbsListView.LayoutParams) child.getLayoutParams();
		if (params == null) {
			params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		}

		child.setTag(DownloadType.NONE);// update ui
		addViewInLayout(child, viewPos, params, true);
		child.measure(MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.AT_MOST));
	}
	
	

	@Override
	protected synchronized void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);

		if (mAdapter == null) {
			return;
		}

		if (mDataChanged) {
			int oldCurrentX = mCurrentX;
			initView();
			removeAllViewsInLayout();
			mNextX = oldCurrentX;
			mDataChanged = false;
		}

		if (mScroller.computeScrollOffset()) {
			int scrollx = mScroller.getCurrX();
			mNextX = scrollx;
		}

		if (mNextX <= 0) {
			mNextX = 0;
			mScroller.forceFinished(true);
		}
		if (mNextX >= mMaxX) {
			mNextX = mMaxX;
			mScroller.forceFinished(true);
		}

		int dx = mCurrentX - mNextX;

		removeNonVisibleItems(dx);
		fillList(dx);
		positionItems(dx);

		mCurrentX = mNextX;

		if (!mScroller.isFinished()) {
			post(new Runnable() {
				@Override
				public void run() {
					if (mOnItemUpdateListener != null) {
						int childCount = getChildCount();
						for (int i = 0; i < childCount; i++) {
							View child = getChildAt(i);
							DownloadType type = (DownloadType) child.getTag();
							if (type == DownloadType.NONE) {
								mOnItemUpdateListener.updateFling(HorizontalListView.this, getChildAt(i), mLeftViewIndex + 1 + i,
										mAdapter.getItemId(mLeftViewIndex + 1 + i));
								child.setTag(DownloadType.CACHE);
							}
						}
					}
					requestLayout();
				}
			});
		} else {
			if (mOnItemUpdateListener != null) {
				int childCount = getChildCount();
				for (int i = 0; i < childCount; i++) {
					View child = getChildAt(i);
					DownloadType type = (DownloadType) child.getTag();
					if (type != DownloadType.DOWNLOAD) {
						mOnItemUpdateListener.updateOnStop(HorizontalListView.this, getChildAt(i), mLeftViewIndex + 1 + i,
								mAdapter.getItemId(mLeftViewIndex + 1 + i));
						child.setTag(DownloadType.DOWNLOAD);
					}
				}
			}
		}
	}

	private void fillList(final int dx) {
		int edge = 0;
		View child = getChildAt(getChildCount() - 1);
		if (child != null) {
			edge = child.getRight();
		}
		fillListRight(edge, dx);

		edge = 0;
		child = getChildAt(0);
		if (child != null) {
			edge = child.getLeft();
		}
		fillListLeft(edge, dx);

	}

	private void fillListRight(int rightEdge, final int dx) {
		while (rightEdge + dx < getWidth() && mRightViewIndex < mAdapter.getCount()) {

			View child = mAdapter.getView(mRightViewIndex, pollChildFromCache(mRightViewIndex), this);
			addAndMeasureChild(child, -1);
			rightEdge += child.getMeasuredWidth();

			if (mRightViewIndex == mAdapter.getCount() - 1) {
				mMaxX = mCurrentX + rightEdge - getWidth();
			}

			if (mMaxX < 0) {
				mMaxX = 0;
			}
			mRightViewIndex++;
		}

	}

	private void fillListLeft(int leftEdge, final int dx) {
		while (leftEdge + dx > 0 && mLeftViewIndex >= 0) {
			View child = mAdapter.getView(mLeftViewIndex, pollChildFromCache(mLeftViewIndex), this);
			addAndMeasureChild(child, 0);
			leftEdge -= child.getMeasuredWidth();
			mLeftViewIndex--;
			mDisplayOffset -= child.getMeasuredWidth();
		}
	}

	private void removeNonVisibleItems(final int dx) {
		View child = getChildAt(0);
		while (child != null && child.getRight() + dx <= 0) {
			mDisplayOffset += child.getMeasuredWidth();
			offerChildToCache(mLeftViewIndex + 1, child);
			removeViewInLayout(child);
			mLeftViewIndex++;
			child = getChildAt(0);

		}

		child = getChildAt(getChildCount() - 1);
		while (child != null && child.getLeft() + dx >= getWidth()) {
			offerChildToCache(mRightViewIndex - 1, child);
			removeViewInLayout(child);
			mRightViewIndex--;
			child = getChildAt(getChildCount() - 1);
		}
	}

	/**
	 * Save ViewType child to cache listing
	 * 
	 * @param postion
	 *            Item position in list
	 * @param child
	 */
	private void offerChildToCache(int postion, View child) {
		int viewType = getAdapter().getItemViewType(postion);
		LinkedList<View> queue = (LinkedList<View>) mRemovedViewQueue.get(viewType);
		if (queue == null) {
			queue = new LinkedList<View>();
		}
		queue.offer(child);
		mRemovedViewQueue.put(viewType, queue);
	}

	/**
	 * Get child at ViewType index
	 * 
	 * @param postion
	 *            Item position in list
	 * @return
	 */
	private View pollChildFromCache(int postion) {
		int viewType = getAdapter().getItemViewType(postion);

		LinkedList<View> queue = (LinkedList<View>) mRemovedViewQueue.get(viewType);
		if (queue == null) {
			return null;
		}
		return queue.poll();
	}

	private void positionItems(final int dx) {
		if (getChildCount() > 0) {
			mDisplayOffset += dx;
			int left = mDisplayOffset;
			for (int i = 0; i < getChildCount(); i++) {
				View child = getChildAt(i);
				int childWidth = child.getMeasuredWidth();
				child.layout(left, 0, left + childWidth, child.getMeasuredHeight());
				left += childWidth + child.getPaddingRight();
			}
		}
	}

	public synchronized void scrollTo(int x) {
		mScroller.startScroll(mNextX, 0, x - mNextX, 0);
		requestLayout();
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		boolean handled = mGesture.onTouchEvent(ev);
		return handled;
	}

	protected boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		synchronized (HorizontalListView.this) {
			mScroller.fling(mNextX, 0, (int) -velocityX, 0, 0, mMaxX, 0, 0);
		}
		requestLayout();

		return true;
	}

	protected boolean onDown(MotionEvent e) {
		mScroller.forceFinished(true);
		return true;
	}

	public void setOnItemUpdateListener(OnItemUpdateListener mOnItemUpdateListener) {
		this.mOnItemUpdateListener = mOnItemUpdateListener;
	}

	private OnGestureListener mOnGesture = new GestureDetector.SimpleOnGestureListener() {

		@Override
		public boolean onDown(MotionEvent e) {
			return HorizontalListView.this.onDown(e);
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			return HorizontalListView.this.onFling(e1, e2, velocityX, velocityY);
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			getParent().requestDisallowInterceptTouchEvent(true);

			synchronized (HorizontalListView.this) {
				mNextX += (int) distanceX;
			}

			requestLayout();

			return true;
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {

			Rect viewRect = new Rect();
			for (int i = 0; i < getChildCount(); i++) {
				final View child = getChildAt(i);
				int left = child.getLeft();
				int right = child.getRight();
				int top = child.getTop();
				int bottom = child.getBottom();
				viewRect.set(left, top, right, bottom);
				if (viewRect.contains((int) e.getX(), (int) e.getY())) {
					// TODO add Selector here

					child.setPressed(true);
					refreshDrawableState();
					setPressed(true);

					if (mPerformClick == null) {
						mPerformClick = new PerformClick();
					}

					final HorizontalListView.PerformClick performClick = mPerformClick;
					performClick.mClickMotionPosition = i;
					performClick.rememberWindowAttachCount();

					if (mTouchModeReset != null) {
						removeCallbacks(mTouchModeReset);
					}

					mTouchModeReset = new Runnable() {
						@Override
						public void run() {
							child.setPressed(false);
							setPressed(false);
							if (!mDataChanged) {
								performClick.run();
							}
						}
					};
					postDelayed(mTouchModeReset, ViewConfiguration.getPressedStateDuration());

					break;
					// return true;
				}

			}
			return false;
		}

		@Override
		public void onLongPress(MotionEvent e) {
			Rect viewRect = new Rect();
			int childCount = getChildCount();
			for (int i = 0; i < childCount; i++) {
				final View child = getChildAt(i);
				int left = child.getLeft();
				int right = child.getRight();
				int top = child.getTop();
				int bottom = child.getBottom();
				viewRect.set(left, top, right, bottom);
				if (viewRect.contains((int) e.getX(), (int) e.getY())) {

					child.setPressed(true);
					refreshDrawableState();
					setPressed(true);

					if (mPerformLongPress == null) {
						mPerformLongPress = new PerformLongPress();
					}

					final HorizontalListView.PerformLongPress performLongPress = mPerformLongPress;
					mPerformLongPress.mClickMotionPosition = i;
					mPerformLongPress.rememberWindowAttachCount();

					if (mTouchModeReset != null) {
						removeCallbacks(mTouchModeReset);
					}

					mTouchModeReset = new Runnable() {
						@Override
						public void run() {
							child.setPressed(false);
							setPressed(false);
							if (!mDataChanged) {
								performLongPress.run();
							}
						}
					};
					postDelayed(mTouchModeReset, ViewConfiguration.getLongPressTimeout());
					break;
				}

			}
		}

	};

	public static abstract class OnItemUpdateListener {

		public abstract void updateOnStop(AdapterView<?> adapterView, View view, int position, long id);

		public abstract void updateFling(AdapterView<?> adapterView, View view, int position, long id);

	}

	/**
	 * A base class for Runnables that will check that their view is still attached to the original window as when the Runnable was created.
	 * 
	 */
	private class WindowRunnnable {
		private int mOriginalAttachCount;

		public void rememberWindowAttachCount() {
			mOriginalAttachCount = getWindowAttachCount();
		}

		public boolean sameWindow() {
			return hasWindowFocus() && getWindowAttachCount() == mOriginalAttachCount;
		}
	}

	private class PerformClick extends WindowRunnnable implements Runnable {
		int mClickMotionPosition;

		public void run() {
			// The data has changed since we posted this action in the event queue,
			// bail out before bad things happen
			if (mDataChanged)
				return;

			final ListAdapter adapter = mAdapter;
			final int motionPosition = mClickMotionPosition;
			if (adapter != null && mAdapter.getCount() > 0 && motionPosition != INVALID_POSITION && motionPosition < adapter.getCount()
					&& sameWindow()) {
				final View view = getChildAt(motionPosition);
				// If there is no view, something bad happened (the view scrolled off the
				// screen, etc.) and we should cancel the click
				if (view != null) {
					performItemClick(view, motionPosition, adapter.getItemId(motionPosition));
				}
			}
		}
	}

	private class PerformLongPress extends WindowRunnnable implements Runnable {
		public int mClickMotionPosition;

		public void run() {
			// The data has changed since we posted this action in the event queue,
			// bail out before bad things happen
			if (mDataChanged)
				return;

			final ListAdapter adapter = mAdapter;
			final int motionPosition = mClickMotionPosition;
			if (adapter != null && mAdapter.getCount() > 0 && motionPosition != INVALID_POSITION && motionPosition < adapter.getCount()
					&& sameWindow()) {
				final View view = getChildAt(motionPosition);
				// If there is no view, something bad happened (the view scrolled off the
				// screen, etc.) and we should cancel the click
				if (view != null) {
					performLongPress(view, motionPosition, adapter.getItemId(motionPosition));
				}
			}
		}
	}

	/**
	 * Creates the ContextMenuInfo returned from {@link #getContextMenuInfo()}. This methods knows the view, position and ID of the item that received the long press.
	 * 
	 * @param view
	 *            The view that received the long press.
	 * @param position
	 *            The position of the item that received the long press.
	 * @param id
	 *            The ID of the item that received the long press.
	 * @return The extra information that should be returned by {@link #getContextMenuInfo()}.
	 */
	ContextMenuInfo createContextMenuInfo(View view, int position, long id) {
		return new AdapterContextMenuInfo(view, position, id);
	}

	boolean performLongPress(final View child, final int longPressPosition, final long longPressId) {
		boolean handled = false;
		if (mOnItemLongClickListener != null) {
			handled = mOnItemLongClickListener.onItemLongClick(HorizontalListView.this, child, longPressPosition, longPressId);
		}
		if (!handled) {
			mContextMenuInfo = createContextMenuInfo(child, longPressPosition, longPressId);
			handled = super.showContextMenuForChild(HorizontalListView.this);
		}
		if (handled) {
			performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
		}
		return handled;
	}
}
