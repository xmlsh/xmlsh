/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.sh.core;

import java.io.PrintWriter;
import java.util.List;

import org.xmlsh.core.CommandFactory;
import org.xmlsh.core.ICommand;
import org.xmlsh.core.XIOEnvironment;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.Util;

public class SimpleCommand extends Command {
	private CommandPrefix  mPrefix;
	private String			mCommand;
	private CommandSuffix	mSuffix;
	
	
	
	public SimpleCommand(CommandPrefix prefix , String command, CommandSuffix suffix )
	{
		mPrefix = prefix;
		mCommand = command;
		mSuffix = suffix ;
		
	}
	
	

	/* (non-Javadoc)
	 * @see org.xmlsh.sh.core.Command#print(java.io.PrintStream)
	 */
	@Override
	public void print(PrintWriter out, boolean bExec) {
		if( mPrefix != null )
			mPrefix.print(out);
		out.print( mCommand );
		if( mSuffix  != null ){
			out.print(" ");

			mSuffix.print(out);
		}

	}



	/* (non-Javadoc)
	 * @see org.xmlsh.sh.core.Command#exec(org.xmlsh.core.XEnvironment)
	 */
	@Override
	public int exec(Shell shell) throws Exception {
		
		
		
		if(  Util.isEmpty( mCommand ))
			return execNull( shell );
		
		List<XValue>	cmdLine = mSuffix.toCmdLine(shell, mCommand);
		
		String cmdName = cmdLine.remove(0).toString();
		
		ICommand cmd = CommandFactory.getInstance().getCommand( shell , cmdName );
		
		if( cmd == null ){
			shell.printErr(mCommand + ": not found");
			return 1;
			
		}
		
		XIOEnvironment saved_io = null ;
		Shell		   saved_shell = null;
		
		
		/*
		 * If there is a prefix then clone the shell, otherwise just clone the IO
		 */
		if( mPrefix == null )
			saved_io = shell.getEnv().saveIO();
		else {
			saved_shell = shell ;
			shell = shell.clone();
		}
			
		try {
		
		
			if( mPrefix != null )
				mPrefix.exec( shell );
			
			
		
			mSuffix.exec( shell );

			
			return cmd.run(  shell, cmdName , cmdLine );

			
		} finally {
			if( saved_io != null )
				shell.getEnv().restoreIO(saved_io);
			else
			if( saved_shell != null )
				shell.close();
		}
		
	}



	private int execNull(Shell shell) throws Exception {

		if( mPrefix != null )
			mPrefix.exec( shell );
		
		
		if( mSuffix != null )
			mSuffix.exec( shell );
		
		return 0;
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
