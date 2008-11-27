/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.core;

import java.io.IOException;
import java.io.OutputStream;

import org.xmlsh.util.SynchronizedOutputStream;

/*
 * An OutputPort represents an output sync of data, either Stream (bytes) or XML data
 * 
 */




public class OutputPort implements IPort
{

	// Actual input stream
	private SynchronizedOutputStream	 mStream;
	
	
	public OutputPort( OutputStream os ) throws IOException
	{
		mStream = new SynchronizedOutputStream(os);
	}

	/*
	 * Standard input stream - created on first request
	 */
	
	public	OutputStream asOutputStream() 
	{
		
		return mStream;
	}
	public void close() throws IOException {
		
			mStream.close();
	
		
	}
	public void addRef() 
	{
		mStream.addRef();
		
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
