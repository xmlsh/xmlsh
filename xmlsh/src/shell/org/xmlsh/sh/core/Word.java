/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.sh.core;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.xmlsh.core.CoreException;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.Util;

/*
 * A Value that evaulates to a "cmd_word" which is either a simple string,
 * or a subprocess expression 
 * 
 */
public abstract class Word {
	
	private		boolean 	bExpand = true ;
	
	public abstract void print( PrintWriter out );

	public abstract XValue expand(Shell shell,boolean bExpandWild , boolean bExpandWords ) throws IOException, CoreException;
	

	public String expandString(Shell shell, boolean bExpandWild) throws IOException, CoreException {
		return expand(shell,bExpandWild,false).toString();
	}
	
	public List<XValue> expand(Shell shell, boolean bExpandSequences , boolean bExpandWild , boolean bExpandWords ) throws IOException, CoreException {
		XValue v = expand( shell , false,false);
		List<XValue> list = new ArrayList<XValue>(1);
		list.add( v );
		if( bExpandSequences)
			list = Util.expandSequences(list);
		return list;
		
	}

	public abstract boolean isEmpty();
	
	public abstract String toString();

	public boolean isExpand() {
		
		return bExpand;
	}
	public void setExpand( boolean expand ){
		bExpand = expand ;
	}
}



//
//
//Copyright (C) 2008,2009,2010 , David A. Lee.
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
