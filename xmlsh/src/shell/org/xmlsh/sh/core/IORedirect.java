/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.sh.core;

import org.xmlsh.core.EvalEnv;
import org.xmlsh.sh.shell.Shell;

import java.io.PrintWriter;

public class IORedirect {
	private static final EvalEnv mPortEnv = EvalEnv.basicInstance();
	String	mPortname;	// (port)
	IOFile	mFile;		//  < file
	IOHere	mHere;		// <<tag ...tag

	// <port>op  ->  (port) op

	/* (port)>(port) */
	public IORedirect( String portstring ) {
		
		if( portstring.startsWith("(")){
			mPortname = portstring.substring(0, portstring.indexOf(')') + 1 );
			String op = portstring.substring(mPortname.length(),portstring.lastIndexOf('(')) ;
			String port2 = portstring.substring(mPortname.length() + op.length() );
			mFile = new IOFile( op , port2 );
		} 
		
	}
	/* (port)OP fileWord */
	public IORedirect( String portstring , Word file ) {
		if( portstring.startsWith("(")){
			mPortname = portstring.substring(0, portstring.indexOf(')') + 1 );
			String op = portstring.substring(mPortname.length()) ;
			mFile = new IOFile( op , file );
		} else
			mFile = new IOFile( portstring , file );
	}
	
	// <port> op file
	public IORedirect( String portstring , String op , Word file ) {
		mPortname = portstring ;
		mFile = new IOFile( op , file );
	}
	
	public IORedirect(String portname ,  IOHere here ){
		mPortname = portname ;
		mFile = null;
		mHere = here;
	}
	
	

	public void print(PrintWriter out) {
		
		if( mPortname != null ){
			out.print(mPortname);
			
		}
			
		if( mFile != null)
			mFile.print(out);
		if( mHere != null)
			mHere.print(out);

		
	}

	public void exec(Shell shell,SourceLocation loc ) throws Exception {
		
		String port = null;
		if( mPortname != null )
			port  = mPortname.substring(
					  mPortname.indexOf('(') + 1 ,
					  mPortname.indexOf(')'));
		
		
		if( mFile != null )
			mFile.exec(shell,port,loc);
		if( mHere != null )
			mHere.exec( shell,port);

		
	}
}
//
//
//Copyright (C) 2008-2014    David A. Lee.
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
