/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.core;

import java.io.IOException;
import java.io.OutputStream;


/*
 * An output stream that conditionally ignores all output 
 */
public class OmittingOutputStream extends OutputStream
{

	private OutputStream mOut ;
	private boolean bDiscard ;
	public OmittingOutputStream(OutputStream out )
	{

		mOut = out ;
		bDiscard = false ;

	}

	@Override
	public void write(int b) throws IOException
	{
		if( ! bDiscard )
			mOut.write(b);
	}

	/**
	 * @return the discard
	 */
	public boolean getDiscard()
	{
		return bDiscard;
	}

	/**
	 * @param discard the discard to set
	 */
	public void setDiscard(boolean discard)
	{
		bDiscard = discard;
	}

	/**
	 * @param b
	 * @throws IOException
	 * @see java.io.OutputStream#write(byte[])
	 */
	@Override
	public void write(byte[] b) throws IOException
	{
		if( ! bDiscard )
			mOut.write(b);
	}

	/**
	 * @param b
	 * @param off
	 * @param len
	 * @throws IOException
	 * @see java.io.OutputStream#write(byte[], int, int)
	 */
	@Override
	public void write(byte[] b, int off, int len) throws IOException
	{
		if( ! bDiscard )
			mOut.write(b, off, len);
	}


	/**
	 * @throws IOException
	 * @see java.io.OutputStream#flush()
	 */
	@Override
	public void flush() throws IOException
	{
		mOut.flush();
	}

	/**
	 * @throws IOException
	 * @see java.io.OutputStream#close()
	 */
	@Override
	public void close() throws IOException
	{
		mOut.close();
	}

}



/*
 * Copyright (C) 2008-2012 David A. Lee.
 * 
 * The contents of this file are subject to the "Simplified BSD License" (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.opensource.org/licenses/bsd-license.php 

 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied.
 * See the License for the specific language governing rights and limitations under the License.
 *
 * The Original Code is: all this file.
 *
 * The Initial Developer of the Original Code is David A. Lee
 *
 * Portions created by (your name) are Copyright (C) (your legal entity). All Rights Reserved.
 *
 * Contributor(s): David A. Lee
 * 
 */