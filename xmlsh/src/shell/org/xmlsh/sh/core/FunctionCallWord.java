/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.sh.core;

import org.xmlsh.core.CommandFactory;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.EvalEnv;
import org.xmlsh.core.IFunction;
import org.xmlsh.core.IFunctionDecl;
import org.xmlsh.core.IFunctionExpr;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.grammar.Token;
import org.xmlsh.sh.shell.ParseResult;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.Util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;

/*
 * A Value that evaulates to a "cmd_word" which is either a simple string,
 * or a subprocess expression 
 * 
 */
public class FunctionCallWord extends Word
{
	String	 mFunction;
	WordList	mArgs;
	Word      mPrefix;  // expr . function( ... )

	public FunctionCallWord(Token t , String func, WordList args)
	{
		super(t);
		mPrefix = null;
		mFunction = func;
		mArgs = args;

	}
  public FunctionCallWord( Word prefix , Token t , String func, WordList args)
  {
    super(t);
    mPrefix = prefix;
    mFunction = func;
    mArgs = args;

  }
  
	@Override
	public void print(PrintWriter out)
	{
		out.print(mFunction);
		out.print("(");
		if(mArgs != null)
			mArgs.print(out);
		out.print(")");

	}

	@Override
	public boolean isEmpty()
	{
		return Util.isEmpty(mFunction);
	}

	@Override
	public String toString()
	{
		Writer sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		print(pw);
		pw.flush();
		return sw.toString();
	}

	@Override
	public String getSimpleName()
	{
		return mFunction;
	}

	@Override
	protected ParseResult expandToResult(Shell shell, EvalEnv env, SourceLocation loc, ParseResult result)
			throws IOException, CoreException
			{

		// Try builtin functions first
	  IFunction func = CommandFactory.getInstance().getBuiltinFunction(shell, mFunction, loc);

		if(func == null){
			IFunctionDecl funcdecl = shell.getFunctionDecl(mFunction);
			if( funcdecl != null )
			  func = funcdecl.getFuntionExpr().getFunction();
		}

		if(func == null)
			throw new InvalidArgumentException("Unknown function: " + mFunction);

		ArrayList<XValue> args = new ArrayList<XValue>();

		EvalEnv argEnv = func.argumentEnv(env);

		if(mArgs != null)
			for (Word arg : mArgs)
				args.addAll(arg.expandToList(shell, argEnv, loc));

		try {

		  XValue xret = func.run(shell, loc , args);
			// ?? should check ret ?
			return EvalUtils.expandValueToResult(shell, xret, func.returnEnv(env), loc, result);

		} catch (Exception e) {
			if(e instanceof CoreException)
				throw (CoreException) e;
			if(e instanceof IOException)
				throw (IOException) e;
			throw new CoreException(e);
		}
			}

}

//
//
// Copyright (C) 2008-2014 David A. Lee.
//
// The contents of this file are subject to the "Simplified BSD License" (the
// "License");
// you may not use this file except in compliance with the License. You may
// obtain a copy of the
// License at http://www.opensource.org/licenses/bsd-license.php
//
// Software distributed under the License is distributed on an "AS IS" basis,
// WITHOUT WARRANTY OF ANY KIND, either express or implied.
// See the License for the specific language governing rights and limitations
// under the License.
//
// The Original Code is: all this file.
//
// The Initial Developer of the Original Code is David A. Lee
//
// Portions created by (your name) are Copyright (C) (your legal entity). All
// Rights Reserved.
//
// Contributor(s): none.
//
