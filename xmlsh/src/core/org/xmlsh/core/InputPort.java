/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.core;

import java.io.IOException;
import java.io.InputStream;

import org.xmlsh.util.SynchronizedInputStream;

/*
 * An InputPort represents an input source of data, either Stream (bytes) or XML data
 * 
 */


public class InputPort 
{
	// Actual input stream
	private SynchronizedInputStream	 mStream;


	public InputPort( InputStream is ) throws IOException
	{
		setInputStream(is);
	}
	
	public void		setInputStream( InputStream is ) throws IOException
	{
		if( mStream != null )
			mStream.close();
		mStream = new SynchronizedInputStream(is);
	}

	/*
	 * Standard input stream - created on first request
	 */
	
	public	InputStream asInputStream() 
	{
		
		return mStream;
	}

	public void close() throws IOException {

			mStream.close();

		
	}

	public void addRef() {
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
