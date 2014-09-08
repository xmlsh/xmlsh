/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.sh.core;

import java.io.PrintWriter;

import org.xmlsh.core.EvalEnv;
import org.xmlsh.sh.grammar.Token;
import org.xmlsh.sh.shell.Shell;

public class IORedirect {
	private static final EvalEnv mPortEnv = EvalEnv.basicInstance();
	private String	mPortname;	// (port)
	private IOFile	mFile;		//  < file
	private IOHere	mHere;		// <<tag ...tag
	private Token   mFirstToken;
	
	

	// <port>op  ->  (port) op

	/* (port)>(port) */
	public IORedirect( Token t, String portstring ) {
	  mFirstToken = t ;
		//OP(POT)
		if( portstring.startsWith("<(") || 
				portstring.startsWith(">(") ) {

			String op = portstring.substring(0,1);
			String port = portstring.substring(1);
			mFile = new IOFile( op , port );

		}
		else
			// (port)OP<port)
			if( portstring.startsWith("(")){
				mPortname = portstring.substring(0, portstring.indexOf(')') + 1 );
				String op = portstring.substring(mPortname.length(),portstring.lastIndexOf('(')) ;
				String port2 = portstring.substring(mPortname.length() + op.length() );
				mFile = new IOFile( op , port2 );
			} 

	}
	/* (port)OP fileWord */
	public IORedirect( Token t , String portstring , Word file ) {
	   mFirstToken = t ;
		if( portstring.startsWith("(")){
			mPortname = portstring.substring(0, portstring.indexOf(')') + 1 );
			String op = portstring.substring(mPortname.length()) ;
			mFile = new IOFile( op , file );
		} else
			mFile = new IOFile( portstring , file );
	}

	// <port> op file
	public IORedirect( Token t ,String portstring , String op , Word file ) {
	   mFirstToken = t ;

		mPortname = portstring ;
		mFile = new IOFile( op , file );
	}

	public IORedirect(Token t , String portname ,  IOHere here ){
	   mFirstToken = t ;
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
