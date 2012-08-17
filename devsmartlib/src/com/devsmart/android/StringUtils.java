package com.devsmart.android;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import android.content.res.Resources;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.ClickableSpan;
import android.widget.TextView;

public class StringUtils {
	
	  public static String loadRawResourceString(Resources res, int resourceId) throws IOException {
			InputStream is = res.openRawResource(resourceId);
			return loadString(is);
	  }
	  
	  public static String loadAssetString(Resources res, String filename) throws IOException {
			InputStream is = res.getAssets().open(filename);
			return loadString(is);
	  }
	  
	  public static String loadString(InputStream is) throws IOException {
		  StringBuilder builder = new StringBuilder();
		  InputStreamReader reader = new InputStreamReader(is);
		  char[] buf = new char[1024];
		  int numRead=0;
		  while((numRead=reader.read(buf)) != -1){
			  builder.append(buf, 0, numRead);
		  }
		  return builder.toString();
	  }
	  
	  public static String[] union(String[] a, String[] b) {
		  LinkedList<String> retval = new LinkedList<String>();
		  for(int i=0;i<a.length;i++){
			  retval.add(a[i]);
		  }
		  
		  for(int i=0;i<b.length;i++){
			  if(!retval.contains(b[i])){
				  retval.add(b[i]);
			  }
		  }
		  
		  String[] retarray = new String[retval.size()];
		  retval.toArray(retarray);
		  return retarray;
		  
	  }
	  
	  public static String[] intersection(String[] a, String[] b){
		  List<String> blist = Arrays.asList(b);
		  LinkedList<String> retval = new LinkedList<String>();
		  
		  for(int i=0;i<a.length;i++){
			  if(blist.contains(a[i])){
				  retval.add(a[i]);
			  }
		  }
		  
		  String[] retarray = new String[retval.size()];
		  retval.toArray(retarray);
		  return retarray;
	  }
	  
	  public abstract static class ClickSpan extends ClickableSpan {
		  
	  }
	  public static void clickify(TextView view, final String clickableText, 
			    final ClickSpan span) {

			    CharSequence text = view.getText();
			    String string = text.toString();

			    int start = string.indexOf(clickableText);
			    int end = start + clickableText.length();
			    if (start == -1) return;

			    if (text instanceof Spannable) {
			        ((Spannable)text).setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			    } else {
			        SpannableString s = SpannableString.valueOf(text);
			        s.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			        view.setText(s);
			    }

			    MovementMethod m = view.getMovementMethod();
			    if ((m == null) || !(m instanceof LinkMovementMethod)) {
			        view.setMovementMethod(LinkMovementMethod.getInstance());
			    }
			}

}
