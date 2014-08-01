package org.xmlsh.core;

import org.xmlsh.util.ReferenceCounter;

import java.io.Closeable;
import java.io.IOException;

public class ReferenceCountedHandle<T extends Closeable > implements IHandle<T> {

	private final  ReferenceCounter mCounter ;
	private volatile T mObj;
	public ReferenceCountedHandle( T obj , ReferenceCounter counter) {
		mCounter = counter;
		mObj = obj ;
	}
	@Override
	final public boolean release() throws IOException  {
		if( mCounter.release() ) {
			mObj.close() ;
			mObj = null ;
			return true ;
		}
		return false ;
	} 

	public void addRef() {
		mCounter.addRef();
	}

	@Override
	final public T get() {
		return mObj;
	}
	@Override
	public IHandle<T> newReference() {
		mCounter.addRef();
		return this ;
	}

}
