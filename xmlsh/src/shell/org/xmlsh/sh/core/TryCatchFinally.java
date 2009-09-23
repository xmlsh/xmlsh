/**
 * $Id: IfClause.java 209 2009-04-11 16:32:13Z daldei $
 * $Date: 2009-04-11 12:32:13 -0400 (Sat, 11 Apr 2009) $
 *
 */

package org.xmlsh.sh.core;

import java.io.PrintWriter;

import org.xmlsh.core.XIOEnvironment;
import org.xmlsh.sh.shell.Shell;

public class TryCatchFinally extends CompoundCommand 
{
	private 	Command		mTryPart;
	private		String		mCatchVar;
	private 	Command 	mCatchPart;
	private 	Command 	mFinallyPart;
	
	public TryCatchFinally(Command tryPart, String catchVar , Command catchPart, Command finallyPart ) {
		super();
		mTryPart 		= 	tryPart;
		mCatchVar		= 	catchVar;
		mCatchPart 		= 	catchPart;
		mFinallyPart 	= 	finallyPart;
	}

	/* (non-Javadoc)
	 * @see org.xmlsh.sh.core.Command#print(java.io.PrintWriter)
	 */
	@Override
	public void print(PrintWriter out, boolean bExec) {
		out.print("try " );
		mTryPart.print(out, bExec);
		
		out.print("catch ");
		
		out.println(mCatchVar);
		
		mCatchPart.print(out, bExec);
		
		if( mFinallyPart !=null){
			out.println("finally");
			mFinallyPart.print(out, bExec);
		}
		out.println("fi");
		
	}
	
	
	public int exec(Shell shell) throws Exception 
	{
		XIOEnvironment io = shell.getEnv().saveIO();
		try {
			applyRedirect(shell);
			
			int ret = 0;
			try {
				ret = shell.exec( mTryPart  );
			} catch( Exception e ){
				ret = shell.exec( mCatchPart );
				
				
			} finally {

				if( shell.keepRunning() && mFinallyPart != null )
					ret = shell.exec( mFinallyPart );
			}
			
		
			return ret;
		} finally {
			shell.getEnv().restoreIO(io);
		}
	}
}

//
//
//Copyright (C) 2008,2009 , David A. Lee.
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
