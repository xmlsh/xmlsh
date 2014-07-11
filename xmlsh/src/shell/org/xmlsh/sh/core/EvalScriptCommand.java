/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.sh.core;


/*
 * Evaluate a single string as a shell script encpuslated into a command
 * Used by $<(cmd)>
 * 
 */

import org.xmlsh.core.ICommand;
import org.xmlsh.core.ScriptCommand;
import org.xmlsh.sh.shell.Shell;

import java.io.PrintWriter;

public class EvalScriptCommand extends Command {
	private String			mCommand;
	
	public	boolean		isSimple() { return true ; }

	
	public EvalScriptCommand( String command)
	{
		mCommand = command;
		
	}
	
	

	/* (non-Javadoc)
	 * @see org.xmlsh.sh.core.Command#print(java.io.PrintStream)
	 */
	@Override
	public void print(PrintWriter out, boolean bExec) {

		out.print( mCommand );

	}



	/* (non-Javadoc)
	 * @see org.xmlsh.sh.core.Command#exec(org.xmlsh.core.XEnvironment)
	 */
	@Override
	public int exec(Shell shell) throws Exception {
		
		// Commands run in a sub shell
		
		
		shell = shell.clone();
		try {
		
			
			ICommand cmd = new ScriptCommand( mCommand , shell.getSerializeOpts() );
			
			if( cmd == null ){
				SourceLocation loc = getLocation();
				if( loc != null )
					shell.printErr(loc.toString());
				
				shell.printErr(mCommand + ": not found");
				return 1;
				
			}	

			
			
			return cmd.run(  shell, "", null );

			
		} finally {
			shell.close();
		}
		
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
