package com.devsmart.android;

import java.lang.reflect.Method;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.IInterface;
import android.util.Log;

public class ServiceBinder<T extends IInterface> {

	private Class<? extends Service> mServiceClass;
	private Class<? extends IInterface> mInterfaceClass;
	private T mServiceInterface;
	private Context mContext;
	private Runnable mOnServiceReady;
	
	public ServiceBinder(Context context, 
			Class<? extends Service> serviceClass, 
			Class<? extends IInterface> interfaceClass,
			Runnable onServiceReady) {
		mContext = context;
		mServiceClass = serviceClass;
		mInterfaceClass = interfaceClass;
		mOnServiceReady = onServiceReady;
		Intent serviceIntent = new Intent(context, mServiceClass);
		context.bindService(serviceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
	}
	
	public void unBind(){
		mContext.unbindService(mServiceConnection);
	}
	
	public T getServiceInterface() {
		return mServiceInterface;
	}
	
	private ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			try {
				Method asInterface = null;
				for(Class c : mInterfaceClass.getClasses()) {
					String className = c.getSimpleName();
					if(className.equals("Stub")) {
						asInterface = c.getMethod("asInterface", IBinder.class);
						break;
					}
				}
				
				mServiceInterface = (T)asInterface.invoke(null, service);
				if(mOnServiceReady != null) {
					mOnServiceReady.run();
				}
			
			}catch(Exception e) {
				Log.e(ServiceBinder.class.getName(), "Unable to bind to service", e);
			}
			
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			
		}
		
	};
}
