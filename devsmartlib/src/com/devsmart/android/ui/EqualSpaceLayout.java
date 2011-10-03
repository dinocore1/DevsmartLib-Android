package com.devsmart.android.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class EqualSpaceLayout extends ViewGroup {


	private int mOrientation = LinearLayout.HORIZONTAL;
	private int mMaxChildWidth;
	private int mMaxChildHeight;
	private int mNumVisibleChildren;

	public EqualSpaceLayout(Context context){
		super(context);
		initDefaults();
	}
	
	public EqualSpaceLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		initDefaults();
		
		{
			TypedArray a = context.obtainStyledAttributes(attrs, new int[]{
					android.R.attr.layout_width,
					android.R.attr.layout_height,
			});
			
			int layoutwidth = a.getLayoutDimension(0, ViewGroup.LayoutParams.WRAP_CONTENT);
			int layoutheight = a.getLayoutDimension(1, ViewGroup.LayoutParams.WRAP_CONTENT);
			setLayoutParams(new ViewGroup.LayoutParams(layoutwidth, layoutheight));
			a.recycle();
		}
		
		{
			TypedArray a = context.obtainStyledAttributes(attrs, new int[]{android.R.attr.orientation});
			mOrientation = a.getInt(0, LinearLayout.HORIZONTAL);
			a.recycle();
		}
	}
	
	private void initDefaults() {
		setLayoutParams(new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT));
		
	}
	
	public void setOrientation(int orient){
		mOrientation = orient;
	}
	

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		
		final int count = getChildCount();
		mNumVisibleChildren = 0;
		for(int i=0;i<count;i++){
			final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
            	mNumVisibleChildren++;
            }
		}
		
		if(mNumVisibleChildren == 0){
			return;
		}
		
		int maxChildWidth = mOrientation == LinearLayout.HORIZONTAL ? (MeasureSpec.getSize(widthMeasureSpec) / mNumVisibleChildren) : MeasureSpec.getSize(widthMeasureSpec);
		int maxChildHeight = mOrientation == LinearLayout.VERTICAL ?  (MeasureSpec.getSize(heightMeasureSpec) / mNumVisibleChildren) : MeasureSpec.getSize(heightMeasureSpec);
		
		final int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(
                MeasureSpec.getSize(maxChildWidth), MeasureSpec.AT_MOST);
        final int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
                MeasureSpec.getSize(maxChildHeight), MeasureSpec.AT_MOST);
		
        mMaxChildWidth = 0;
        mMaxChildHeight = 0;
        
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue;
            }

            child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
            if(mOrientation == LinearLayout.HORIZONTAL){
            	mMaxChildWidth += child.getMeasuredWidth();
            } else {
            	mMaxChildWidth = Math.max(mMaxChildWidth, child.getMeasuredWidth());
            }
            if(mOrientation == LinearLayout.VERTICAL){
            	mMaxChildHeight += child.getMeasuredHeight();
            } else {
            	mMaxChildHeight = Math.max(mMaxChildHeight, child.getMeasuredHeight());
            }
        }
        
        int width = resolveSize(mMaxChildWidth, widthMeasureSpec);
        int height = resolveSize(mMaxChildHeight, heightMeasureSpec);
        setMeasuredDimension(width, height);
	}



	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		
		final int count = getChildCount();
		
		int spacing = 0;
		if(mOrientation == LinearLayout.HORIZONTAL){
			spacing = ((r-l) - mMaxChildWidth) / mNumVisibleChildren;
		} else if(mOrientation == LinearLayout.VERTICAL){
			spacing = ((b-t) - mMaxChildHeight) / mNumVisibleChildren;
		}
		
		
		l = -spacing / 2;
		t = -spacing / 2;
		
		for (int i = 0; i < count; i++) {
			final View child = getChildAt(i);
			if (child.getVisibility() == GONE) {
				continue;
			}
			
			if(mOrientation == LinearLayout.HORIZONTAL){
				l += spacing;
				r = l + child.getMeasuredWidth();
				child.layout(l, 0, r, child.getMeasuredHeight());
				l = r;
			} else if(mOrientation == LinearLayout.VERTICAL){
				t += spacing;
				b = t + child.getMeasuredHeight();
				child.layout(0, t, child.getMeasuredWidth(), b);
				t = b;
			}
		}

	}

}
