/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.sh.core;

import org.xmlsh.core.CoreException;
import org.xmlsh.core.EvalEnv;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.XValue;
import org.xmlsh.core.XVariable;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.JavaUtils;
import org.xmlsh.util.StringPair;
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

@Deprecated
public class MethodCallWord extends Word {
	String		mVariable;
	String		mMethod;
	WordList	mArgs ;
	
	public MethodCallWord( String func , WordList args )
	{
		StringPair pair = new StringPair(func,'.');
		mVariable = pair.getLeft();
		mMethod = pair.getRight();
		
		mArgs = args ;

	}
	
	public void print( PrintWriter out )
	{
		out.print( mVariable );
		out.print(".");
		out.print(mMethod);
		out.print("(");
		if( mArgs != null )
			mArgs.print(out);
		out.print(")");
		
	}	
	


	
	@Override
	public XValue expand(Shell shell, EvalEnv env,SourceLocation loc ) throws IOException, CoreException {
		
		
		XVariable var = shell.getEnv().getVar(mVariable);
		if( var == null )
			throw new InvalidArgumentException("Unknown variable: " + mVariable );
	

		ArrayList<XValue>	args = new ArrayList<XValue>();
		
		if( mArgs != null )
			for( Word arg : mArgs )
				args.addAll(arg.expandToList(shell,env,loc));
		
		
		try {
			
			
			XValue obj = JavaUtils.callMethod(var.getValue(), mMethod, args, shell.getClassLoader(null));
			
			
			return obj;
			
		
		} catch (Exception e) {
			if( e instanceof CoreException )
				throw (CoreException)e ;
			if( e instanceof IOException )
				throw (IOException)e;
			throw new CoreException(e);
		} 
		
		
		
		
		
	}

	
	public boolean isEmpty() {
		return Util.isEmpty(mVariable);
	}
	

	
@Override
	String getSimpleName()
	{
	   return mVariable  + "." + mMethod ;
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
