package org.xmlsh.util;

import java.util.concurrent.atomic.AtomicInteger;

public class ReferenceCounter  {

	protected 	final AtomicInteger	mRef = new AtomicInteger(1);
	public String toString(){ return mRef.toString(); }
	public void addRef() {
		mRef.incrementAndGet();
	}

	public boolean release() {
		return mRef.decrementAndGet() <= 0;
	}
	// get the current value - not thread safe 
	public int value() {
		return mRef.get();
	}

}
