package org.xmlsh.core;

import java.io.Closeable;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.util.ReferenceCounter;

@SuppressWarnings("serial")
public class ReferenceCountedHandle< T extends IHandleable & Closeable >  implements   IReferencedCountedHandle<T> {
    private static Logger mLogger = LogManager.getLogger();
    private volatile ReferenceCounter mCounter;
	private volatile T mObj;
	
	@Override
	public String toString() {
		return mObj == null ?  "null" : mObj.toString()  + super.toString();
	}
	public ReferenceCountedHandle( T obj ) {
      this( obj , new ReferenceCounter() ); 
	}
	
	public ReferenceCountedHandle( T obj , ReferenceCounter counter ) {
		mCounter = counter ;
		mLogger.entry( obj  );
		mObj = obj ;
	}
	
	/* (non-Javadoc)
	 * @see org.xmlsh.core.IManagedHandle#release()
	 */
	@Override
	final public boolean release() throws IOException  {
		mLogger.entry( this );
		assert( ! isNull() );
		if( mCounter.decrement() <= 0  ) {
			mLogger.info("Closing : {} " , mObj );
			mObj.close();
			mObj = null ;
			return true ;
		}
		return false ;
	} 

	@Override
	public void addRef() { 

		assert( ! isNull() );
		mLogger.entry(this);
		mCounter.increment();
	}
	
	@Override
	final public T get() {
		mLogger.entry();
		return mObj;
	}
	
	@Override
	final public boolean isNull() {
		return mObj == null;
	}
	@Override
	public int getRefCount() {
		// TODO Auto-generated method stub
		return mCounter.getRefCount();
	}
}
