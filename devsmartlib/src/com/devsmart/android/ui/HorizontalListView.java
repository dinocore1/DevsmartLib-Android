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


import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.Scroller;

/**
 * Class acting like a ListView - using some adapter and convertviews, but in
 * horizontal direction. 
 * Also supports snapping when the scrolling slows down -
 * so that it always centers some view when scrolling stops
 */
public class HorizontalListView extends AdapterView<ListAdapter>
{

	public boolean mAlwaysOverrideTouch = true;
	protected ListAdapter mAdapter;
	
	// the index of the next view coming in form the left
	private int mLeftViewIndex = -1;
	//the index of the next view coming in from the right
	private int mRightViewIndex = 0;
	// the current scroll in pixels
	protected int mCurrentX;
	// the scroll to apply in the next drawing / onLayout Frame
	protected int mNextX;
	
	// the start of the first view visible
	private int mDisplayOffset = 0;
	protected Scroller mScroller;
	private GestureDetector mGesture;
	private Queue<View> mRemovedViewQueue = new LinkedList<View>();
	private OnItemSelectedListener mOnItemSelected;
	private OnItemClickListener mOnItemClicked;
	private OnItemLongClickListener mOnItemLongClicked;
	private boolean mDataChanged = false;
	
	
	private AdjustPositionAnimation mAdjustAnimation;
	private boolean mFingerDown = false;
	private boolean mSnappingToCenter = false;
	private boolean mCircleScrolling = false;
	private boolean mFlinging = false;
	private int mSelectedIndex =0;


	private int mMaxX = Integer.MAX_VALUE;
	private int mMinX=0;
	
	public HorizontalListView(Context context)
	{
		super(context);
		initView();
	}

	public HorizontalListView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		initView();
	}

	/**
	 * is the list snapping to the middle item
	 * @return
	 */
	public boolean isSnappingToCenter()
	{
		return mSnappingToCenter;
	}

	/**
	 * sets whether the view should snap to its middle item
	 * @param snappingToCenter
	 */
	public void setSnappingToCenter(boolean snappingToCenter)
	{
		mSnappingToCenter = snappingToCenter;
		adjustSubviewPositions();
	}

	/**
	 * if the view is scrolling in a circle - there is no first and last item then
	 * @return
	 */
	public boolean isCircleScrolling()
	{
		return mCircleScrolling;
	}

	/**
	 * sets whether the view should scroll in an infinite circle
	 * @param circleScrolling
	 */
	public void setCircleScrolling(boolean circleScrolling)
	{
		mCircleScrolling = circleScrolling;
		adjustSubviewPositions();
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		super.onSizeChanged(w, h, oldw, oldh);
		setSelection(mSelectedIndex);
	}
	
	private synchronized void initView()
	{
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
	public void setOnItemSelectedListener(AdapterView.OnItemSelectedListener listener)
	{
		mOnItemSelected = listener;
	}

	@Override
	public void setOnItemClickListener(AdapterView.OnItemClickListener listener)
	{
		mOnItemClicked = listener;
	}

	@Override
	public void setOnItemLongClickListener(AdapterView.OnItemLongClickListener listener)
	{
		mOnItemLongClicked = listener;
	}

	private DataSetObserver mDataObserver = new DataSetObserver()
	{

		@Override
		public void onChanged()
		{
			synchronized (HorizontalListView.this)
			{
				mDataChanged = true;
			}
			adjustSubviewPositions();
		}

		@Override
		public void onInvalidated()
		{
			reset();
			adjustSubviewPositions();
		}

	};

	@Override
	public ListAdapter getAdapter()
	{
		return mAdapter;
	}

	@Override
	public View getSelectedView()
	{
		return getMiddleItem();
	}

	@Override
	public void setAdapter(ListAdapter adapter)
	{
		if (mAdapter != null)
		{
			mAdapter.unregisterDataSetObserver(mDataObserver);
		}
		mAdapter = adapter;
		mAdapter.registerDataSetObserver(mDataObserver);
		reset();
	}

	/**
	 * resets this view
	 */
	private synchronized void reset()
	{
		initView();
		removeAllViewsInLayout();
		adjustSubviewPositions();
	}

	@Override
	public void setSelection(int position)
	{
		int childCount = getChildCount();
		for (int i = 0; i < childCount; i++)
		{
			View child = getChildAt(i);
			mRemovedViewQueue.offer(child);
			removeViewInLayout(child);
		}
		
		mLeftViewIndex = position-1;
		mRightViewIndex = position+1;
		
		View newCenterView = mAdapter.getView(position, mRemovedViewQueue.poll(), this);
		if (newCenterView != null)
		{
			mSelectedIndex = position;
			addAndMeasureChild(newCenterView, 0);
			int newViewWidth = newCenterView.getMeasuredWidth();
			int center = getWidth()/2;
			int leftEdge = center - newViewWidth/2;
			int rightEdge = center + newViewWidth/2;
			mDisplayOffset = leftEdge;
			fillListLeft(leftEdge, 0);
			fillListRight(rightEdge, 0);
			positionItems(0);
			mCurrentX = newViewWidth*position; //just an estimate - not so important since the actual corners get set in fillListRight and Left
			mNextX = mCurrentX;
			adjustSubviewPositions();
		}
	}

	private void addAndMeasureChild(final View child, int viewPos)
	{
		LayoutParams params = child.getLayoutParams();
		if (params == null)
		{
			params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		}

		addViewInLayout(child, viewPos, params, true);
		child.measure(MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.AT_MOST),
				MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.AT_MOST));
	}
	

	@Override
	protected synchronized void onLayout(boolean changed, int left, int top, int right, int bottom)
	{
		super.onLayout(changed, left, top, right, bottom);
	}


	/**
	 * readjusts the positions of the subviewa according to the new Position set in mNextX
	 */
	private void adjustSubviewPositions()
	{
		if (mAdapter == null)
		{
			return;
		}

		if (mDataChanged)
		{
			int oldCurrentX = mCurrentX;
			initView();
			removeAllViewsInLayout();
			mNextX = oldCurrentX;
			mDataChanged = false;
		}

		if (mScroller.computeScrollOffset())
		{
			int scrollx = mScroller.getCurrX();
			mNextX = scrollx;
		}

		if (mNextX <=mMinX && !mCircleScrolling)
		{
			mNextX = mMinX;
			mScroller.forceFinished(true);
			if (mAdjustAnimation!=null)
			{
				mAdjustAnimation.stop();
			}
		}
		if (mNextX >= mMaxX && !mCircleScrolling)
		{
			mNextX = mMaxX;
			mScroller.forceFinished(true);
			if (mAdjustAnimation!=null)
			{
				mAdjustAnimation.stop();
			}
		}

		int dx = mCurrentX - mNextX;

		
		removeNonVisibleItems(dx);
		fillList(dx);
		positionItems(dx);
		
		mCurrentX = mNextX;

		if (!mScroller.isFinished())
		{
			post(new Runnable()
			{
				@Override
				public void run()
				{
					adjustSubviewPositions();
				}
			});
		}
		else
		{
			if (mFlinging)
			{
				scrollerFinished();
			}
			mFlinging = false;
		}
		invalidate();
	}
	
	@Override
	public long getSelectedItemId()
	{
		long result = getMiddlePositon();
		return result==-1?INVALID_ROW_ID:result;
	}
	
	@Override
	public int getSelectedItemPosition()
	{
		int result = getMiddlePositon();
		return result==-1?INVALID_POSITION:result;
	}
	
	

	/**
	 * fills the list according to the current transition
	 * @param dx the current movement
	 */
	private void fillList(final int dx)
	{
		int edge = 0;
		View child = getChildAt(getChildCount() - 1);
		if (child != null)
		{
			edge = child.getRight();
		}
		fillListRight(edge, dx);

		edge = 0;
		child = getChildAt(0);
		if (child != null)
		{
			edge = child.getLeft();
		}
		fillListLeft(edge, dx);

	}

	
	/**
	 * gets the index of the next item to come in from the right
	 * @return
	 */
	private int getNextRightItemNo()
	{
		return getAdapterIndexNumber(mRightViewIndex);
	}
	
	/**
	 * fills the list from the right
	 * @param rightEdge the edge to start from
	 * @param dx the current movement
	 */
	private void fillListRight(int rightEdge, final int dx)
	{
		int nextViewNo = getNextRightItemNo();
		while (rightEdge + dx < getWidth() && nextViewNo < mAdapter.getCount())
		{
			View child = mAdapter.getView(nextViewNo, mRemovedViewQueue.poll(), this);
			addAndMeasureChild(child, -1);
			rightEdge += child.getMeasuredWidth();
			mRightViewIndex++;
			nextViewNo = getNextRightItemNo();
		}
		
		if (mRightViewIndex == mAdapter.getCount() )
		{
			mMaxX = mCurrentX + rightEdge - getWidth()/2;
		}
		else
		{
			mMaxX = Integer.MAX_VALUE;
		}

	}

	/**
	 * fills the list from the left side
	 * @param leftEdge the edge to start from
	 * @param dx the movement of this next step
	 */
	private void fillListLeft(int leftEdge, final int dx)
	{
		int nextViewNo = getNextLeftItemNo();
		while (leftEdge + dx > 0 && nextViewNo >= 0)
		{
			View child = mAdapter.getView(nextViewNo, mRemovedViewQueue.poll(), this);
			addAndMeasureChild(child, 0);
			leftEdge -= child.getMeasuredWidth();
			mLeftViewIndex--;
			nextViewNo = getNextLeftItemNo();
			mDisplayOffset -= child.getMeasuredWidth();
		}
		
		if (mLeftViewIndex == -1 )
		{
			mMinX = mCurrentX + leftEdge - getWidth()/2;
		}
		else
		{
			mMinX = Integer.MIN_VALUE;
		}
	}
	
	/**
	 * returns the index of the next item to come in from the left side 
	 * @return
	 */
	private int getNextLeftItemNo()
	{
		return getAdapterIndexNumber(mLeftViewIndex);
	}
	
	
	/**
	 * Returns the real number in a range between 0 and adapter.getCount -1 if circleScrolling is activated
	 * @param i
	 * @return
	 */
	private int getAdapterIndexNumber(int i)
	{
		if (mAdapter != null)
		{
			if (mCircleScrolling)
			{
				int result = i % mAdapter.getCount();
				return result<0?result + mAdapter.getCount():result;
			}
			else
			{
				return i;
			
			}
		}
		return -1;
	}
	

	private void removeNonVisibleItems(final int dx)
	{
		View child = getChildAt(0);
		while (child != null && child.getRight() + dx <= 0)
		{
			mDisplayOffset += child.getMeasuredWidth();
			mRemovedViewQueue.offer(child);
			removeViewInLayout(child);
			mLeftViewIndex++;
			child = getChildAt(0);

		}

		child = getChildAt(getChildCount() - 1);
		while (child != null && child.getLeft() + dx >= getWidth())
		{
			mRemovedViewQueue.offer(child);
			removeViewInLayout(child);
			mRightViewIndex--;
			child = getChildAt(getChildCount() - 1);
		}
	}

	private void positionItems(final int dx)
	{
		if (getChildCount() > 0)
		{
			mDisplayOffset += dx;
			int left = mDisplayOffset;
			for (int i = 0; i < getChildCount(); i++)
			{
				View child = getChildAt(i);
				int childWidth = child.getMeasuredWidth();
				child.layout(left, 0, left + childWidth, child.getMeasuredHeight());
				left += childWidth + child.getPaddingRight();
			}
		}
	}

	public synchronized void scrollTo(int x)
	{
		mScroller.startScroll(mCurrentX, 0, x-mCurrentX, 0, 500);
		adjustSubviewPositions();
		invalidate();
	}
	

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev)
	{
		boolean handled = super.dispatchTouchEvent(ev);
		handled |= mGesture.onTouchEvent(ev);
		switch (ev.getAction())
		{
			case MotionEvent.ACTION_DOWN:
				mFingerDown = true;
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				mFingerDown = false;
				onUp();
				break;
			default:
				break;
		}
		return handled;
	}

	

	protected boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
	{
		synchronized (HorizontalListView.this)
		{
			mFlinging = true;
			mScroller.fling(mNextX, 0, (int) -velocityX, 0, mCircleScrolling?Integer.MIN_VALUE:mMinX, mCircleScrolling?Integer.MAX_VALUE:mMaxX, 0, 0);
		}
		adjustSubviewPositions();
		return true;
	}

	protected boolean onDown(MotionEvent e)
	{
		synchronized (HorizontalListView.this)
		{
			mScroller.forceFinished(true);
			if (mAdjustAnimation!=null)
			{
				mAdjustAnimation.stop();
			}
		
		}
		return true;
	}
	
	/**
	 * called when the user lifts its finger
	 */
	private void onUp()
	{
		synchronized (HorizontalListView.this)
		{
			if (!mFlinging)
			{
				readjustScrollToMiddleItem();
			}
		}
	}
	
	/**
	 * called when the scrolling (fling) is finished
	 */
	private void scrollerFinished()
	{
		if (!mFingerDown)
		{
			readjustScrollToMiddleItem();
		}
		
	}
	
	
	/**
	 * snaps the view to the child in the middle 
	 */
	private void readjustScrollToMiddleItem()
	{
		if (mSnappingToCenter)
		{
			View middleChild = getMiddleItem();
			if (middleChild != null)
			{
				mSelectedIndex = getMiddlePositon();
				int width = getWidth();
				int center = width/2;
				int childwidth = middleChild.getMeasuredWidth();
				int middleItemCenter = middleChild.getLeft() + childwidth /2;
				int moveDx = middleItemCenter-center;
				if (mAdjustAnimation!=null)
				{
					mAdjustAnimation.stop();
				}
				mAdjustAnimation = new AdjustPositionAnimation(moveDx);
				setAnimation(mAdjustAnimation);
				startAnimation(mAdjustAnimation);
			}
		}
	}

	/**
	 * returns the view closest to the middle
	 * assumes that all children are of the same size
	 * @return the middleMostView
	 */
	private View getMiddleItem()
	{
		synchronized (this)
		{
			int count = getChildCount();
			if (count!= 0)
			{
				int width = getWidth();
				int childNumber =0;
				View child = null;
				// take next child starting from left until one is reached whose right edge is over the middle
				do
				{
					child = getChildAt(childNumber);
					childNumber++;
				}
				while (childNumber < count && child.getRight()<width/2);
				return child;
			}
		}
		
		return null;
	}
	
	/**
	 * returns the medium position of this listview
	 * @return
	 */
	private int getMiddlePositon()
	{
		int count = getChildCount();
		if (count!= 0)
		{
			int width = getWidth();
			int childNumber =0;
			View child = null;
			// take next child starting from left until one is reached whose right edge is over the middle
			do
			{
				child = getChildAt(childNumber);
				childNumber++;
			}
			while (childNumber < count && child.getRight()<width/2);
			return getAdapterIndexNumber(mLeftViewIndex + childNumber);
		}
		return 0;
	}
	
	

	private OnGestureListener mOnGesture = new GestureDetector.SimpleOnGestureListener()
	{

		@Override
		public boolean onDown(MotionEvent e)
		{
			return HorizontalListView.this.onDown(e);
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
		{
			return HorizontalListView.this.onFling(e1, e2, velocityX, velocityY);
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
		{

			synchronized (HorizontalListView.this)
			{
				mNextX += (int) distanceX;
			}
			adjustSubviewPositions();
			return true;
		}
		

		@Override
		public boolean onSingleTapConfirmed(MotionEvent e)
		{
			for (int i = 0; i < getChildCount(); i++)
			{
				View child = getChildAt(i);
				if (isEventWithinView(e, child))
				{
					if (mOnItemClicked != null)
					{
						mOnItemClicked.onItemClick(HorizontalListView.this, child, mLeftViewIndex + 1 + i,
								mAdapter.getItemId(mLeftViewIndex + 1 + i));
					}
					if (mOnItemSelected != null)
					{
						mOnItemSelected.onItemSelected(HorizontalListView.this, child, mLeftViewIndex + 1 + i,
								mAdapter.getItemId(mLeftViewIndex + 1 + i));
					}
					break;
				}

			}
			return true;
		}

		@Override
		public void onLongPress(MotionEvent e)
		{
			int childCount = getChildCount();
			for (int i = 0; i < childCount; i++)
			{
				View child = getChildAt(i);
				if (isEventWithinView(e, child))
				{
					if (mOnItemLongClicked != null)
					{
						mOnItemLongClicked.onItemLongClick(HorizontalListView.this, child, mLeftViewIndex + 1 + i,
								mAdapter.getItemId(mLeftViewIndex + 1 + i));
					}
					break;
				}

			}
		}
		

		private boolean isEventWithinView(MotionEvent e, View child)
		{
			Rect viewRect = new Rect();
			int[] childPosition = new int[2];
			child.getLocationOnScreen(childPosition);
			int left = childPosition[0];
			int right = left + child.getWidth();
			int top = childPosition[1];
			int bottom = top + child.getHeight();
			viewRect.set(left, top, right, bottom);
			return viewRect.contains((int) e.getRawX(), (int) e.getRawY());
		}
	};

	/**
	 * adjusts the position by the given number of pixels
	 */
	private class AdjustPositionAnimation extends Animation
	{
		
		private int mPixelsToScrollLeft;
		private boolean mCancelled = false;

		public AdjustPositionAnimation (int pixelsToScrollLeft)
		{
			mPixelsToScrollLeft = pixelsToScrollLeft;
			setInterpolator(new AccelerateDecelerateInterpolator());
			setDuration(200);
		}
		
		public void stop()
		{
			mCancelled = true;
		}
		
		private int mAlreadyScrolled =0;
		@Override
		protected void applyTransformation(float interpolatedTime, Transformation t)
		{
			if (!mCancelled)
			{
				int scrollThisStep = (int) (interpolatedTime * mPixelsToScrollLeft) -mAlreadyScrolled;
				mAlreadyScrolled += scrollThisStep;
				mNextX += scrollThisStep;
				adjustSubviewPositions();
			}
			
		}
		
	}

}