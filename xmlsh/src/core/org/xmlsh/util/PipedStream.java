/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class PipedStream {
	private		PipedInputStream 	mIn;
	private		PipedOutputStream 	mOut;
	
	public PipedStream() throws IOException
	{
		mOut = new PipedOutputStream();
		mIn = new PipedInputStream(mOut);
		
	}
	
	public	InputStream	getInput() { return mIn ; }
	public OutputStream getOutput() { return mOut ; }
	
	static public PipedStream[] getPipes(int n ) throws IOException
	{
		if( n == 0 )
			return null;
		PipedStream	streams[] = new PipedStream[n];
		for( int i = 0 ; i < n ; i++ )
			streams[i] = new PipedStream();
		return streams;
		
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
