package com.devsmart.android;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Random;

public class RandomIterator  {
	
	public static <T> Iterator<T> createRandomIterator(Collection<T> collection){
		return createRandomIterator(collection, new Random());
	}
	
	public static <T> Iterator<T> createRandomIterator(Collection<T> collection, Random r){
		ArrayList<T> list = new ArrayList<T>(collection);
		Collections.shuffle(list, r);
		return list.iterator();
	}
	
	

}
