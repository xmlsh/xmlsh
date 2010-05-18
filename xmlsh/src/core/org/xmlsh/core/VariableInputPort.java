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
import net.sf.saxon.evpull.Decomposer;
import net.sf.saxon.evpull.EventToStaxBridge;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import org.xml.sax.InputSource;
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

	public VariableInputPort(XVariable value) throws InvalidArgumentException {
		mVariable = value;
		if( ! value.getValue().isAtomic() )
			this.setSystemId( value.getValue().asXdmNode().getBaseURI().toString() );
	}

	public synchronized InputStream asInputStream(SerializeOpts opts)
			throws  CoreException{
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		try {
			if (mVariable.getValue().isXExpr())
				Util.writeXdmValue(mVariable.getValue().asXdmNode(), Util
						.streamToDestination(buf, opts));
			else
				buf.write(mVariable.getValue().toBytes(opts.getEncoding()));
			return new ByteArrayInputStream(buf.toByteArray());
		} catch (SaxonApiException e) {
			throw new CoreException(e);
		} catch (IOException e) {
			throw new CoreException(e);
		}

	}

	public synchronized void close() throws CoreException {

	}

	public synchronized Source asSource(SerializeOpts opts) throws CoreException{

		return mVariable.getValue().asSource();

	}
	
	public synchronized InputSource asInputSource(SerializeOpts opts) throws CoreException{

		InputSource in = new InputSource(asInputStream(opts));
		in.setSystemId(this.getSystemId());
		return in;

	}
	
	

	public synchronized XdmNode asXdmNode(SerializeOpts opts) throws CoreException {
		return mVariable.getValue().asXdmNode();

	}
	
	public  synchronized XdmItem asXdmItem(SerializeOpts serializeOpts) throws CoreException
	{
		return mVariable.getValue().asXdmItem();
	}




	public boolean isStream() {
		return false;
	}

	public void copyTo(OutputStream out, SerializeOpts opts) throws CoreException , IOException {
		if (mVariable.getValue().isXExpr())
			try {
				Util.writeXdmValue(mVariable.getValue().asXdmNode(), Util
						.streamToDestination(out, opts));
			} catch (SaxonApiException e) {
				throw new CoreException(e);
			}
		else
			out.write(mVariable.getValue().toString().getBytes(opts.getText_encoding()));

	}

	@Override
	public XMLEventReader asXMLEventReader(SerializeOpts opts) throws CoreException
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
		try {
			return XMLInputFactory.newInstance().createXMLEventReader(asXMLStreamReader(opts));
		} catch (Exception e){
			throw new CoreException(e);
		}
		
		
			
	}
	

	@Override
	public XMLStreamReader asXMLStreamReader(SerializeOpts opts) throws CoreException
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

		
		
		
								
		// SequenceIterator iter = value.asSequenceIterator();
		NodeInfo nodeInfo = value.asNodeInfo();
		
		/*
		 * 2010-05-19 - EventReaders assume documents, if not a document then wrap with one
		 */
		if( nodeInfo.getNodeKind() != net.sf.saxon.type.Type.DOCUMENT )
			nodeInfo = Util.wrapDocument( nodeInfo ) ;
	
		
		
		
		Decomposer decomposed = new Decomposer( nodeInfo , config.makePipelineConfiguration()  );
		
		// EventIteratorOverSequence eviter = new EventIteratorOverSequence(iter);
		
		
		EventToStaxBridge ps = new EventToStaxBridge(	decomposed , config.makePipelineConfiguration() );
		
		
		
		// TODO: Bug in Saxon 9.1.0.6 
		// PullToStax starts in state 0 not state START_DOCUMENT
		if( ps.getEventType() == 0 )
			try {
				ps.next();
			} catch (XMLStreamException e) {
				throw new CoreException(e);
			}
		
		
		return ps;

		

	}

}

//
//
// Copyright (C) 2008,2009,2010 , David A. Lee.
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
