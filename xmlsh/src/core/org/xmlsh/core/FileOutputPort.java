/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import net.sf.saxon.s9api.Destination;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.trans.XPathException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.xml.sax.ContentHandler;
import org.xmlsh.sh.shell.SerializeOpts;




public class FileOutputPort extends OutputPort 
{
	private		static 	Logger	mLogger = LogManager.getLogger( FileOutputPort.class );				
	
	private		StreamOutputPort		mStreamPort = null ;
	private		File					mFile  = null ;
	private		boolean					bAppend ;
	
	
	public	FileOutputPort( File file , boolean bAppend ) throws IOException
	{
		this( file , bAppend , true );
	}
	
	public	FileOutputPort( File file , boolean bAppend , boolean bCreateNow ) throws IOException
	
	
	{
		mFile = file;
		this.bAppend = bAppend ;
		
		/*
		 * Need to create the file now if it doesnt exist
		 */
		
		if( bCreateNow )
			mFile.createNewFile();
		
		
		
	}
	
	private StreamOutputPort getStreamPort()  {
		if( mStreamPort == null )
			try {
				mStreamPort = new StreamOutputPort( new  FileOutputStream(mFile,bAppend) , true );
				mStreamPort.setSystemId(getSystemId());
			} catch (FileNotFoundException e) {
				mLogger.warn( "Error opening file for output: " + mFile.getAbsolutePath() , e );
				
			}
		
		return mStreamPort;
	
	}
	
	


	/**
	 * @param systemId
	 * @see org.xmlsh.core.IPort#setSystemId(java.lang.String)
	 */
	public void setSystemId(String systemId) {
		super.setSystemId(systemId);
		if( mStreamPort != null )
			mStreamPort.setSystemId(systemId);
	}
	/**
	 * @param opts
	 * @return
	 * @see org.xmlsh.core.StreamOutputPort#asOutputStream(org.xmlsh.sh.shell.SerializeOpts)
	 */
	public OutputStream asOutputStream(SerializeOpts opts) {
		return getStreamPort().asOutputStream(opts);
	}
	/**
	 * @throws CoreException
	 * @see org.xmlsh.core.StreamOutputPort#flush()
	 */
	public void flush() throws CoreException {
		if( mStreamPort != null )
			mStreamPort.flush();
	}
	/**
	 * @throws CoreException
	 * @see org.xmlsh.core.StreamOutputPort#close()
	 */
	public void close() throws CoreException {
		if( mStreamPort != null ){
			mStreamPort.close();
			mStreamPort = null ;
		}
	}
	/**
	 * @return
	 * @see org.xmlsh.core.OutputPort#isFile()
	 */
	public boolean isFile() {
		return true ;
	}
	/**
	 * @param opts
	 * @return
	 * @see org.xmlsh.core.StreamOutputPort#asPrintStream(org.xmlsh.sh.shell.SerializeOpts)
	 */
	public PrintStream asPrintStream(SerializeOpts opts) {
		return getStreamPort().asPrintStream(opts);
	}
	/**
	 * @param opts
	 * @return
	 * @throws CoreException
	 * @see org.xmlsh.core.StreamOutputPort#asDestination(org.xmlsh.sh.shell.SerializeOpts)
	 */
	public Destination asDestination(SerializeOpts opts) throws CoreException {
		return getStreamPort().asDestination(opts);
	}
	/**
	 * @param opts
	 * @return
	 * @throws UnsupportedEncodingException
	 * @see org.xmlsh.core.StreamOutputPort#asPrintWriter(org.xmlsh.sh.shell.SerializeOpts)
	 */
	public PrintWriter asPrintWriter(SerializeOpts opts) throws UnsupportedEncodingException {
		return getStreamPort().asPrintWriter(opts);
	}
	/**
	 * @param opts
	 * @throws IOException
	 * @throws InvalidArgumentException
	 * @see org.xmlsh.core.StreamOutputPort#writeSequenceSeperator(org.xmlsh.sh.shell.SerializeOpts)
	 */
	public void writeSequenceSeperator(SerializeOpts opts) throws IOException,
			InvalidArgumentException {
		getStreamPort().writeSequenceSeperator(opts);
	}
	/**
	 * @param opts
	 * @throws IOException
	 * @see org.xmlsh.core.StreamOutputPort#writeSequenceTerminator(org.xmlsh.sh.shell.SerializeOpts)
	 */
	public void writeSequenceTerminator(SerializeOpts opts) throws IOException {
		getStreamPort().writeSequenceTerminator(opts);
	}
	/**
	 * @param opts
	 * @return
	 * @throws SaxonApiException
	 * @see org.xmlsh.core.StreamOutputPort#asXMLStreamWriter(org.xmlsh.sh.shell.SerializeOpts)
	 */
	public XMLStreamWriter asXMLStreamWriter(SerializeOpts opts) throws SaxonApiException {
		return getStreamPort().asXMLStreamWriter(opts);
	}
	/**
	 * @param opts
	 * @return
	 * @throws InvalidArgumentException
	 * @throws XMLStreamException
	 * @see org.xmlsh.core.StreamOutputPort#asXMLEventWriter(org.xmlsh.sh.shell.SerializeOpts)
	 */
	public XMLEventWriter asXMLEventWriter(SerializeOpts opts) throws InvalidArgumentException,
			XMLStreamException {
		return getStreamPort().asXMLEventWriter(opts);
	}
	/**
	 * @param opts
	 * @return
	 * @throws CoreException
	 * @see org.xmlsh.core.StreamOutputPort#asXdmValueOutputStream(org.xmlsh.sh.shell.SerializeOpts)
	 */
	public IXdmValueOutputStream asXdmValueOutputStream(SerializeOpts opts) throws CoreException {
		return getStreamPort().asXdmValueOutputStream(opts);
	}
	/**
	 * @param opts
	 * @return
	 * @throws XPathException
	 * @see org.xmlsh.core.StreamOutputPort#asContentHandler(org.xmlsh.sh.shell.SerializeOpts)
	 */
	public ContentHandler asContentHandler(SerializeOpts opts) throws XPathException {
		return getStreamPort().asContentHandler(opts);
	}

	/* (non-Javadoc)
	 * @see org.xmlsh.core.OutputPort#getFile()
	 */
	@Override
	public File getFile() throws UnimplementedException {
		return mFile ;
	}



}



//
//
//Copyright (C) 2008,2009,2010,2011,2012 David A. Lee.
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
