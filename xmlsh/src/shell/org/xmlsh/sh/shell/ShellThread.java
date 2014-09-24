/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.sh.shell;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.sh.core.ICommandExpr;
import org.xmlsh.sh.core.IExpression;
import org.xmlsh.util.Util;
import org.xmlsh.xpath.ThreadLocalShell;

public class ShellThread extends Thread implements Closeable {

	@SuppressWarnings("unused")
	private static Logger mLogger = LogManager.getLogger( ShellThread.class);

	private volatile Shell 	mShell ;
	private ICommandExpr 	mCommand;
	private	 File		mIniitalCD;

	public ShellThread(ThreadGroup threadGroup, Shell shell ,  String name ,ICommandExpr cmd ) {
		super(threadGroup == null ? shell.getThreadGroup() : threadGroup , simpleName(name,cmd ) );
		mShell = shell;
		mCommand = cmd;
		mIniitalCD = shell.getCurdir();

	}


	private static String simpleName(String name, IExpression cmd)
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

			Shell shell = null ;
			synchronized( this ) {
				shell = mShell ;
				if( shell == null ) {
					mLogger.error("Shell is closed before run(): " + describe() );
					return ;
				}	
			}

			ThreadLocalShell.set( shell );
			shell.setCurdir(mIniitalCD); // Populate the current directory in this thread
			mCommand.exec(shell);

		} catch (Exception e) {
			// mShell.printErr("Exception running: " + mCommand.toString(true) + "\n" +  e.toString() );
			mLogger.error("Exception running command: " + mCommand.describe(false) , e );

		} finally {
			try {
				close();
			} catch (IOException e) {
				mLogger.error("Exception closing self: " + describe()  , e );

			}

		}

	}

	public IExpression getCommand(){
		return mCommand ;
	}


	public Shell getShell()
	{
		return mShell;
	}


	// Attempt to nicely close this job 
	@Override
	public void close() throws IOException 
	{
		if( mShell == null )
			return ;
		synchronized(this) {
			if( mShell == null )
				return ;
			ThreadLocalShell.set(null);
			mShell.close();
			mShell = null ;
		}
	}


	public String describe()
	{
		return "[" + getId() + "] : " + getName();
	}

	/*
	 * Attempt to force a shutdown of this shell
	 * Intended to be called from a external thread to try to force the shell to quit
	 */
	public boolean shutdown( boolean force , long waitTime ) throws IOException {
		Shell sh = mShell ;
		if(  sh == null )
			return 	true ;

		sh.shutdown(force,waitTime);
		Thread.yield();
		return mShell == null ;
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
