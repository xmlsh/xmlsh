/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.sh.core;

import java.io.PrintWriter;

import org.xmlsh.sh.shell.Shell;

public class BraceGroup extends CompoundCommandExpr {
	private CommandExpr		mCommand;

	public BraceGroup(CommandExpr command) {
		super(command.getName());
		mCommand = command;
		setLocation( command );
	}
	@Override
	public void print( PrintWriter out, boolean bExec ){
		out.print("{");
		mCommand.print(out, bExec);
		out.print("}");
	}
	/* (non-Javadoc)
	 * @see org.xmlsh.sh.core.Command#exec(org.xmlsh.sh.shell.Shell)
	 */
	@Override
	public int exec(Shell shell) throws Exception {

		shell.getEnv().saveIO();
		try {
			applyRedirect(shell);
			return shell.exec( mCommand);
		} finally {
			shell.getEnv().restoreIO();
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
