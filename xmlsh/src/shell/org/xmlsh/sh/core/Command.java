/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.sh.core;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.xmlsh.sh.shell.Shell;

public abstract class Command {
	
	private		boolean		mWait = true ;
	
	public boolean isWait(){ return mWait ; }
	public void	setWait(boolean w) { mWait = w ; }
	
	public abstract void print( PrintWriter out, boolean bExec);
	public abstract int exec( Shell shell) throws Exception;
	
	public String	toString() {
		
		return toString(false);
	}
	
	public String	toString(boolean bExec) {
		StringWriter sw = new StringWriter();
		PrintWriter w = new PrintWriter(sw);
		print(w, bExec);
		w.flush();
		return sw.toString();
		
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
