/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import javanet.staxutils.StAXSource;
import javanet.staxutils.XMLEventStreamWriter;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.sax.TransformerHandler;

import net.sf.saxon.Configuration;
import net.sf.saxon.event.Builder;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.s9api.Destination;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.S9Util;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmDestination;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.tinytree.TinyBuilder;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.Util;
import org.xmlsh.util.XMLEventWriterBuffer;

/*
 * An OutputPort represents an output sync of data, either Stream (bytes) or XML data
 * 
 */




public class VariableOutputPort extends OutputPort
{
	private	 XVariable		 mVariable;

	// Transient classes 
	private		XdmDestination	 		mXdmDestination;
	private		ByteArrayOutputStream 	mByteArrayOutputStream;
	private		Builder					mBuilder;
	private		XMLEventWriterBuffer	mWriterBuffer;

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public VariableOutputPort( XVariable var)
	{
		mVariable = var ;
	}
	
	
	/*
	 * Standard input stream - created on first request
	 */
	
	public	synchronized OutputStream asOutputStream() 
	{
		/*
		 * If going to a variable, then create a variable stream
		 */
			return ( mByteArrayOutputStream = new ByteArrayOutputStream()); 	// BOS is synchroized 
	}

	public synchronized void flush() throws IOException, CoreException
	{
			
			
			if (mXdmDestination != null)
				appendVar( mXdmDestination.getXdmNode());

			
			// else
			if (mByteArrayOutputStream != null)
				appendVar( mByteArrayOutputStream.toString(   ) );

			//else
			if (mBuilder != null)
				appendVar((XdmNode) S9Util.wrapNode(mBuilder.getCurrentRoot()));
			
			if( mWriterBuffer != null ){
		
				DocumentBuilder builder = Shell.getProcessor().newDocumentBuilder();
				XdmNode node;
				try {
					node = builder.build( new StAXSource( mWriterBuffer.getReader()));
				} catch (Exception e) {
					throw new CoreException( e );
				}
				appendVar( node );
			}
			
			
			
			
			mXdmDestination = null;
			mByteArrayOutputStream = null ;
			mBuilder = null ;
			mWriterBuffer = null ;
		
	}
	
	
	
	public synchronized void close() throws IOException, InvalidArgumentException {
		
		
		
	}


	public synchronized TransformerHandler asTransformerHandler(SerializeOpts opts) throws TransformerConfigurationException, IllegalArgumentException, TransformerFactoryConfigurationError
	{
			
			mBuilder = new TinyBuilder();
	        PipelineConfiguration pipe = Shell.getProcessor().getUnderlyingConfiguration().makePipelineConfiguration();
	        mBuilder.setPipelineConfiguration(pipe);
	            
			return Util.getTransformerHander(mBuilder,opts);
			
	}
	
	public synchronized PrintStream asPrintStream()
	{
		return new PrintStream(asOutputStream());
	}

	public synchronized Destination asDestination(SerializeOpts opts) throws InvalidArgumentException
	{
			// mVariable.clear();
			mXdmDestination = newXdmDestination();
		
			return mXdmDestination;
	}
	
	private void setupDestination( XdmDestination dest )
	{
		 /*
		  * TODO: Remove this extra code when Saxon is fixed 
		  * XdmDestinatin shouldn't need the configuration
		  */
		 Configuration config = Shell.getProcessor().getUnderlyingConfiguration();
		 try {
			Receiver r = dest.getReceiver(config);
		    PipelineConfiguration pipe = config.makePipelineConfiguration();

			r.setPipelineConfiguration(pipe);
			;
		} catch (SaxonApiException e) {
			;
		}

	}
	
	
	
	private XdmDestination newXdmDestination() {
		XdmDestination dest = new XdmDestination();
	    setupDestination(dest);
	    return dest;
		
	}

	public synchronized PrintWriter asPrintWriter(SerializeOpts opts) throws UnsupportedEncodingException {
		return new PrintWriter( 		
				new OutputStreamWriter(asOutputStream() , 
						opts.getText_encoding() ));
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
	
	public synchronized void writeSequenceSeperator() throws IOException, InvalidArgumentException
	{
			if( mXdmDestination != null ){
				appendVar(mXdmDestination.getXdmNode() );
				
				mXdmDestination.reset();
				setupDestination( mXdmDestination);
			
			}
			else
			if( mBuilder != null ){
				appendVar( (XdmNode) S9Util.wrapNode(mBuilder.getCurrentRoot()));
			
			}
		
	}

	public void writeSequenceTerminator() throws IOException {
		
	}


	@Override
	public XMLStreamWriter asXMLStreamWriter(SerializeOpts opts) throws XMLStreamException, TransformerConfigurationException, IllegalArgumentException, TransformerFactoryConfigurationError {
		mWriterBuffer = new XMLEventWriterBuffer(); 
		return new XMLEventStreamWriter(mWriterBuffer);
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
