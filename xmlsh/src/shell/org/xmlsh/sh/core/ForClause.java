/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.sh.core;

import java.io.PrintWriter;
import java.util.List;

import org.xmlsh.core.EvalEnv;
import org.xmlsh.core.XValue;
import org.xmlsh.core.XVariable;
import org.xmlsh.sh.shell.ControlLoop;
import org.xmlsh.sh.shell.Shell;

public class ForClause extends CompoundCommandExpr {
	private static final EvalEnv mEnv = EvalEnv.newInstance(true ,true,true , false );
	String		mName;
	WordList	mWords;
	CommandExpr		mCommand;

	public ForClause( String name, WordList words, CommandExpr c )
	{
		mName = name ;
		mWords = words;
		mCommand = c;
		setLocation(c);
	}
	public ForClause( String name,  CommandExpr c )
	{
		this(name,null,c);
	}

	@Override
	public void print(PrintWriter out, boolean bExec)
	{
		out.print("for " + mName );
		if( mWords != null && ! mWords.isEmpty()  ){
			out.print(" in ");

			mWords.print(out);
		}
		out.println(" ; do");
		mCommand.print(out, bExec);
		out.println();
		out.println("done");

	}

	@Override
	public int exec(Shell shell) throws Exception {

		if( !mName.matches("[a-zA-Z_0-9]+")){
			shell.printErr("Not an identifier: " + mName );
			return 1;
		}


		shell.getEnv().saveIO();
		ControlLoop loop = shell.pushLoop( getSourceLocation()  );
		try {
			applyRedirect(shell);
			if( mWords == null ) {	// for all args 
				for( XValue inword :  shell.getArgs() ) {
					if( ! shell.keepRunning() )
						break ;
					// Forcibly use a new variable every iteration
					shell.getEnv().setVar(mName, inword	);
					shell.exec( mCommand );	
					if( loop.mContinue ) // continue clause - clear out  continue & keep going
						loop.mContinue = false ;


				}
			} else
				for( Word in : mWords ) {

					List<XValue> inList = in.expandToList( shell , mEnv );
					if( ! shell.keepRunning() )
						break ;
					for( XValue inword : inList ) {
						if( ! shell.keepRunning() )
							break ;
						// Forcibly use a new variable every iteration
						shell.getEnv().setVar( mName, inword );
						shell.exec( mCommand );
						if( loop.mContinue ) // continue clause - clear out  continue & keep going
							loop.mContinue = false ;

					}

				}


		} finally {
			shell.popLoop(  loop );
			shell.getEnv().restoreIO();
		}
		return 0;

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

