package org.xmlsh.core;

import java.io.Closeable;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.util.ReferenceCounter;

public class ReferenceCountedHandle<T extends Closeable > implements IHandle<T> {
    private static Logger mLogger = LogManager.getLogger();
	private final  ReferenceCounter mCounter ;
	private volatile T mObj;
	
	public ReferenceCountedHandle( T obj ) {
	  this( obj , new ReferenceCounter() );
	}

	public ReferenceCountedHandle( T obj , ReferenceCounter counter) {
		mLogger.entry( obj , counter );
		mCounter = counter;
		mObj = obj ;
	}
	@Override
	final public boolean release() throws IOException  {
		mLogger.entry( mCounter.value() );
		assert( mObj != null );
		if( mCounter.release() ) {

			mObj.close() ;
			mObj = null ;
			return true ;
		}
		return false ;
	} 

	public void addRef() { 
		mLogger.entry(mCounter);
		mCounter.addRef();
	}

	@Override
	final public T get() {
		mLogger.entry(mCounter);
		return mObj;
	}
	@Override
	public IHandle<T> newReference() {
		mLogger.entry(mCounter);
		mCounter.addRef();
		return this ;
	}

}
