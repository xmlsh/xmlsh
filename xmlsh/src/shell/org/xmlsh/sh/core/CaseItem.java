/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.sh.core;

import java.io.IOException;
import java.io.PrintWriter;

import org.xmlsh.core.CoreException;
import org.xmlsh.core.EvalEnv;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.Util;

public class CaseItem {
	private		StringList		mPattern;
	private		CommandExpr			mCommand;
	private		SourceLocation  mLocation ;
	private static final EvalEnv mEnv = EvalEnv.basicInstance();
	public CaseItem(StringList pattern, CommandExpr command, SourceLocation location ) {

		mPattern = pattern;
		mCommand = command;
		mLocation = location;

	}


	public void print(PrintWriter out,boolean bExec){
		mPattern.print(out);
		out.print(") ");
		if( mCommand != null )
			mCommand.print(out, bExec);
	}

	public boolean matches(Shell shell , String word) throws IOException, CoreException {
		for( String pattern : mPattern ){

			String p = EvalUtils.expandStringToString(shell, pattern , mEnv );


			if( Util.wildMatches( p, word , true ))
				return true ;
		}
		return false;
	}

	public int exec(Shell shell) throws Exception {
		if( mCommand == null )
			return 0 ;
		return shell.exec(mCommand);
	}


	/**
	 * @return the location
	 */
	public SourceLocator getLocation() {
		return mLocation;
	}


	/**
	 * @param location the location to set
	 */
	public void setLocation(SourceLocation location) {
		mLocation = location;
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
