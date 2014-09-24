/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.sh.core;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.xmlsh.core.CoreException;
import org.xmlsh.core.EvalEnv;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.grammar.Token;
import org.xmlsh.sh.shell.ParseResult;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.Util;

/*
 * A Value that evaulates to a "cmd_word" which is either a simple string,
 * or a subprocess expression 
 * 
 */
public abstract class Word {

	private Token mFirstToken ;
	public abstract void print( PrintWriter out );


	/*protected Word() {
		mFirst = null ; 
	}*/
	protected Word( Token t  ) {
		mFirstToken = t ;

	}

	public boolean isJoinable() {
		return 
		mFirstToken !=null && 
		mFirstToken.specialToken == null &&
			! isDelim() ; 
		
	}
	

	// Expand a word into an existing result class 
	protected abstract ParseResult expandToResult(Shell shell, EvalEnv env, ParseResult result ) throws IOException, CoreException;


	public final XValue expand(Shell shell , EvalEnv env ) throws IOException, CoreException {

		ParseResult result = new ParseResult(shell);
		env=evalEnv(env);
		expandToResult( shell , env , result );
		return EvalUtils.expandResultToValue(shell, result, env) ;

	}


	public String expandString(Shell shell, EvalEnv env ) throws IOException, CoreException {
	   env=evalEnv(env);
		return expand(shell,env).toString();
	}

	public List<XValue> expandToList(Shell shell, EvalEnv env ) throws IOException, CoreException {
		env = evalEnv(env);
		XValue v = expand( shell,  env);
		List<XValue> list = new ArrayList<XValue>(1);
		if( v != null && ! v.isNull() )
			list.add( v );
		if( env.expandSequences())
			list = Util.expandSequences(list);
		return list;

	}

	public abstract boolean isEmpty();

	@Override
	public String toString() {

		StringWriter sw;
		PrintWriter w = new PrintWriter(sw = new StringWriter());
		print(w);
		w.close();
		return sw.toString();
	}

	public EvalEnv evalEnv(EvalEnv parent) {
		return parent ;
	}


	abstract String getSimpleName();


    public boolean isDelim() {
        return false;
    }


	/**
	 * @return the first
	 */
	public Token getFirstToken()
	{
		return mFirstToken;
	}


	/**
	 * @param first the first to set
	 */
	public void setFirstToken(Token first)
	{
		mFirstToken = first;
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
