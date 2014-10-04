package org.xmlsh.core;

import java.io.Closeable;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.builtin.commands.exit;
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
		mLogger.entry(obj, counter);
		mCounter = counter ;
		mLogger.entry( obj  );
		mObj = obj ;
		mLogger.exit();
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
			return mLogger.exit(true) ;
		}
		return mLogger.exit(false) ;
	} 

	@Override
	public void addRef() { 

		
		mLogger.entry();
		assert( ! isNull() );
		mLogger.entry(this);
		mLogger.exit(mCounter.increment());
	}
	
	@Override
	final public T get() {
		return mObj;
	}
	
	@Override
	final public boolean isNull() {
		return mObj == null;
	}
	@Override
	public int getRefCount() {
		return mCounter.getRefCount();
	}
}
