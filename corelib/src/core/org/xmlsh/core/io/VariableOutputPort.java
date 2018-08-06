/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.core.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;

import javanet.staxutils.ContentHandlerToXMLStreamWriter;
import javanet.staxutils.XMLStreamEventWriter;

import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamWriter;

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
import org.xmlsh.core.CoreException;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.XValue;
import org.xmlsh.core.XVariable;
import org.xmlsh.json.JSONUtils;
import org.xmlsh.json.JSONUtils.JsonNodeBuilder;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.types.TypeFamily;
import org.xmlsh.util.Util;

import com.fasterxml.jackson.core.Base64Variant;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonGenerator.Feature;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.base.GeneratorBase;
import com.fasterxml.jackson.core.json.JsonWriteContext;
import com.fasterxml.jackson.core.util.JsonGeneratorDelegate;
import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.jsonpath.internal.Utils;

/*
 * An OutputPort represents an output sync of data, either Stream (bytes) or XML data
 * 
 */




public class VariableOutputPort extends OutputPort 
{



	public class VariableJsonGenerator extends JsonGeneratorDelegate {
		
		@Override
		public void close() throws IOException {
			JsonNodeBuilder b = getNodeBuilder();
			super.close();
			try {
				appendVar(XValue.newXValue(TypeFamily.JSON  , b.build() ));
			} catch (InvalidArgumentException e) {
				Util.wrapIOException(e);
			}
		}
		public VariableJsonGenerator() {
			super( JSONUtils.createJsonNodeBuilder());
			// TODO Auto-generated constructor stub
		}
		JsonNodeBuilder getNodeBuilder() { return (JsonNodeBuilder) getDelegate(); }
	}



	// Set to true if any asXXX method was caused which used a non-xml stream or access 
	private		boolean		mAsText = false ;


	private class VariableXdmItemOutputStream extends AbstractXdmItemOutputStream 
	{
		@Override
		public void write(XdmItem item) throws IOException  {

			try {
				appendVar(item);
			} catch (InvalidArgumentException e) {
				throw new IOException(e);
			}
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
	  assert( var != null );
		mVariable = var ;
	}


	/*
	 * Standard input stream - created on first request
	 */

	@Override
	public	synchronized OutputStream asOutputStream(	SerializeOpts serializeOpts  ) throws IOException
	{
		flush();
		mAsText = true ;
		/*
		 * If going to a variable, then create a variable stream
		 */
		mSerializeOpts = serializeOpts ;
		return ( mByteArrayOutputStream = new ByteArrayOutputStream()); 	// BOS is synchroized 
	}

	@Override
	public synchronized void flush() throws IOException 
	{


		try {
			if (mXdmDestination != null)
				appendVar( mXdmDestination.getXdmNode());


			// else
			if (mByteArrayOutputStream != null)
				appendVar( mByteArrayOutputStream.toString(mSerializeOpts.getOutputTextEncoding()   ) );

			//else
			if (mBuilder != null)
				appendVar(mBuilder.getDocumentNode());
		} catch (InvalidArgumentException | UnsupportedEncodingException
				| SaxonApiException e) {
			Util.wrapIOException("Exception flushing VariableOutputPort" , e );
		} finally {
			mXdmDestination = null;
			mByteArrayOutputStream = null ;
			mBuilder = null ;
		}
	}



	@Override
	public synchronized void close() throws IOException  {
		flush();
	}



	@Override
	public synchronized PrintStream asPrintStream(SerializeOpts opts) throws IOException
	{
		flush();
		mAsText = true ;
		return new PrintStream(asOutputStream(opts));
	}

	@Override
	public synchronized Destination asDestination(SerializeOpts opts) throws InvalidArgumentException, IOException
	{
		flush();
		// mVariable.clear();
		mXdmDestination = newXdmDestination();
		return mXdmDestination;
	}





	private XdmDestination newXdmDestination() {
		
		XdmDestination dest = new XdmDestination();
		// setupDestination(dest);
		return dest;

	}

	@Override
	public synchronized PrintWriter asPrintWriter(SerializeOpts opts) throws IOException {
		flush();
 
		mAsText = true ;

		return new PrintWriter( 		
				new OutputStreamWriter(asOutputStream(opts) , 
						opts.getOutputTextEncoding() ));
	}

	/*
	private void appendVar(String string) throws InvalidArgumentException 
	{

		XValue value = mVariable.getValue();
		if (value == null)
			mVariable.setValue(new XValue(string));
		else {
			mVariable.setValue( value.append(XValue.asXValue(string)));
		}


	}
	 */

	private void appendVar(String string) throws InvalidArgumentException 
	{
		XValue value = mVariable.getValue();
		if (value == null || value.isNull())
			mVariable.setValue(XValue.newXValue(string));
		else {
			if (value.isAtomic())
				mVariable.setValue(XValue.newXValue(value.toString() + string));
			else {
				mVariable.setValue(value.append(new XdmAtomicValue(string)));
			}
		}


	}
	private void appendVar(XValue xv) throws InvalidArgumentException 
	{
		XValue value = mVariable.getValue();
		if (value == null || value.isNull())
			mVariable.setValue(xv);
		else {
			mVariable.setValue(value.append(xv));
		}


	}

	/*
	 * Append an item to the current output
	 */
	private void appendVar( XdmItem xitem ) throws InvalidArgumentException
	{
	/* Saxon 6.9 - nodes systemid is imutable

		  if( xitem instanceof XdmNode ){
			XdmNode node = (XdmNode)xitem;
			node.getUnderlyingNode().setSystemId(getSystemId());
		}
*/
		XValue value = mVariable.getValue();
		if (value == null)
			mVariable.setValue(XValue.newXValue(xitem));
		else {
			mVariable.setValue( value.append(xitem));
		}
	}

	@Override
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
		flush();
	}


	@Override
	public synchronized XMLStreamWriter asXMLStreamWriter(SerializeOpts opts) throws SaxonApiException, IOException {
		flush();

		Processor proc = Shell.getProcessor();
		BuildingStreamWriter bw = proc.newDocumentBuilder().newBuildingStreamWriter();
		mBuilder = bw;
		return bw;


	}


	/* (non-Javadoc)
	 * @see org.xmlsh.core.OutputPort#asXMLEventWriter(org.xmlsh.sh.shell.SerializeOpts)
	 */
	@Override
	public XMLEventWriter asXMLEventWriter(SerializeOpts opts) throws InvalidArgumentException, SaxonApiException, IOException {
		flush();
		XMLStreamWriter sw = asXMLStreamWriter(opts);
		return new XMLStreamEventWriter( sw );
	}


	@Override
	public	IXdmItemOutputStream	asXdmItemOutputStream(SerializeOpts opts) throws CoreException, IOException
	{
		flush();
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
	public synchronized ContentHandler asContentHandler(SerializeOpts opts) throws XPathException, SaxonApiException, IOException {
		flush();
		XMLStreamWriter sw = asXMLStreamWriter(opts);
		return new ContentHandlerToXMLStreamWriter(sw);
	}
	
	@Override
	public JsonGenerator asJsonGenerator(SerializeOpts opts)
			throws IOException, CoreException {
		return new VariableJsonGenerator();
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
