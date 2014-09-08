/**
 * $Id: StreamCopier.java 21 2008-07-04 08:33:47Z daldei $
 * $Date: 2008-07-04 04:33:47 -0400 (Fri, 04 Jul 2008) $
 *
 */

package org.xmlsh.util;

import java.io.OutputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.core.InputPort;
import org.xmlsh.sh.shell.SerializeOpts;

public class PortCopier extends AbstractCopier
{
	private 	static 	Logger	mLogger  = LogManager.getLogger();
	private		volatile    InputPort	mIn;
	private		volatile    OutputStream	mOut;

	private		SerializeOpts mOpts;

	@Override
	protected void finalize() {
		// Just in case - remove references to mIn and mOut
		close();
	}

	public PortCopier( String name, InputPort in , OutputStream out , SerializeOpts opts  )
	{
		super( name );
		mIn = in;
		mOut = out;
		mOpts = opts;


	}
	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() 
	{

		try {
			mIn.copyTo(mOut,mOpts);
		}catch (Exception e) {
			mLogger.warn("Exception copying streams",e);
		} finally {
			mLogger.debug("run close(): " + getName()  );
			close();
		}

	}
	@Override

	public void close() 
	{
		if( mIn != null || mOut != null ) { 
			closeIn();
			closeOut();

			if( this != Thread.currentThread() ) {
				Thread.yield();
				if( this.isAlive() ) {
					try {
						this.interrupt();  
						Thread.yield();
					} catch( SecurityException e ) {
						mLogger.debug("Security exception interrupting copy thread");
					}
					if( this.isAlive() )
						return ;
				}
				try {
					this.join(1);
				} catch (InterruptedException e) {
					mLogger.debug("Intterrupt exception joining copy thread",e);

				}
			}

		}

	}

	@Override
	public void closeOut()
	{
		if( mOut != null ) {
			synchronized (this) {
				Util.safeClose(mOut);
				mOut = null ;
			} 
		}
	}
	@Override


	public void closeIn()
	{
		// Cannot close  ports - they are self-managed
		mIn = null ;
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
