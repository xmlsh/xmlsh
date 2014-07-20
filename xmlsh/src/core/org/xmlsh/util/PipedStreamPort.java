/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.util;

import org.xmlsh.core.InputPort;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.StreamInputPort;
import org.xmlsh.core.StreamOutputPort;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class PipedStreamPort extends PipedPort {
	private		PipedInputStream 	mIn;
	private		PipedOutputStream 	mOut;
	private		static final int sDefaultSize = 10*1024;
	
	public PipedStreamPort()  throws IOException
	{
	
		mOut = new PipedOutputStream();
		mIn = new PipedInputStream(mOut,sDefaultSize);
		
	}
	
	public PipedStreamPort(int size) throws IOException {
		mOut = new PipedOutputStream();
		mIn = new PipedInputStream(mOut,size);
		
	}

	public	InputPort	getInput() throws IOException { 
		return new StreamInputPort(mIn,null) ; 
	}
	
	public OutputPort getOutput() { 
		return new StreamOutputPort(mOut) ; 
	}
	
	static public PipedStreamPort[] getPipes(int n ) throws IOException
	{
		if( n == 0 )
			return null;
		PipedStreamPort	streams[] = new PipedStreamPort[n];
		for( int i = 0 ; i < n ; i++ )
			streams[i] = new PipedStreamPort();
		return streams;
		
	}

	@Override
	public void close() {
		Util.safeClose(mIn);
		Util.safeClose(mOut);
		
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
