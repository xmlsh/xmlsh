/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.sh.core;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.xmlsh.sh.shell.Shell;
import org.xmlsh.sh.shell.ShellThread;
import org.xmlsh.util.PipedStream;

public class Pipeline extends Command {
	
	private		ArrayList<Command>	mList = new ArrayList<Command>();
	
	
	private boolean mBang;
	public Pipeline( boolean bBang ) {
		mBang = bBang;
	}

	/**
	 * @param bang the bang to set
	 */
	public void setBang(boolean bang) {
		mBang = bang;
	}

	public boolean isBang() { return mBang ; }

	/**
	 * @param e
	 * @return
	 * @see java.util.ArrayList#add(java.lang.Object)
	 */
	public boolean add(Command e) {
		return mList.add(e);
	}
	public void print( PrintWriter out ){
		if( isBang() )
			out.print("! ");
		
		int n = mList.size();
		for (Command c : mList) {
			c.print(out);
			if( n-- > 1 )
				out.print("|");
			
			
		}
	}

	/* (non-Javadoc)
	 * @see org.xmlsh.sh.core.Command#exec(org.xmlsh.core.XEnvironment)
	 */
	@Override
	public int exec(Shell shell) throws Exception {
		
		
		int		ncmds = mList.size();
		
		
		ArrayList<ShellThread>  threads = new ArrayList<ShellThread>();
		
		OutputStream rightOut = null;
		
		PipedStream	pipes[] = PipedStream.getPipes(ncmds-1);
		
		
		/*
		 * Setup all but first command as seperate threads
		 */
		for( int pi = ncmds-1 ; pi > 0 ; pi-- ){
			Shell sh = shell.clone();	// clone shell for execution
			Command c = mList.get(pi);
			
			
			// Set input to pipe
			sh.getEnv().setStdin( pipes[pi-1].getInput() );
			
			// all but last command set output to pipe
			if( pi < ncmds-1)
				sh.getEnv().setStdout(pipes[pi].getOutput());
				
			
			ShellThread sht = new ShellThread( sh , null ,   c );

			sht.start();
			threads.add(sht);
		}
		
		if( ncmds > 1 )
			shell.pushEnv();
		try 
		{
			if( ncmds > 1 )
				shell.getEnv().setStdout(pipes[0].getOutput());
			Command c = mList.get(0);
			
			int ret = shell.exec(c);

			if( ncmds > 1 )
				pipes[0].getOutput().close();
			
			for( ShellThread sht : threads )
				sht.join();

			return mBang ? (ret == 0 ? 1 : 0 ) : ret ;
			
			
		} finally {
			if( ncmds > 1 )
				shell.popEnv();
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
