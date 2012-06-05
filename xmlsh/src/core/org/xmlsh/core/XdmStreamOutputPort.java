/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import javanet.staxutils.ContentHandlerToXMLStreamWriter;
import javanet.staxutils.XMLStreamEventWriter;

import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import net.sf.saxon.s9api.BuildingStreamWriter;
import net.sf.saxon.s9api.Destination;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmDestination;
import net.sf.saxon.trans.XPathException;
import org.xml.sax.ContentHandler;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.sh.shell.Shell;

public class XdmStreamOutputPort extends OutputPort {
 
	private IXdmItemWriter mWriter ;
	
	// Set to true if any asXXX method was caused which used a non-xml stream or access 
	private		boolean		mAsText = false ;
	
	// Transient classes 
	private		XdmDestination	 		mXdmDestination;
	private		ByteArrayOutputStream 	mByteArrayOutputStream;
	private		BuildingStreamWriter	mBuilder;
	private		SerializeOpts 			mSerializeOpts; 	// for converting from ByteArray to string  
	
	
	
	public XdmStreamOutputPort(IXdmItemWriter writer, SerializeOpts opts) {
		mWriter = writer;
	}

	@Override
	public OutputStream asOutputStream(SerializeOpts opts) {
		mSerializeOpts = opts ;
		mAsText = true ;
		return ( mByteArrayOutputStream = new ByteArrayOutputStream()); 	// BOS is synchroized 
	}

	@Override
	public synchronized void flush() throws  CoreException, SaxonApiException
	{
			
			
			if (mXdmDestination != null)
				mWriter.write( mXdmDestination.getXdmNode());

			
			// else
			if (mByteArrayOutputStream != null)
				try {
					mWriter.write( new XValue(mByteArrayOutputStream.toString(mSerializeOpts.getOutputTextEncoding()   )).asXdmValue() );
				} catch (UnsupportedEncodingException e1) {
					throw new CoreException( e1 );
				}

			//else
			if (mBuilder != null)
				mWriter.write(mBuilder.getDocumentNode());
				
		
			mXdmDestination = null;
			mByteArrayOutputStream = null ;
			mBuilder = null ;
		
	}
	@Override
	public void close() throws CoreException {
		mWriter.close();

	}

	@Override
	public synchronized PrintStream asPrintStream(SerializeOpts opts)
	{
		mAsText = true ;
		return new PrintStream(asOutputStream(opts));
	}

	@Override
	public synchronized Destination asDestination(SerializeOpts opts) throws InvalidArgumentException
	{
			// mVariable.clear();
			mXdmDestination = new XdmDestination();
		
			return mXdmDestination;
	}
	

	@Override
	public void writeSequenceSeperator(SerializeOpts serializeOpts) throws IOException,
			SaxonApiException, CoreException {
		flush();

	}

	@Override
	public void writeSequenceTerminator(SerializeOpts serializeOpts) throws IOException, CoreException, SaxonApiException {
		flush();


	}

	@Override
	public synchronized XMLStreamWriter asXMLStreamWriter(SerializeOpts opts) throws SaxonApiException {
	
		Processor proc = Shell.getProcessor();
		BuildingStreamWriter bw = proc.newDocumentBuilder().newBuildingStreamWriter();
		
		
		mBuilder = bw;
		return bw;
		
		
	}

	@Override
	public XMLEventWriter asXMLEventWriter(SerializeOpts opts) throws InvalidArgumentException,
			 SaxonApiException {
		XMLStreamWriter sw = asXMLStreamWriter(opts);
		return new XMLStreamEventWriter( sw );
		
		
	}

	@Override
	public IXdmItemOutputStream asXdmItemOutputStream(SerializeOpts opts) throws CoreException {
		// TODO Auto-generated method stub
		return mWriter;
	}

	@Override
	public ContentHandler asContentHandler(SerializeOpts opts) throws XPathException,
			SaxonApiException {
		XMLStreamWriter sw = asXMLStreamWriter(opts);
		return new ContentHandlerToXMLStreamWriter(sw);
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
