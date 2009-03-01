/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.sh.core;

import java.io.IOException;
import java.io.PrintWriter;

import org.xmlsh.core.CoreException;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.XEnvironment;
import org.xmlsh.core.XValue;
import org.xmlsh.core.XVariable;
import org.xmlsh.sh.shell.Shell;

public class IOFile {
	private String	mPrefix;
	private Word	mFile;
	public IOFile(String prefix, Word file) {
		super();
		mPrefix = prefix;
		mFile = file;
	}
	public void print(PrintWriter out) {
		out.print(mPrefix);
		mFile.print(out);
		
	}

	
	
	public void exec(Shell shell) throws IOException, CoreException {

		XEnvironment env = shell.getEnv();
		
		String file = mFile.expandString(shell, false);
		
		boolean isPort = 	file.startsWith("{") &&
							file.endsWith("}");
		if( isPort ){
			String var = file.substring(1,file.length()-1);
			
			
			if( mPrefix.equals("<"))
				env.setStdin( env.getVar(var) );
			else
			if( mPrefix.equals(">")){
				XVariable xvar = new XVariable(var,null);
				env.setVar(xvar);
				env.setStdout(xvar);
			}
			else
			if( mPrefix.equals(">>"))
			{
				XVariable xvar = env.getVar(var);
				if( var == null ){
					xvar = new XVariable(var,null);
					env.setVar(xvar);
				}
				env.setStdout(xvar);				
			}
			
			return ;
		}
		
		
	
		
		
		if( mPrefix.equals("<")){
			env.setStdin( shell.getInputStream(file)  );
			env.getStdin().setSystemId(file);
		}
		else
		if( mPrefix.equals("2>"))
				env.setStderr( shell.getOutputStream(file, false));
		
		else
		if( mPrefix.equals(">"))
			env.setStdout(shell.getOutputStream(file, false));
		else
		if( mPrefix.equals(">>"))
				env.setStdout(shell.getOutputStream(file, true));

				

		
		
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
