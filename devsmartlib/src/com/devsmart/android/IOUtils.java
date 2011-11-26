package com.devsmart.android;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.os.Handler;

public class IOUtils {
	
	public static final ExecutorService sIOThreads = Executors.newScheduledThreadPool(3);
	
	public static byte[] toByteArray(InputStream in) throws IOException{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		pumpStream(in, out, null);
		return out.toByteArray();
	}
	
	public static void writeByteArrayToFile(File file, byte[] bytes) throws IOException {
		FileOutputStream fout = new FileOutputStream(file);
		try {
			fout.write(bytes);
		} finally {
			fout.close();
		}
	}
	
	public static void copyFile(File src, File dest) throws IOException {
		InputStream in = new FileInputStream(src);
		OutputStream out = new FileOutputStream(dest);
		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0) {
		   out.write(buf, 0, len);
		}
		in.close();
		out.close();

	}
	
	public interface DataProgressCallback {
		public void onDataProgress(int bytesWritten);
	}
	
	public static void pumpStream(InputStream in, OutputStream out, DataProgressCallback callback) throws IOException {
		byte[] buff = new byte[1024];
		int bytesRead = 0;
		while ((bytesRead = in.read(buff, 0, buff.length)) != -1) {
			out.write(buff, 0, bytesRead);
			if(callback != null){
				callback.onDataProgress(bytesRead);
			}
		}
		out.close();
		in.close();
	}

	public static abstract class BackgroundTask implements Runnable{
		public Handler mHandler = new Handler();
		
		private Runnable mOnFinished = new Runnable() {
			@Override
			public void run() {
				onFinished();
			}
		};
		
		public void onFinished() {
			//default do nothing
		}
		
		public final void finish() {
			mHandler.post(mOnFinished);
		}

	}
	
	public static InputStream getPhoneLogs() throws IOException, InterruptedException {
		ProcessBuilder builder = new ProcessBuilder("logcat", "-d");
		builder.redirectErrorStream(true);
		Process process = builder.start();
		//process.waitFor();
		return process.getInputStream();
	}
	

}
