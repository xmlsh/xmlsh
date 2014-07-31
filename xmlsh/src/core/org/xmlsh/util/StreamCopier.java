/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.util;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamCopier extends AbstractCopier
{
	private 	static 	Logger	mLogger  = LogManager.getLogger(StreamCopier.class);
	private		volatile InputStream		mIn;
	private		volatile OutputStream	mOut;

	protected void finalize() {
		// Just in case - remove references to mIn and mOut
		close();
	}
	
	public StreamCopier( String name , InputStream in , OutputStream out  )
	{
		super(name);
		mIn = in;
		mOut = out;
	}
	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() 
	{
		mLogger.debug("running: " + getName() );

		try {
			Util.copyStream(mIn, mOut);
		} catch (IOException e) {
			mLogger.warn("IOException copying streams: " + getName()  ,e);
        } finally {
			mLogger.debug("run close(): " + getName()  );
			close();
		}
		
	}
	@Override
	public void closeOut() {
		if(  mOut != null )
			synchronized (this) {
				Util.safeClose(mOut);
				mOut = null ;
            }
	}
	@Override
	public void closeIn() {
		if(  mIn != null )
			synchronized (this) {
				Util.safeClose(mIn);
				mIn = null ;
            }
	}
	@Override
    public
    void close( )
    {
	    closeIn();
	    closeOut();
	    
    }
	 
	 
	 
}
//
//
//Copyright (C) 2008-2014    David A. Lee.
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
