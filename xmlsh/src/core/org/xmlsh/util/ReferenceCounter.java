package org.xmlsh.util;

import java.util.concurrent.atomic.AtomicInteger;

import org.xmlsh.core.IHandleable;

@SuppressWarnings("serial")
public class ReferenceCounter extends AtomicInteger {

	public ReferenceCounter() {
		super(1);
	}
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
