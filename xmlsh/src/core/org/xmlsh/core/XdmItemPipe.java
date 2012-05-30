/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.core;


import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmValue;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;




public class XdmItemPipe {

	/** 
	 * Default maximum number of events that may be stored by this pipe until
	 * the write end blocks.
	 */
	public static final int QUEUE_CAPACITY = 16;

	/** List of events ready to be read. */
	private List<XdmItem> eventQueue = new LinkedList<XdmItem>();

	/** The maximum capacity of the queue, after which the pipe should block. */
	private int capacity = QUEUE_CAPACITY;

	/** Whether the read end has been closed. */
	private boolean readEndClosed;

	/** Whether the write end has been closed. */
	private boolean writeEndClosed;

	/** 
	 * The read end of the pipe. This will be <code>null</code> until
	 * {@link #getReadEnd()} is called for the first time.
	 */
	private PipedXdmItemReader readEnd = new PipedXdmItemReader(this);

	/** 
	 * The write end of the pipe. This will be <code>null</code> until
	 * {@link #getWriteEnd()} is called for the first time.
	 */
	private PipedXdmItemWriter writeEnd = new PipedXdmItemWriter(this);

	/**
	 * Constructs a new XdmValuePipe with the default capacity.
	 */
	public XdmItemPipe() {
	}

	/**
	 * Constructs a new XdmValuePipe with the specified capacity.
	 * 
	 * @param capacity The number of events to buffer until the pipe will block.
	 * 		A number less than or equal to 0 means the pipe will buffer an
	 * 		unbounded number of events.
	 */
	public XdmItemPipe(int capacity) {

		this.capacity = capacity;

	}

	/**
	 * Returns the read end of the pipe, from which events written to the write
	 * end of the pipe will be available.
	 * 
	 * @return The read end of the pipe.
	 */
	public synchronized IXdmValueReader getReadEnd() {

		if (readEnd == null) {

			readEnd = new PipedXdmItemReader(this);

		}

		return readEnd;

	}

	/**
	 * Returns the write end of the pipe, whose events will be available from
	 * the read end of this pipe.
	 * 
	 * @return The write end of the pipe.
	 */
	public synchronized IXdmValueWriter getWriteEnd() {

		if (writeEnd == null) {

			writeEnd = new PipedXdmItemWriter(this);

		}

		return writeEnd;

	}

	/**
	 * {@link XMLEventWriter} implementation used to provide the write end of
	 * the pipe.
	 * 
	 * @author christian
	 * @version $Revision: 1.2 $
	 */
	private static final class PipedXdmItemWriter implements IXdmValueWriter {

		/** The pipe we're connected to. */
		private XdmItemPipe pipe;
		private boolean closed = false ;

		public PipedXdmItemWriter(XdmItemPipe pipe) {

			this.pipe = pipe;

		}

		public synchronized void close()  {


			synchronized (pipe) {

				if (pipe.readEndClosed) {

					pipe.eventQueue.clear();

				}

				pipe.writeEndClosed = true;
				pipe.notifyAll();

			}

		}

		protected void put(XdmItem item) throws UnexpectedException {


			if (closed) {

				throw new UnexpectedException("Stream has completed");

			}

			synchronized (pipe) {

				if (pipe.readEndClosed) {

					// if read end is closed, throw away event
					return;

				}

				if (pipe.capacity > 0) {

					while (pipe.eventQueue.size() >= pipe.capacity) {

						try {

							pipe.wait();

						} catch (InterruptedException e) {

							e.printStackTrace();

						}

					}

				}

				pipe.eventQueue.add(item);
				if (pipe.eventQueue.size() == 1) {

					pipe.notifyAll();

				}

				
			}

		}

		@Override
		public void write(XdmItem item) throws CoreException {
			put(item);
			
		}

		@Override
		public void write(XdmValue value) throws CoreException {
			for( XdmItem item : value )
				put(item);
			
		}

	}


	private static final class PipedXdmItemReader implements IXdmValueReader {

		private static Logger mLogger = LogManager.getLogger( PipedXdmItemReader.class );

		/** THe pipe this stream is connected to. */
		private XdmItemPipe pipe;
		private boolean closed = false ;
		
		public PipedXdmItemReader(XdmItemPipe pipe) {

			this.pipe = pipe;

		}

		public synchronized XdmItem nextItem()   {


			synchronized (pipe) {

				while (pipe.eventQueue.size() == 0) {

					if (pipe.writeEndClosed) {

						return null ;

					}

					try {

						pipe.wait();

					} catch (InterruptedException e) {

						mLogger.error("Interrupted exception in pipe" , e );

					}

				}

				boolean notify =
					pipe.capacity > 0
						&& pipe.eventQueue.size() >= pipe.capacity;

				// remove next event from the queue
				XdmItem item = pipe.eventQueue.remove(0);
				if (notify) {

					pipe.notifyAll();

				}
				return item;

			}

		}

		public synchronized boolean hasNext() {

			if (closed) {

				return false;

			}

			synchronized (pipe) {

				while (pipe.eventQueue.size() == 0) {

					if (pipe.writeEndClosed) {

						break;

					}

					try {

						pipe.wait();

					} catch (InterruptedException e) {
					}

				}

				return pipe.eventQueue.size() > 0;

			}

		}

		public synchronized XdmValue peek() {

			if (closed) {

				return null;

			}

			synchronized (pipe) {

				// wait until the queue has more events
				while (pipe.eventQueue.size() == 0) {

					if (pipe.writeEndClosed) {

						return null;

					}

					try {

						pipe.wait();

					} catch (InterruptedException e) {
						mLogger.error("Interrupted exception in pipe" , e );
					}

				}

				return  pipe.eventQueue.get(0);

			}

		}

		public synchronized void close()  {

			if (closed) {

				return;

			}

			synchronized (pipe) {

				pipe.readEndClosed = true;
				pipe.notifyAll();

			}
			
			closed = true ;

		}

		public void finalize() {

			if (!closed) {

				synchronized (pipe) {

					pipe.readEndClosed = true;
					pipe.notifyAll();

				}
				closed = true ;
			}

		}

		@Override
		public XdmItem read() throws CoreException {
			return nextItem();
		}

	}

}




//
//
//Copyright (C) 2008,2009,2010,2011 David A. Lee.
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
