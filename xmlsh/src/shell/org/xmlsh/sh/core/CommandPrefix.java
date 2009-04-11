/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.sh.core;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.xmlsh.core.CoreException;
import org.xmlsh.core.XValue;
import org.xmlsh.core.XVariable;
import org.xmlsh.sh.shell.Shell;

public class CommandPrefix {
	private List<Assign>	mList = new ArrayList<Assign>();

	public void add( Assign a )
	{
		mList.add(a);
	}
	public void add( String var , Word value )
	{
		mList.add( new Assign( var , value ));
	}
	public void add( String var , WordList value )
	{
		mList.add( new Assign( var , value ));
	}
	


	public void print(PrintWriter out) {
		for (Assign ass : mList) {
			ass.print(out);
			
		}
		
	}

	public void exec(Shell shell) throws IOException, CoreException {
		for (Assign ass : mList) {
			XValue value = ass.expand(shell);
			shell.getEnv().setVar( new XVariable(ass.getVariable(), value) );
			
		}
		
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
