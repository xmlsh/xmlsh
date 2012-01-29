/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class StreamCopier extends Thread
{
	private 	static 	Logger	mLogger  = LogManager.getLogger(StreamCopier.class);
	private		InputStream		mIn;
	private		OutputStream	mOut;
	private		boolean			mCloseOut;


	public StreamCopier( InputStream in , OutputStream out ,  boolean closeOut )
	{
		mIn = in;
		mOut = out;
		
		mCloseOut = closeOut;
		
	}
	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() 
	{

		try {
			Util.copyStream(mIn, mOut);
			//mIn.close();
			//mOut.close();
		} catch (IOException e) {
			mLogger.warn("IOException copying streams",e);
		} finally {
			if( mCloseOut )
				closeOut();
		}
		
	}
	public void closeOut() {
		try {
			mOut.close();
		} catch (IOException e) {
			mLogger.warn("IOException closing streams",e);
		}
	}
	public void closeIn() {
		try {
			mIn.close();
		} catch (IOException e) {
			mLogger.warn("IOException closing streams",e);
		}
	}
	 
	 
	 
}
//
//
//Copyright (C) 2008,2009,2010,2011,2012 , David A. Lee.
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
