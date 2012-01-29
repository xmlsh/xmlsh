/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.sh.core;

import java.io.IOException;
import java.io.PrintWriter;

import org.xmlsh.core.CoreException;
import org.xmlsh.core.InputPort;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.XEnvironment;
import org.xmlsh.core.XVariable;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.Util;

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
		if( mFile != null )
			mFile.print(out);
		
	}

	
	
	public void exec(Shell shell, String port, SourceLocation loc ) throws IOException, CoreException {

		XEnvironment env = shell.getEnv();
		SerializeOpts sopts = shell.getSerializeOpts();
		
		/*
		 * File-less redirections 1>&2 2>&1 
		 */
		if( mFile == null ){
			
			/*
			 * Port Duplication puts the same port in 2 slots
			 * must manage an extra reference count to avoid over releasing
			 */
			
			if( mPrefix.equals("1>&2")){
				OutputPort stderr = env.getStderr();
				stderr.addRef(); // keep stdout from being over released
				
				// Duplicate stdout from stderr
				env.setStdout(  stderr );
			} else
			if( mPrefix.equals("2>&1")){

				OutputPort stdout = env.getStdout();

				// Duplicate stderr from stdout
				
				stdout.addRef(); // keep stderr from being over released
				env.setStderr(  stdout );
				
			}
			
			return ;
		}
		
		
		
		String file = mFile.expandString(shell, true,loc);
		
		
		/*
		 * Variable IO syntax   cmd <{var}
		 * 
		 */
		boolean isVar = 	file.startsWith("{") &&
							file.endsWith("}");
		if( isVar ){
			String var = file.substring(1,file.length()-1);
			if( Util.isBlank(var))
				throw new CoreException("Invalid blank name for output variable");
			
			if( mPrefix.equals("<"))
				env.setInput( port ,  env.getVar(var) );
			else
			if( mPrefix.equals(">")){

				XVariable xvar = env.getVar(var);
				if( xvar == null ){
					xvar = new XVariable(var,null);
					env.setVar(xvar, false );
				}
				else
					xvar.clear();
				
				env.setOutput(port,xvar);
			}
			else
			if( mPrefix.equals(">>"))
			{
				XVariable xvar = env.getVar(var);
				if( var == null ){
					xvar = new XVariable(var,null);
					env.setVar(xvar, false );
				}
				env.setOutput(port,xvar);				
			}
			
			return ;
		}
		
		/*
		 * Port IO syntax   cmd <(port)
		 * 
		 */
		boolean isPort = 	file.startsWith("(") &&
							file.endsWith(")");
		if( isPort ){
			String portname = file.substring(1,file.length()-1);
				
			if( mPrefix.equals("<")){
				InputPort inp = env.getInputPort(portname);
				if( inp == null )
					throw new InvalidArgumentException("Input port not found: " + portname );

					env.setInput( port , inp );
			}
			else
			if( mPrefix.equals(">")){
				
				
				OutputPort outp=env.getOutputPort( portname , false );
				if( outp == null )
					throw new InvalidArgumentException("Output port not found: " + portname );
				
				env.setOutput(port,outp);
			}
			else
			if( mPrefix.equals(">>"))
			{	
				OutputPort outp=env.getOutputPort( portname , true );
				if( outp == null )
					throw new InvalidArgumentException("Output port not found: " + portname );

				env.setOutput(port,outp);
					
			}
			else
			if( mPrefix.equals(">&"))
			{	
				
				// Duplicate port from port
				OutputPort outp=env.getOutputPort( portname );
				if( outp == null )
					throw new InvalidArgumentException("Output port not found: " + portname );


				outp.addRef(); // keep stderr from being over released
				env.setOutput(port,outp);
						
			}
			else
			if( mPrefix.equals("<&"))
			{	
					
					// Duplicate port from port
					InputPort inp=env.getInputPort( portname );
					if( inp == null )
						throw new InvalidArgumentException("Input port not found: " + portname );


					inp.addRef(); // keep stderr from being over released
					env.setInput(port,inp);
							
				}
			return ;
		}
		
		
	
		
	
		
		
		if( mPrefix.equals("<")){
			InputPort in = env.setInput(port, shell.getInputPort(file)  );
			in.setSystemId(file);
		}
		else
		if( mPrefix.equals("2>"))
				env.setStderr( shell.getOutputPort(file, false));
		else
		if( mPrefix.equals("2>>"))
				env.setStderr( shell.getOutputPort(file, true));
		
		else
		if( mPrefix.equals(">"))
			env.setOutput(port,shell.getOutputPort(file, false));
		else
		if( mPrefix.equals(">>"))
				env.setOutput(port,shell.getOutputPort(file, true));

				

		
		
	}
	
}

//
//
//Copyright (C) 2008,2009,2010,2011,2012 , David A. Lee.
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
