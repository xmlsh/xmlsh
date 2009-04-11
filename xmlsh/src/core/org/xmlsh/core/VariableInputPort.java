/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;

import net.sf.saxon.Configuration;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.pull.PullFromIterator;
import net.sf.saxon.pull.PullToStax;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.Util;

/*
 * An InputPort represents an input source of data, either Stream (bytes) or XML
 * data
 * 
 */

public class VariableInputPort extends InputPort {

	private XVariable mVariable;

	/*
	 * Standard input stream - created on first request
	 */

	public VariableInputPort(XVariable value) {
		mVariable = value;

	}

	public synchronized InputStream asInputStream(SerializeOpts opts)
			throws InvalidArgumentException, SaxonApiException, IOException {
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		if (mVariable.getValue().isXExpr())
			Util.writeXdmValue(mVariable.getValue().asXdmNode(), Util
					.streamToDestination(buf, opts));
		else
			buf.write(mVariable.getValue().toBytes(opts.getEncoding()));
		return new ByteArrayInputStream(buf.toByteArray());

	}

	public synchronized void close() throws IOException {

	}

	public synchronized Source asSource(SerializeOpts opts) throws InvalidArgumentException,
			SaxonApiException, IOException {

		return mVariable.getValue().asSource();

	}

	public synchronized XdmNode asXdmNode(SerializeOpts opts) throws SaxonApiException,
			InvalidArgumentException, IOException {
		return mVariable.getValue().asXdmNode();

	}



	public boolean isStream() {
		return false;
	}

	public void copyTo(OutputStream out, SerializeOpts opts) throws IOException, SaxonApiException,
			InvalidArgumentException {
		if (mVariable.getValue().isXExpr())
			Util.writeXdmValue(mVariable.getValue().asXdmNode(), Util
					.streamToDestination(out, opts));
		else
			out.write(mVariable.getValue().toString().getBytes(opts.getText_encoding()));

	}

	@Override
	public XMLEventReader asXMLEventReader(SerializeOpts opts) throws XMLStreamException, CoreException
	{
		/*
		 * @TODO: Look at TinyTreeEventIterator and EventIterator and EventToStaxBridge
		 */
		/*
		
		XMLEventWriterBuffer buffer = new XMLEventWriterBuffer();
		NodeInfo node = mVariable.getValue().asXdmNode().getUnderlyingNode();
		
		StAXUtils.copy( node , buffer);
		
		
		//return XMLInputFactory.newInstance().createXMLStreamReader( buffer.getReader() );
		return  buffer.getReader();
		*/
		return XMLInputFactory.newInstance().createXMLEventReader(asXMLStreamReader(opts));
		
		
			
	}
	

	@Override
	public XMLStreamReader asXMLStreamReader(SerializeOpts opts) throws InvalidArgumentException,
			CoreException, XMLStreamException 
	{
		
		
		/*
		XMLEventReader reader = asXMLEventReader(opts);	
		return new XMLEventStreamReader( reader );
		*/
		// See EventToStaxBridge
		/*
		Configuration config = Shell.getProcessor().getUnderlyingConfiguration();
		PipelineConfiguration pipe = config.makePipelineConfiguration();
		pipe.setHostLanguage(Configuration.XQUERY);
		Decomposer iter = new Decomposer( mVariable.getValue().asNodeInfo() , pipe);
		
		XMLStreamReader sr = new EventToStaxBridge2(iter, config.getNamePool());
		return sr;
		*/
		XValue value = mVariable.getValue();
		
		//System.err.println("sysid: " + this.getSystemId() );
		//System.err.println("base: " + value.asXdmNode().getBaseURI());
	
		Configuration config = Shell.getProcessor().getUnderlyingConfiguration();


		SequenceIterator iter = value.asSequenceIterator();
		
		PullFromIterator pull = new PullFromIterator(	iter );
		pull.setPipelineConfiguration( config.makePipelineConfiguration());
		PullToStax ps = new PullToStax(  pull );
		
		// TODO: Bug in Saxon 9.1.0.6 
		// PullToStax starts in state 0 not state START_DOCUMENT
		if( ps.getEventType() == 0 )
			ps.next();
		
		return ps;

		

	}

}

//
//
// Copyright (C) 2008,2009 , David A. Lee.
//
// The contents of this file are subject to the "Simplified BSD License" (the
// "License");
// you may not use this file except in compliance with the License. You may
// obtain a copy of the
// License at http://www.opensource.org/licenses/bsd-license.php
//
// Software distributed under the License is distributed on an "AS IS" basis,
// WITHOUT WARRANTY OF ANY KIND, either express or implied.
// See the License for the specific language governing rights and limitations
// under the License.
//
// The Original Code is: all this file.
//
// The Initial Developer of the Original Code is David A. Lee
//
// Portions created by (your name) are Copyright (C) (your legal entity). All
// Rights Reserved.
//
// Contributor(s): none.
//
