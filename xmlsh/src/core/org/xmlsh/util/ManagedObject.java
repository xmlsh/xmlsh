/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.util;

import java.io.Closeable;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.core.IReleasable;


/*
 * Default implementation of a managed object
 * Simple close on release 
 * 
 */
public abstract class ManagedObject<T extends IManagable >  implements  IManagable , Closeable, IReleasable  {

	private static Logger  mLogger = LogManager.getLogger();
	private volatile boolean bClosed = true ;

	protected boolean isClosed() {
		return bClosed;
	}


	protected ManagedObject()
	{
		bClosed = false ;
		mLogger.entry(this);
	}
	
	@Override
	protected void finalize() throws IOException
	{
		if( ! bClosed )
		 close();
	}


	@Override
	public synchronized boolean release() throws IOException  {
		mLogger.entry(this);
		if( ! bClosed )
		  close();

		return mLogger.exit(true);
	}
	@Override
	public final void close() throws IOException {
		if( ! bClosed )
			  doClose();
		bClosed = true ;
	}
}



//
//
//Copyright (C) 2008-2014 David A. Lee.
//
//The contents of this file are subject to the "Simplified BSD License" (the "License");
//you may not use this file except in compliance with the License. You may obtain a copy of the
//License at http://www.opensource.org/licenses/bsd-license.php 
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied.
//See the License for the specific language governing rights and limitations under the License.
//
//The Original Code is: all this file.
//
//The Initial Developer of the Original Code is David A. Lee
//
//Portions created by (your name) are Copyright (C) (your legal entity). All Rights Reserved.
//
//Contributor(s): none.
//
