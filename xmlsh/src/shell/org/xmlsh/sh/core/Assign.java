/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.sh.core;

import java.io.IOException;
import java.io.PrintWriter;

import org.xmlsh.core.CoreException;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.Shell;

public class Assign {
	private	 String		mVariable;
	private Word		mValue;		// a single value a=b
	private WordList	mValueList; // a sequence constructor a=(b)
	public Assign(String variable, Word value) {
		super();
		setVariable(variable);
		mValue = value;

	}
	public Assign(String variable, WordList value) {
		super();
		setVariable(variable);
		mValueList = value;
	}
	public void print(PrintWriter out) {
		out.print(getVariable());
		out.print("=");
		if( mValue != null )
			mValue.print(out);
		else
		{
			out.print("(");
			boolean bFirst = true ;
			for( Word w: mValueList ){
				if( ! bFirst )
					out.print(" ");
				w.print(out);
				bFirst = false ;
				
			}
			out.print(")");
				
		}
		out.print( " ");
		
	}
	public void setVariable(String variable) {
		mVariable = variable;
	}
	public String getVariable() {
		return mVariable;
	}
	public XValue expand(Shell shell) throws IOException, CoreException {
		if( mValue != null )
			// Single variables dont expand wildcards
			return mValue.expand(shell, false, false);
		else
			// Sequences expand wildcards
			return mValueList.expand(shell, true, false);
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
