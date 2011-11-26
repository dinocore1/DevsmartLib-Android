package com.devsmart.android;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.util.Log;

public class DbUtils {
	
	public static String getStringCursorValue(Cursor cursor, String columnName, String defaultValue) {
		String retval = null;
		int columnNum = cursor.getColumnIndex(columnName);
		if(columnNum == -1){
			retval = defaultValue;
		} else {
			retval = cursor.getString(columnNum);
		}
		return retval;
	}
	
	public static Long getLongCursorValue(Cursor cursor, String columnName, Long defaultValue){
		Long retval = defaultValue;
		int columnNum = cursor.getColumnIndex(columnName);
		if(columnNum == -1){
			retval = defaultValue;
		} else {
			retval = cursor.getLong(columnNum);
		}
		return retval;
	}

	public static Integer getIntCursorValue(Cursor cursor, String columnName, Integer defaultvalue) {
		Integer retval = defaultvalue;
		int columnNum = cursor.getColumnIndex(columnName);
		if(columnNum == -1){
			retval = defaultvalue;
		} else {
			retval = cursor.getInt(columnNum);
		}
		return retval;
	}
	
	public static String lookupSingleStringValue(Context context, Uri uri, String columnName, String defaultValue) {
		String retval = defaultValue;
		Cursor cursor = context.getContentResolver().query(uri, new String[]{columnName}, null, null, null);
		try {
			if(cursor.moveToFirst()){
				retval = cursor.getString(0);
			}
		} finally {
			cursor.close();
		}
		
		return retval;
	}
	
	public static String lookupSingleStringValue(SQLiteDatabase db, String columnName, String table, String selection, String[] selectionArgs, String defaultValue) {
		String retval = defaultValue;
		Cursor c = db.query(table, new String[]{columnName}, selection, selectionArgs, null, null, null);
		try {
			if(c.moveToFirst()){
				retval = c.getString(0);
			}
		} finally {
			c.close();
		}
		return retval;
	}
	
	public static void executeAssetSQL(SQLiteDatabase db, Resources resources, String assetPath){
		db.beginTransaction();
		try {
			String createSQL = StringUtils.loadAssetString(resources, assetPath);
			
			for(String sql : createSQL.split(";")){
				sql = sql.trim();
				if(sql.length() > 0){
					db.execSQL(sql);
				}
			}
			db.setTransactionSuccessful();
		} catch (IOException e) {
			Log.e(DbUtils.class.getName(), "Error executing sql statement", e);
		} finally {
			db.endTransaction();
		}
	}
	
	public static long updateOrInsert(SQLiteDatabase db, String table, ContentValues values, String selection, String[] selectionArgs) {
		long retval = -1;
		db.beginTransaction();
		try {
			retval = db.update(table, values, selection, selectionArgs);
			if(retval == 0){
				retval = db.insert(table, null, values);
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		return retval;
	}
	
	/*
	public static long updateOrReplaceRow(SQLiteDatabase db, ContentValues values, String table, String selection, String[] selectionArgs) {
		String query = "UPDATE OR REPLACE %s SET ";
		
		Iterator<Entry<String, Object>> it = values.valueSet().iterator();
		while(it.hasNext()){
			Entry<String, Object> entry = it.next();
			query += String.format("%s = '%s'", entry.getKey(), entry.getValue().toString());
			if(it.hasNext()){
				query += ", ";
			}
		}
		
		query += " WHERE";
		
		selection.rep
		String.format(selection, selectionArgs));

	}
	*/

}
