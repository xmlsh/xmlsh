/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.sh.core;

import java.io.PrintWriter;

import org.xmlsh.core.XIOEnvironment;
import org.xmlsh.sh.shell.Shell;

public class UntilClause extends CompoundCommand {
	private		Command		mUntil;
	private		Command		mDo;
	public UntilClause(Command until1, Command do1) {
		super();
		mUntil = until1;
		mDo = do1;
	}
	
	public void print(PrintWriter out)
	{
		out.print("until ");
		mUntil.print(out);
		out.println(" do");
		mDo.print(out);
		out.println("done");
	}

	public int exec(Shell shell) throws Exception {
		
		XIOEnvironment io = shell.getEnv().saveIO();
		try {
			applyRedirect(shell);
			while( !shell.toBool( shell.exec( mUntil ) ) && shell.keepRunning() ){
				
				shell.exec( mDo );
			}
			
		}
		finally {
			shell.getEnv().restoreIO(io);
		}
		
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

