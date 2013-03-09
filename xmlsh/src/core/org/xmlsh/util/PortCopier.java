/**
 * $Id: StreamCopier.java 21 2008-07-04 08:33:47Z daldei $
 * $Date: 2008-07-04 04:33:47 -0400 (Fri, 04 Jul 2008) $
 *
 */

package org.xmlsh.util;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.xmlsh.core.InputPort;
import org.xmlsh.sh.shell.SerializeOpts;

public class PortCopier extends Thread
{
	private 	static 	Logger	mLogger  = LogManager.getLogger(PortCopier.class);
	private		InputPort	mIn;
	private		OutputStream	mOut;
	private		boolean			mCloseOut;
	private		SerializeOpts mOpts;
	public PortCopier( InputPort in , OutputStream out , SerializeOpts opts ,  boolean closeOut )
	{
		mIn = in;
		mOut = out;
		mOpts = opts;
		
		mCloseOut = closeOut;
		
	}
	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() 
	{

		try {
			
			mIn.copyTo(mOut,mOpts);
			
			//mIn.close();
			//mOut.close();
		} catch (Exception e) {
			mLogger.warn("Exception copying streams",e);
		} finally {
			if( mCloseOut )
				try {
					mOut.close();
				} catch (IOException e) {
					mLogger.warn("IOException closing streams",e);
				}
		}
		
	}
}
//
//
//Copyright (C) 2008-2013    David A. Lee.
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
