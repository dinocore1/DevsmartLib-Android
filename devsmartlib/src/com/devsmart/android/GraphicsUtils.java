package com.devsmart.android;

import java.io.FileDescriptor;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Matrix.ScaleToFit;

public class GraphicsUtils {
	
	public static enum ScaleType {
		CENTER_CROP,
		CENTER_FIT
	}
	
	public static Matrix createScaleRect(RectF src, RectF dest, ScaleType type){
		Matrix retval = new Matrix();
		
		switch(type){
		case CENTER_CROP:
			float[] points = new float[8];
			
			points[0] = src.left;
			points[1] = src.top;
			points[2] = src.right;
			points[3] = src.bottom;
			
			points[4] = dest.left;
			points[5] = dest.top;
			points[6] = dest.right;
			points[7] = dest.bottom;
			
			final float ratioSrc = src.width() / src.height();
			final float ratioDest = dest.width() / dest.height();
			
			if(ratioSrc > ratioDest){
				float fwidth = ratioSrc * dest.height();
				points[4] = dest.left - (fwidth - dest.width()) / 2;
				points[6] = dest.right + (fwidth - dest.width()) / 2;
			} else {
				float fheight = dest.width() / ratioSrc;
				points[5] = dest.top - (fheight - dest.height()) / 2;
				points[7] = dest.bottom + (fheight - dest.height()) / 2;
			}
			retval.setPolyToPoly(points, 0, points, 4, 2);
			
			break;
			
		case CENTER_FIT:
			retval.setRectToRect(src, dest, ScaleToFit.CENTER);
		}
		
		return retval;
	}
	
	public static Bitmap rotateBitmap(Bitmap input, int degrees) {
		RectF srcRect = new RectF(0, 0, input.getWidth(), input.getHeight());
		Matrix matrix = new Matrix();
		matrix.setRotate(degrees);
		matrix.mapRect(srcRect);
		matrix.postTranslate(0 - srcRect.left, 0 - srcRect.top);
		
		Bitmap targetBitmap = Bitmap.createBitmap(Math.round(srcRect.width()), Math.round(srcRect.height()), Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(targetBitmap);
		canvas.drawBitmap(input, matrix, new Paint());
		return targetBitmap;
	}
	
	public static Bitmap downsampleBitmap(FileDescriptor fd, int maxArea) {
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;
		
		Rect outRect = new Rect();
		BitmapFactory.decodeFileDescriptor(fd, outRect, opts);
		
		int subsample = 1;
		int width = opts.outWidth;
		int height = opts.outHeight;
		while(width * height > maxArea) {
			width /= 2;
			height /= 2;
			subsample++;
		}
		
		opts.inJustDecodeBounds = false;
		opts.inSampleSize = subsample;
		Bitmap retval = BitmapFactory.decodeFileDescriptor(fd, null, opts);
		return retval;
	}
	

}