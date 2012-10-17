/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.sh.core;

import java.io.PrintWriter;

import org.xmlsh.sh.shell.Shell;

public class SubShell extends CompoundCommand {
	private Command		mCommand;
	public SubShell( Command c )
	{
		mCommand = c;
		setLocation(c);
	}
	public void print( PrintWriter out, boolean bExec ){
		out.print("(");
		mCommand.print(out, bExec);
		out.print(")");
	}
	
	
	/* (non-Javadoc)
	 * @see org.xmlsh.sh.core.Command#exec(org.xmlsh.sh.shell.Shell)
	 */
	@Override
	public int exec(Shell shell) throws Exception {

		/*
		 * Save the IO environment then redirect before cloning
		 * the shell so that port redirections take place in the parent shell
		 */
		shell.getEnv().saveIO();
		try {
			applyRedirect(shell);
			
			// Clone shell to run command inside new shell
			Shell subshell = shell.clone();
			try {
				return subshell.exec( mCommand);
			} finally {
				subshell.close();
			}
		} finally {
			shell.getEnv().restoreIO();
		}
		
		
		
	}
	
	

}
//
//
//Copyright (C) 2008-2012  David A. Lee.
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
