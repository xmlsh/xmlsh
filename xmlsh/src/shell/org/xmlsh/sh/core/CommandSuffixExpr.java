/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.sh.core;

import org.xmlsh.core.CoreException;
import org.xmlsh.core.EvalEnv;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.Shell;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class CommandSuffixExpr {
	private static final EvalEnv mCmdEnv = EvalEnv.newInstance(true,true,true,false);
	private static final EvalEnv mArgEnv = EvalEnv.commandArgsInstance();

	private WordList		mArgs = new WordList();
	private	 IORedirectList		mRedirect = new IORedirectList();
	public CommandSuffixExpr() {

	}

	public void addArg( Word arg){
		mArgs.add(arg);
	}

	public void addIO( IORedirect io){
		mRedirect.add(io);
	}

	public void print(PrintWriter out) {
		mArgs.print(out);
		mRedirect.print(out);



	}

	/*
	 * Expand command line argument list
	 * into runtime argument list 0-n ... 
	 */

	public List<XValue> toCmdLine(Shell shell, Word command, SourceLocation loc ) throws IOException, CoreException 
	{
		ArrayList<XValue>	args = new ArrayList<XValue>();

		args.addAll( command.expandToList(shell,mCmdEnv,loc));
		for( Word arg : mArgs )
			args.addAll(arg.expandToList(shell, mArgEnv,loc));

		return args;
	}

	public void exec(Shell shell, SourceLocation loc) throws Exception {
		this.mRedirect.exec(shell, loc );

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
