/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.core;


import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmValue;




public class XdmItemPipe {
	BlockingQueue<XdmItem>   mQueue ;
	
	// Special EOF marker indicating closed pipe
	private static XdmItem mEOF = new XdmAtomicValue("");
	


	private class XdmItemPipeReader implements IXdmItemReader
	{

		@Override
		public XdmItem read() throws CoreException {
			try {
				XdmItem item =  mQueue.take();
				if( item == mEOF )
					return null ;
				return item ;
			} catch (InterruptedException e) {
				throw new CoreException(e);
			}
		}
	
	}
	
	private class XdmItemPipeWriter implements IXdmItemWriter
	{

		@Override
		public void write(XdmValue value) throws CoreException {
			for( XdmItem item : value )
				try {
					mQueue.put(item);
				} catch (InterruptedException e) {
					throw new CoreException(e);
				}
			
		}

		@Override
		public void write(XdmItem item) throws CoreException {
			try {
				mQueue.put(item);
			} catch (InterruptedException e) {
				throw new CoreException(e);
			}
			
		}

		@Override
		public void close() throws CoreException {
			write( mEOF );
			
		}
	
	}
	
	
	
	
	public XdmItemPipe(int size) {
		mQueue = new LinkedBlockingQueue<XdmItem>(size);
	}

	public IXdmItemReader getReadEnd() {
		return new XdmItemPipeReader();
	}

	public IXdmItemWriter getWriteEnd() {
		// TODO Auto-generated method stub
		return new XdmItemPipeWriter();
	}
	
}




//
//
//Copyright (C) 2008-2012 David A. Lee.
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
