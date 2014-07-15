/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.sh.shell;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import org.xmlsh.sh.core.Command;
import org.xmlsh.util.Util;
import org.xmlsh.xpath.ShellContext;

import java.io.File;

public class ShellThread extends Thread {
	
	@SuppressWarnings("unused")
	private static Logger mLogger = LogManager.getLogger( ShellThread.class);

	private volatile Shell 	mShell ;
	private Command 	mCommand;
	private	 File		mIniitalCD;

	public ShellThread(ThreadGroup threadGroup, Shell shell ,  String name ,Command cmd ) {
		super(threadGroup == null ? shell.getThreadGroup() : threadGroup , simpleName(name,cmd ) );
		mShell = shell;
		mCommand = cmd;
		mIniitalCD = shell.getCurdir();

	}

	
	private static String simpleName(String name, Command cmd)
    {
		if(  Util.isBlank(name))
			name =cmd.getName();
		if( Util.isBlank(name))
			name = "<shell thread>";
	    return name ;
			
    }


	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() 
	{
		try {
			ShellContext.set( mShell );
			mShell.setCurdir(mIniitalCD); // Populate the current directory in this thread
			mCommand.exec(mShell);
		
		} catch (Exception e) {
			// mShell.printErr("Exception running: " + mCommand.toString(true) + "\n" +  e.toString() );
			mLogger.error("Exception running command: " + mCommand.toString(false) , e );
		
		} finally {
			ShellContext.set(null);
			mShell.close();
		}
		
	}

	public Command getCommand(){
		return mCommand ;
	}


	public Shell getShell()
    {
	    return mShell;
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
