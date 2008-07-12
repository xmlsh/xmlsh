/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.sh.shell;

import java.io.File;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.xmlsh.sh.core.Command;

public class ShellThread extends Thread {
	
	@SuppressWarnings("unused")
	private static Logger mLogger = LogManager.getLogger( ShellThread.class);

	private Shell 		mShell ;
	private Command 	mCmd;
	private	 File		mIniitalCD;
	private		Shell	mParent = null;

	public ShellThread(Shell shell , Shell parent ,  Command cmd ) {
		mShell = shell;
		mParent = parent ;
		mCmd = cmd;
		mIniitalCD = shell.getCurdir();

	}

	
	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() 
	{
		try {
			mShell.setCurdir(mIniitalCD); // Populate the current directory in this thread
			
			
			mCmd.exec(mShell);
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			
			mShell.close();
			if( mParent != null )
				mParent.removeJob( this );
		}
		
	}

	

	
}
//
//
//Copyright (C) 2008, David A. Lee.
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
