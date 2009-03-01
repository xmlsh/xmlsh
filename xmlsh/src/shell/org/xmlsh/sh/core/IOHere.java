/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.sh.core;

import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import org.xmlsh.core.CoreException;
import org.xmlsh.sh.shell.Shell;

public class IOHere {

	private String mHere;
	public IOHere( String dless , String here){
		boolean stripTabs = dless.equals("<<-");
		if( stripTabs )
			here = here.replaceAll("^\t+", "" ).replaceAll("\n\t+", "\n");
		mHere = here;
	}
	public void print(PrintWriter out) {
		out.print(mHere);
		
	}
	public void exec(Shell shell) throws CoreException, UnsupportedEncodingException  {
		
		shell.getEnv().setStdin( 
				new ByteArrayInputStream(mHere.getBytes(Shell.getTextEncoding())));
		
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
