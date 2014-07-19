/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.core;

import net.sf.saxon.s9api.BuildingStreamWriter;
import net.sf.saxon.s9api.Destination;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmDestination;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.trans.XPathException;
import org.xml.sax.ContentHandler;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.sh.shell.Shell;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import javanet.staxutils.ContentHandlerToXMLStreamWriter;
import javanet.staxutils.XMLStreamEventWriter;

import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamWriter;

/*
 * An OutputPort represents an output sync of data, either Stream (bytes) or XML data
 * 
 */




public class VariableOutputPort extends OutputPort
{
	
	// Set to true if any asXXX method was caused which used a non-xml stream or access 
	private		boolean		mAsText = false ;
	
	
	private class VariableXdmItemOutputStream extends AbstractXdmItemOutputStream 
	{
		
		@Override
		public void write(XdmItem item) throws CoreException {
			
				appendVar(item);
			
		}

	}
	
	
	
	
	
	
	
	private	 XVariable		 mVariable;

	// Transient classes 
	private		XdmDestination	 		mXdmDestination;
	private		ByteArrayOutputStream 	mByteArrayOutputStream;
	private		BuildingStreamWriter	mBuilder;
	private		SerializeOpts 			mSerializeOpts; 	// for converting from ByteArray to string  
	
	
	
	
	
	
	
	
	
	
	
	
	public VariableOutputPort( XVariable var)
	{
		mVariable = var ;
	}
	
	
	/*
	 * Standard input stream - created on first request
	 */

	@Override
	public	synchronized OutputStream asOutputStream(	SerializeOpts serializeOpts  )
	{
		mAsText = true ;
		/*
		 * If going to a variable, then create a variable stream
		 */
		mSerializeOpts = serializeOpts ;
		return ( mByteArrayOutputStream = new ByteArrayOutputStream()); 	// BOS is synchroized 
	}

	@Override
	public synchronized void flush() throws  CoreException, SaxonApiException
	{
			
			
			if (mXdmDestination != null)
				appendVar( mXdmDestination.getXdmNode());

			
			// else
			if (mByteArrayOutputStream != null)
				try {
					appendVar( mByteArrayOutputStream.toString(mSerializeOpts.getOutputTextEncoding()   ) );
				} catch (UnsupportedEncodingException e1) {
					throw new CoreException( e1 );
				}

			//else
			if (mBuilder != null)
				appendVar(mBuilder.getDocumentNode());
				
		
			mXdmDestination = null;
			mByteArrayOutputStream = null ;
			mBuilder = null ;
		
	}
	
	

	@Override
	public synchronized void close() throws CoreException {
		
		
		
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
			mXdmDestination = newXdmDestination();
		
			return mXdmDestination;
	}
	
	
	
	
	
	private XdmDestination newXdmDestination() {
		XdmDestination dest = new XdmDestination();
	    // setupDestination(dest);
	    return dest;
		
	}

	public synchronized PrintWriter asPrintWriter(SerializeOpts opts) throws UnsupportedEncodingException {
		mAsText = true ;
		
		return new PrintWriter( 		
				new OutputStreamWriter(asOutputStream(opts) , 
						opts.getOutputTextEncoding() ));
	}

	private void appendVar(String string) throws InvalidArgumentException 
	{

		XValue value = mVariable.getValue();
		if (value == null)
			mVariable.setValue(new XValue(string));
		else {
			if (value.isAtomic())
				mVariable.setValue(new XValue(value.toString() + string));
			else {
				mVariable.setValue(value.append(new XdmAtomicValue(string)));
			}
		}
		
		
	}
	/*
	 * Append an item to the current output
	 */
	private void appendVar( XdmItem xitem ) throws InvalidArgumentException
	{
		if( xitem instanceof XdmNode ){
			XdmNode node = (XdmNode)xitem;
			node.getUnderlyingNode().setSystemId(getSystemId());
		}
		
		XValue value = mVariable.getValue();
		if (value == null)
			mVariable.setValue(new XValue(xitem));
		else {
			mVariable.setValue( value.append(xitem));
		}

	}
	
	public synchronized void writeSequenceSeperator(SerializeOpts opts) throws IOException, InvalidArgumentException, SaxonApiException
	{
			if( mXdmDestination != null ){
				appendVar(mXdmDestination.getXdmNode() );
				
				mXdmDestination.reset();
			
			}
			else
			if( mBuilder != null ){
				appendVar( mBuilder.getDocumentNode() );
				mBuilder = null ; // close ?
			
			}
		
	}

	@Override
	public void writeSequenceTerminator(SerializeOpts opts) throws IOException {
		
	}


	@Override
	public synchronized XMLStreamWriter asXMLStreamWriter(SerializeOpts opts) throws SaxonApiException {
	
		Processor proc = Shell.getProcessor();
		BuildingStreamWriter bw = proc.newDocumentBuilder().newBuildingStreamWriter();
		
		
		mBuilder = bw;
		return bw;
		
		
	}


	/* (non-Javadoc)
	 * @see org.xmlsh.core.OutputPort#asXMLEventWriter(org.xmlsh.sh.shell.SerializeOpts)
	 */
	@Override
	public XMLEventWriter asXMLEventWriter(SerializeOpts opts) throws InvalidArgumentException, SaxonApiException {
		
		XMLStreamWriter sw = asXMLStreamWriter(opts);
		return new XMLStreamEventWriter( sw );
		
		
		
		
		
	}

	
	public	IXdmItemOutputStream	asXdmItemOutputStream(SerializeOpts opts) throws CoreException
	{
		return new VariableXdmItemOutputStream(  );
	}


	/**
	 * @return the asText
	 */
	public boolean isAsText() {
		return mAsText;
	}
	
	/* (non-Javadoc)
	 * @see org.xmlsh.core.OutputPort#asContentHandler(org.xmlsh.sh.shell.SerializeOpts)
	 */
	@Override
	public synchronized ContentHandler asContentHandler(SerializeOpts opts) throws XPathException, SaxonApiException {
	
		XMLStreamWriter sw = asXMLStreamWriter(opts);
		return new ContentHandlerToXMLStreamWriter(sw);

       
		
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
