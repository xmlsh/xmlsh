/**
 * $Id: $
 * $DateTime: $
 *
 */

package org.xmlsh.sh.core;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.SynchronizedInputStream;
import org.xmlsh.util.SynchronizedOutputStream;

public class IOFile {
	private String	mPrefix;
	private String	mFile;
	public IOFile(String prefix, String file) {
		super();
		mPrefix = prefix;
		mFile = file;
	}
	public void print(PrintWriter out) {
		out.print(mPrefix);
		out.print(mFile);
		
	}
	public void exec(Shell shell) throws IOException {
		if( mPrefix.equals("<"))
			shell.getEnv().setStdin( 
					new FileInputStream(shell.getFile(mFile)));
		else
		if( mPrefix.equals(">"))
			shell.getEnv().setStdout(
					 new FileOutputStream(shell.getFile(mFile)));
	
		else
		if( mPrefix.equals(">>"))
				shell.getEnv().setStdout(
						new FileOutputStream(shell.getFile(mFile),true));
		
		
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
