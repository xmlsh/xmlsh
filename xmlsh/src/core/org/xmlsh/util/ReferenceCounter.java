package org.xmlsh.util;

import java.util.concurrent.atomic.AtomicInteger;

public class ReferenceCounter implements IReferenceCounted {

	protected 	final AtomicInteger	mRef = new AtomicInteger(1);

	@Override
	public void addRef() {
		mRef.incrementAndGet();
	}

	@Override
	public boolean release() {
		return mRef.decrementAndGet() <= 0;
	}

}
