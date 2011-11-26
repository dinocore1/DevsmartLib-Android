package com.devsmart.android;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;


public class OrientationUtil implements SensorEventListener {

	public interface OrientationListener {
		
		public void onOrientationChanged(Side newOrientation);
	}
	
	public enum Side {
        TOP,
        BOTTOM,
        LEFT,
        RIGHT;
    }

	private Side mCurrentSide;
	private OrientationListener mListener;
	private SensorManager mSensorManager;
	
	public OrientationUtil(Context context) {
		mSensorManager = (SensorManager)context.getSystemService(Activity.SENSOR_SERVICE);
		mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_NORMAL);
	}
	
	public void setListener(OrientationListener listener){
		mListener = listener;
	}
	
	public void stop() {
		mSensorManager.unregisterListener(this);
	}
	
	public Side getOrientation() {
		return mCurrentSide;
	}
	
	@Override
	protected void finalize() {
		stop();
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		float azimuth = event.values[0];     // azimuth
		float pitch = event.values[1];     // pitch
		float roll = event.values[2];        // roll
		
		Side newSide = Side.TOP;

		if (pitch < -45 && pitch > -135) {
			// top side up
			newSide = Side.TOP;
		} else if (pitch > 45 && pitch < 135) {
			// bottom side up
			newSide = Side.BOTTOM;
		} else if (roll > 45) {
			// right side up
			newSide = Side.RIGHT;
		} else if (roll < -45) {
			// left side up
			newSide = Side.LEFT;
		}

		if (mListener != null && !newSide.equals(mCurrentSide)) {
			mListener.onOrientationChanged(newSide);
			mCurrentSide = newSide;
		}

	}
	

	public static Animation getRotationAnimation(Side oldside, Side newside) {
		RotateAnimation retval = null;
		if(oldside == null || newside == null){
			retval = new RotateAnimation(0, 0,
					Animation.RELATIVE_TO_SELF,
					0.5f,
					Animation.RELATIVE_TO_SELF,
					0.5f);
		} else {
			final int olddegree = toDegrees(oldside);
			int newdegree = toDegrees(newside);
			if(olddegree == 0 && newdegree == 270){
				newdegree = -90;
			} else if(olddegree == 270 && newdegree == 0){
				newdegree = 360;
			}
			retval = new RotateAnimation(olddegree,
					newdegree,
					Animation.RELATIVE_TO_SELF,
					0.5f,
					Animation.RELATIVE_TO_SELF,
					0.5f);

			retval.setDuration(500);
			retval.setFillEnabled(true);
			retval.setFillAfter(true);
			retval.setInterpolator(new DecelerateInterpolator());
		}

		return retval;
	}
	
	public static int toDegrees(Side side){
		int degree = 0;
		switch(side){
		case TOP:
			degree = 0;
			break;
		case RIGHT:
			degree = 90;
			break;
		case BOTTOM:
			degree = 180;
			break;
		case LEFT:
			degree = 270;
			break;
		}
		
		return degree;
		
		
	}

}