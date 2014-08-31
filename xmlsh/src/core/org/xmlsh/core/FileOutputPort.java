/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.core;

import net.sf.saxon.s9api.Destination;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.trans.XPathException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.xml.sax.ContentHandler;
import org.xmlsh.sh.shell.SerializeOpts;

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
		mLogger.debug("FileOutputPort() file = "+file.getName());
		mFile = file;
		this.bAppend = bAppend ;
		/*
		 * Need to create the file now if it doesnt exist
		 */

		if( bCreateNow )
			mFile.createNewFile();

	}

	private StreamOutputPort getStreamPort() throws CoreException  {
		if( mStreamPort == null )
			try {

				FileOutputStream fileStream =  new  FileOutputStream(mFile,bAppend) ;
				mStreamPort = new StreamOutputPort( fileStream , true );
				mStreamPort.setSystemId(getSystemId());
			} catch (FileNotFoundException e) {
				mLogger.warn( "Error opening file for output: " + mFile.getAbsolutePath() , e );

			} 

		return mStreamPort;

	}




	/**
	 * @param systemId
	 * @see org.xmlsh.core.AbstractPort#setSystemId(java.lang.String)
	 */
	@Override
	public void setSystemId(String systemId) {
		super.setSystemId(systemId);
		if( mStreamPort != null )
			mStreamPort.setSystemId(systemId);
	}
	/**
	 * @param opts
	 * @return
	 * @throws CoreException 
	 * @see org.xmlsh.core.StreamOutputPort#asOutputStream(org.xmlsh.sh.shell.SerializeOpts)
	 */
	@Override
	public OutputStream asOutputStream(SerializeOpts opts) throws CoreException {
		return getStreamPort().asOutputStream(opts);
	}
	/**
	 * @throws IOException 
	 * @throws CoreException
	 * @see org.xmlsh.core.StreamOutputPort#flush()
	 */
	@Override
	public void flush() throws IOException  {
		if( mStreamPort != null )
			mStreamPort.flush();
	}
	/**
	 * @throws CoreException
	 * @see org.xmlsh.core.StreamOutputPort#close()
	 */
	@Override
	public void close() throws IOException {
		mLogger.debug("FileOutputPort.close() file = "+ mFile.getName());

		if( mStreamPort != null ){
			mStreamPort.close();
			mStreamPort = null ;
		}
	}
	/**
	 * @return
	 * @see org.xmlsh.core.OutputPort#isFile()
	 */
	@Override
	public boolean isFile() {
		return true ;
	}
	/**
	 * @param opts
	 * @return
	 * @throws CoreException 
	 * @see org.xmlsh.core.StreamOutputPort#asPrintStream(org.xmlsh.sh.shell.SerializeOpts)
	 */
	@Override
	public PrintStream asPrintStream(SerializeOpts opts) throws CoreException {
		return getStreamPort().asPrintStream(opts);
	}
	/**
	 * @param opts
	 * @return
	 * @throws CoreException
	 * @see org.xmlsh.core.StreamOutputPort#asDestination(org.xmlsh.sh.shell.SerializeOpts)
	 */
	@Override
	public Destination asDestination(SerializeOpts opts) throws CoreException {
		return getStreamPort().asDestination(opts);
	}
	/**
	 * @param opts
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws CoreException 
	 * @see org.xmlsh.core.StreamOutputPort#asPrintWriter(org.xmlsh.sh.shell.SerializeOpts)
	 */
	@Override
	public PrintWriter asPrintWriter(SerializeOpts opts) throws UnsupportedEncodingException, CoreException {
		return getStreamPort().asPrintWriter(opts);
	}
	/**
	 * @param opts
	 * @throws IOException
	 * @throws CoreException 
	 * @see org.xmlsh.core.StreamOutputPort#writeSequenceSeperator(org.xmlsh.sh.shell.SerializeOpts)
	 */
	@Override
	public void writeSequenceSeperator(SerializeOpts opts) throws IOException,
	CoreException {
		getStreamPort().writeSequenceSeperator(opts);
	}
	/**
	 * @param opts
	 * @throws IOException
	 * @throws CoreException 
	 * @see org.xmlsh.core.StreamOutputPort#writeSequenceTerminator(org.xmlsh.sh.shell.SerializeOpts)
	 */
	@Override
	public void writeSequenceTerminator(SerializeOpts opts) throws IOException, CoreException {
		getStreamPort().writeSequenceTerminator(opts);
	}
	/**
	 * @param opts
	 * @return
	 * @throws SaxonApiException
	 * @throws CoreException 
	 * @see org.xmlsh.core.StreamOutputPort#asXMLStreamWriter(org.xmlsh.sh.shell.SerializeOpts)
	 */
	@Override
	public XMLStreamWriter asXMLStreamWriter(SerializeOpts opts) throws SaxonApiException, CoreException {
		return getStreamPort().asXMLStreamWriter(opts);
	}
	/**
	 * @param opts
	 * @return
	 * @throws XMLStreamException
	 * @throws IOException 
	 * @throws CoreException 
	 * @see org.xmlsh.core.StreamOutputPort#asXMLEventWriter(org.xmlsh.sh.shell.SerializeOpts)
	 */
	@Override
	public XMLEventWriter asXMLEventWriter(SerializeOpts opts) throws XMLStreamException, IOException, CoreException {
		return getStreamPort().asXMLEventWriter(opts);
	}
	/**
	 * @param opts
	 * @return
	 * @throws CoreException
	 * @see org.xmlsh.core.StreamOutputPort#asXdmItemOutputStream(org.xmlsh.sh.shell.SerializeOpts)
	 */
	@Override
	public IXdmItemOutputStream asXdmItemOutputStream(SerializeOpts opts) throws CoreException {
		return getStreamPort().asXdmItemOutputStream(opts);
	}
	/**
	 * @param opts
	 * @return
	 * @throws XPathException
	 * @throws CoreException 
	 * @see org.xmlsh.core.StreamOutputPort#asContentHandler(org.xmlsh.sh.shell.SerializeOpts)
	 */
	@Override
	public ContentHandler asContentHandler(SerializeOpts opts) throws XPathException, CoreException {
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
//Copyright (C) 2008-2014 David A. Lee.
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
