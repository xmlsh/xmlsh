package org.xmlsh.util;

import java.util.concurrent.atomic.AtomicInteger;

public class ReferenceCounter  {

	protected 	final AtomicInteger	mRef = new AtomicInteger(1);

	public void addRef() {
		mRef.incrementAndGet();
	}

	public boolean release() {
		return mRef.decrementAndGet() <= 0;
	}

}
