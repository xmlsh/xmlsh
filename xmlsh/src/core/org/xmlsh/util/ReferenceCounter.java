package org.xmlsh.util;

import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("serial")
public class ReferenceCounter extends AtomicInteger {

	public ReferenceCounter() {
		super(1);
	}
	@Override
	public String toString(){ return "[" + getRefCount() + "]";}

	public int getRefCount() {
		return super.get();
	}
	public void initRefCount(int count) {
		super.set(count);
	}
	public int decrement() {
		return super.decrementAndGet();
	}
	public int increment() {
		return super.incrementAndGet();
	}

}
