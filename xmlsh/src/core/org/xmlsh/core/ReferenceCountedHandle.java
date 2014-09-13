package org.xmlsh.core;

import java.io.Closeable;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.util.ReferenceCounter;

public class ReferenceCountedHandle< T extends Closeable > implements  IManagedHandle<T> {
    private static Logger mLogger = LogManager.getLogger();
	private final  ReferenceCounter mCounter ;
	private volatile T mObj;
	
	public ReferenceCountedHandle( T obj ) {
	  this( obj , new ReferenceCounter() );
	}

	public String toString() {
		return mObj == null ?  "null" : mObj.toString() + "[" + mCounter.value() + "]" ;
	}
	public ReferenceCountedHandle( T obj , ReferenceCounter counter) {
		mLogger.entry( obj , counter );
		mCounter = counter;
		mObj = obj ;
	}
	/* (non-Javadoc)
	 * @see org.xmlsh.core.IManagedHandle#release()
	 */
	@Override
	final public boolean release() throws IOException  {
		mLogger.entry( this );
		assert( ! isNull() );
		if( mCounter.release() ) {
			mLogger.info("Closing : {} " , mObj );
			mObj.close();
			mObj = null ;
			return true ;
		}
		return false ;
	} 

	/* (non-Javadoc)
	 * @see org.xmlsh.core.IManagedHandle#addRef()
	 */
	@Override
	public void addRef() { 

		assert( ! isNull() );
		mLogger.entry(this);
		mCounter.addRef();
	}
	
	public int getRefCount() {
		return mCounter.value();
	}

	@Override
	final public T get() {
		mLogger.entry(mCounter);
		return mObj;
	}
	
	@Override
	final public boolean isNull() {
		return mObj == null;
	}
}
