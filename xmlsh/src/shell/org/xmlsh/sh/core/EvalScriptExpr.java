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

import java.io.PrintWriter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.core.ScriptCommand;
import org.xmlsh.core.ScriptCommand.SourceMode;
import org.xmlsh.core.ScriptSource;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.Util;

public class EvalScriptExpr extends CommandExpr {
	private String			mCommand;

   static Logger mLogger = LogManager.getLogger();
	@Override
	public	boolean		isSimple() { return true ; }

	public EvalScriptExpr( String command)
	{
		super( Util.simpleName( command, "<script>" ));
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
	public int exec(Shell sh) throws Exception {
		mLogger.entry(sh);
		// Commands run in a sub shell

		ScriptSource ss = new ScriptSource( getName() , mCommand  );

		// Auto close script and shell 
		try ( Shell shell = sh.clone();
			  ScriptCommand cmd = new ScriptCommand( ss , SourceMode.SOURCE , shell.getLocation() , shell.getModule() );
			){  
			return mLogger.exit(cmd.run(shell, "", null));
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
