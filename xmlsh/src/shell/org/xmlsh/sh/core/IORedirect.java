/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.sh.core;

import java.io.PrintWriter;

import org.xmlsh.sh.shell.Shell;

public class IORedirect {
	Word	mPortname;	// (word)
	IOFile	mFile;		//  < file
	IOHere	mHere;		// <<tag ...tag


	
	public IORedirect( Word name , IOFile file ){
		mPortname = name ;
		
		mFile = file;
		mHere = null;

	}
	
	public IORedirect(Word name ,  IOHere here ){
		mPortname = name ;
		mFile = null;

		mHere = here;
	}
	
	

	public void print(PrintWriter out) {
		
		if( mPortname != null ){
			out.print("(");
			mPortname.print(out);
			out.print(")");
			
		}
			
		if( mFile != null)
			mFile.print(out);
		if( mHere != null)
			mHere.print(out);

		
	}

	public void exec(Shell shell,SourceLocation loc ) throws Exception {
		
		String port = null;
		if( mPortname != null )
			port  = mPortname.expandString(shell, false, loc );
		
		
		if( mFile != null )
			mFile.exec(shell,port,loc);
		if( mHere != null )
			mHere.exec( shell,port);

		
	}
}
//
//
//Copyright (C) 2008-2013    David A. Lee.
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
