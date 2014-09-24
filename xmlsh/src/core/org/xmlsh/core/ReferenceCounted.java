package org.xmlsh.core;

import java.io.Closeable;
import java.io.IOException;

import org.xmlsh.util.IManagable;
import org.xmlsh.util.ReferenceCounter;

@SuppressWarnings("serial")
public abstract class ReferenceCounted extends ReferenceCounter implements IManagable, IReferenceCounted , Closeable {

	private boolean bClosed = true ;
	public ReferenceCounted() {
		super();
		bClosed = true ;

	}

	/* (non-Javadoc)
	 * @see org.xmlsh.core.IManagedHandle#release()
	 */
	@Override
	final public boolean release() throws IOException  {
		if( super.decrement() <= 0  ) {
			doClose();
			return true ;
		}
		return false ;
	} 

	@Override
	public void addRef() { 

		super.increment();
	}
	
	@Override
	public final void close() throws IOException {
		if( ! bClosed )
			  doClose();
		bClosed = true ;
	}

}