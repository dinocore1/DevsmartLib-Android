package com.devsmart.android.mathutils;

public class Quotient {
	
	public int numerator;
	public int denominator;
	
	public Quotient(int n, int d) {
		numerator = n;
		denominator = d;
	}
	
	public double getDoubleValue() {
		return (double)numerator / (double)denominator;
	}
	
	public Quotient inverse() {
		return new Quotient(denominator, numerator);
	}

}
