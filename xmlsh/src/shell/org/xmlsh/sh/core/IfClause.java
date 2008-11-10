/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.sh.core;

import java.io.PrintWriter;

import org.xmlsh.core.XIOEnvironment;
import org.xmlsh.sh.shell.Shell;

public class IfClause extends CompoundCommand 
{
	private Command	mIfPart;
	private Command mThenPart;
	private Command mElsePart;
	
	public IfClause(Command ifPart, Command thenPart, Command elsePart) {
		super();
		mIfPart = ifPart;
		mThenPart = thenPart;
		mElsePart = elsePart;
	}

	/* (non-Javadoc)
	 * @see org.xmlsh.sh.core.Command#print(java.io.PrintWriter)
	 */
	@Override
	public void print(PrintWriter out, boolean bExec) {
		out.print("if " );
		mIfPart.print(out, bExec);
		out.println("; ");
		if( mThenPart !=null){
			out.println("then");
			mThenPart.print(out, bExec);
		}
		if( mElsePart !=null){
			out.println("else");
			mElsePart.print(out, bExec);
		}
		out.println("fi");
		
	}
	
	
	public int exec(Shell shell) throws Exception 
	{
		XIOEnvironment io = shell.getEnv().saveIO();
		try {
			applyRedirect(shell);
			
			int ret = shell.exec( mIfPart );
			if( ret == 0 && shell.keepRunning() )
				ret = shell.exec( mThenPart );
			else
			if( mElsePart != null && shell.keepRunning() )
				ret = shell.exec( mElsePart );
			return ret;
		} finally {
			shell.getEnv().restoreIO(io);
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
