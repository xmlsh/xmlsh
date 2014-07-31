/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.sh.core;

import org.xmlsh.core.CoreException;
import org.xmlsh.core.EvalEnv;
import org.xmlsh.core.StaticContextFlag;
import org.xmlsh.core.StaticEnvironment;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.MutableInteger;
import org.xmlsh.util.Util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/*
 * A Value that evaulates to a "cmd_word" which is either a simple string,
 * or a subprocess expression 
 * 
 */
public abstract class Word {
		
	public abstract void print( PrintWriter out );


	protected Word( ) {
	}

	public XValue expandWords( Shell shell , String word , EvalEnv env ) throws IOException {
		
		env = evalEnv( env ); // Mask my own settings
		
		// if expand word then need to do IFS splitting
		if( env.expandWords() && ! env.preserveValue() )
			return new XValue( (String[]) shell.getIFS().split(word).toArray() );
		else
			return new XValue( word);
				
	}

	public abstract XValue expand(Shell shell, EvalEnv env, SourceLocation loc ) throws IOException, CoreException;
	
	public String expandString(Shell shell, EvalEnv env, SourceLocation loc ) throws IOException, CoreException {
		return expand(shell,evalEnv(env),loc).toString();
	}
	
	public List<XValue> expandToList(Shell shell, EvalEnv env , SourceLocation loc ) throws IOException, CoreException {
		env = evalEnv(env);
		XValue v = expand( shell,  env , loc);
		List<XValue> list = new ArrayList<XValue>(1);
		if( v != null && ! v.isNull() )
		     list.add( v );
		if( env.expandSequences())
			list = Util.expandSequences(list);
		return list;
		
	}

	public abstract boolean isEmpty();
	
	public String toString() {
		
		StringWriter sw;
		PrintWriter w = new PrintWriter(sw = new StringWriter());
		this.print(w);
		w.close();
		return sw.toString();
	}

	public EvalEnv evalEnv(EvalEnv parent) {
		return parent ;
	}


	abstract String getSimpleName();


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
