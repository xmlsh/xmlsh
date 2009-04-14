/**
 * $Id: IOFile.java 139 2009-02-03 07:02:34Z daldei $
 * $Date: 2009-02-03 02:02:34 -0500 (Tue, 03 Feb 2009) $
 *
 */

package org.xmlsh.sh.core;

import java.io.IOException;
import java.io.PrintWriter;

import org.xmlsh.core.CoreException;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.XEnvironment;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.sh.shell.ShellThread;
import org.xmlsh.util.NullInputStream;
import org.xmlsh.util.PipedStreamPort;

public class IOCommand {
	private String	mPrefix;
	private Command 		mCommand;
	public IOCommand(String prefix, Command cmd) {
		super();
		mPrefix = prefix;
		mCommand = cmd;
	}
	public void print(PrintWriter out) {
		out.print(mPrefix);
		mCommand.print(out, false);
		out.print(")");
		
	}

	
	
	public void exec(Shell shell, String port) throws IOException, CoreException {

		XEnvironment env = shell.getEnv();
		
		
		
	
		
		// TODO: NOT IMPLEMENTED !!!!
		if( mPrefix.equals("<(")){
			
			
			Shell sh = shell.clone();	// clone shell for execution
			
			PipedStreamPort	pipe = PipedStreamPort.getPipes(1)[0];
			try {
			

				
				sh.getEnv().setStdout(  pipe.getOutput() );
				sh.getEnv().setStdin( new NullInputStream() );
				ShellThread sht =  new ShellThread(sh , null ,   mCommand);



				env.setInput( port , pipe.getInput()  );
				
				sht.start();
				
				
				return ;
		
				
				
			} 
			finally {
				

			}

		
		}
		else
			throw new InvalidArgumentException("Unknown command redirect:" + mPrefix);
				

		
		
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
