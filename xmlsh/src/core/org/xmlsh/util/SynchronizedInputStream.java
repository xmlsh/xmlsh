/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.util;

import java.io.IOException;
import java.io.InputStream;

public class SynchronizedInputStream  extends InputStream {
	private		volatile  InputStream  mInputStream;
    private   boolean mClose ;

	public SynchronizedInputStream(InputStream inputStream, boolean bclose) {
		super();
		mInputStream = inputStream;
		mClose = bclose ;
	}

	/**
	 * @return
	 * @throws IOException
	 * @see java.io.InputStream#available()
	 */
	public synchronized int available() throws IOException {
		return mInputStream.available();
	}

	// do not syncrhonize close - causes deadlock
	public void close() throws IOException {
  
		if( mClose && mInputStream != null )
			mInputStream.close();
	
	}

	/**
	 * @param obj
	 * @return
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public synchronized boolean equals(Object obj) {
		return mInputStream.equals(obj);
	}

	/**
	 * @return
	 * @see java.lang.Object#hashCode()
	 */
	public synchronized int hashCode() {
		return mInputStream.hashCode();
	}

	/**
	 * @param readlimit
	 * @see java.io.InputStream#mark(int)
	 */
	public synchronized void mark(int readlimit) {
		mInputStream.mark(readlimit);
	}

	/**
	 * @return
	 * @see java.io.InputStream#markSupported()
	 */
	public synchronized boolean markSupported() {
		return mInputStream.markSupported();
	}

	/**
	 * @return
	 * @throws IOException
	 * @see java.io.InputStream#read()
	 */
	public synchronized int read() throws IOException {
		return mInputStream.read();
	}

	/**
	 * @param b
	 * @param off
	 * @param len
	 * @return
	 * @throws IOException
	 * @see java.io.InputStream#read(byte[], int, int)
	 */
	public synchronized int read(byte[] b, int off, int len) throws IOException {
		return mInputStream.read(b, off, len);
	}

	/**
	 * @param b
	 * @return
	 * @throws IOException
	 * @see java.io.InputStream#read(byte[])
	 */
	public synchronized int read(byte[] b) throws IOException {
		return mInputStream.read(b);
	}

	/**
	 * @throws IOException
	 * @see java.io.InputStream#reset()
	 */
	public synchronized void reset() throws IOException {
		mInputStream.reset();
	}

	/**
	 * @param n
	 * @return
	 * @throws IOException
	 * @see java.io.InputStream#skip(long)
	 */
	public synchronized long skip(long n) throws IOException {
		return mInputStream.skip(n);
	}

	/**
	 * @return
	 * @see java.lang.Object#toString()
	 */
	public synchronized String toString() {
		return mInputStream.toString();
	}
	public InputStream getStream() {
		return mInputStream;
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
