/**
 * $Id: PipedStream.java 209 2009-04-11 16:32:13Z daldei $
 * $Date: 2009-04-11 12:32:13 -0400 (Sat, 11 Apr 2009) $
 *
 */

package org.xmlsh.util;

import java.io.IOException;

import javanet.staxutils.XMLEventPipe;

import org.xmlsh.core.InputPort;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.XMLEventInputPort;
import org.xmlsh.core.XMLEventOutputPort;
import org.xmlsh.sh.shell.SerializeOpts;

public class PipedXEventPort extends PipedPort {
	private		XMLEventInputPort 	mIn;
	private		XMLEventOutputPort 	mOut;
	private		XMLEventPipe		mPipe;


	public PipedXEventPort(SerializeOpts opts)  throws IOException
	{
		mPipe = new XMLEventPipe(1000);

		mIn = new XMLEventInputPort(mPipe.getReadEnd(),"");
		mOut = new XMLEventOutputPort(mPipe.getWriteEnd(),opts);

	}

	@Override
	public	InputPort	getInput() throws IOException { 
		return mIn;
	}

	@Override
	public OutputPort getOutput() { 
		return mOut;
	}

	static public PipedXEventPort[] getPipes(int n, SerializeOpts opts) throws IOException
	{
		if( n == 0 )
			return null;
		PipedXEventPort	streams[] = new PipedXEventPort[n];
		for( int i = 0 ; i < n ; i++ )
			streams[i] = new PipedXEventPort(opts);
		return streams;

	}

	@Override
	public void close() {
		Util.safeClose(mIn);
		Util.safeClose(mOut);
		mPipe = null ;
		mIn = null;
		mOut = null ;

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
