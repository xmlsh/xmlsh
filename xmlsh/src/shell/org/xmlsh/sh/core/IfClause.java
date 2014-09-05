/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.sh.core;

import org.xmlsh.sh.shell.Shell;

import java.io.PrintWriter;

public class IfClause extends CompoundCommandExpr 
{
	private CommandExpr	mIfPart;
	private CommandExpr mThenPart;
	private CommandExpr mElsePart;

	public IfClause(CommandExpr ifPart, CommandExpr thenPart, CommandExpr elsePart) {
		super();
		mIfPart = ifPart;
		mThenPart = thenPart;
		mElsePart = elsePart;
		setLocation( ifPart );

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


	@Override
	public int exec(Shell shell) throws Exception 
	{
		shell.getEnv().saveIO();
		try {
			applyRedirect(shell);

			/*
			 * Check condition and execute then part or else part
			 * Returns 0 if there is no else part
			 */

			int cond = shell.execCondition( mIfPart );
			int ret = 0;
			if( cond == 0 && shell.keepRunning() )
				ret = shell.exec( mThenPart );
			else
				if( mElsePart != null && shell.keepRunning() )
					ret = shell.exec( mElsePart );
			return ret;
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
