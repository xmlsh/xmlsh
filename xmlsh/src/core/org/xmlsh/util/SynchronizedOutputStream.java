/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.util;

import java.io.IOException;
import java.io.OutputStream;

public class SynchronizedOutputStream extends OutputStream {
	private		OutputStream	mOutputStream;
	private		boolean			mCloseOnClose;

	public SynchronizedOutputStream(OutputStream outputStream, boolean close) {
		super();
		mOutputStream = outputStream;
		mCloseOnClose = close ;
	}

	
	/**
	 * @throws IOException
	 * @see java.io.OutputStream#close()
	 */
	public synchronized void close() throws IOException {
			mOutputStream.flush();
			if( false && mCloseOnClose )
				mOutputStream.close();
	}

	/**
	 * @param obj
	 * @return
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public synchronized boolean equals(Object obj) {
		return mOutputStream.equals(obj);
	}

	/**
	 * @throws IOException
	 * @see java.io.OutputStream#flush()
	 */
	public synchronized void flush() throws IOException {
		mOutputStream.flush();
	}

	/**
	 * @return
	 * @see java.lang.Object#hashCode()
	 */
	public synchronized int hashCode() {
		return mOutputStream.hashCode();
	}

	/**
	 * @return
	 * @see java.lang.Object#toString()
	 */
	public synchronized  String toString() {
		return mOutputStream.toString();
	}

	/**
	 * @param b
	 * @param off
	 * @param len
	 * @throws IOException
	 * @see java.io.OutputStream#write(byte[], int, int)
	 */
	public synchronized void write(byte[] b, int off, int len) throws IOException {
		mOutputStream.write(b, off, len);
	}

	/**
	 * @param b
	 * @throws IOException
	 * @see java.io.OutputStream#write(byte[])
	 */
	public synchronized  void write(byte[] b) throws IOException {
		mOutputStream.write(b);
	}

	/**
	 * @param b
	 * @throws IOException
	 * @see java.io.OutputStream#write(int)
	 */
	public synchronized void write(int b) throws IOException {
		mOutputStream.write(b);
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
