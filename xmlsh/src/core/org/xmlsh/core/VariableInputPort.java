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
import org.xmlsh.json.JSONUtils;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.S9Util;
import org.xmlsh.util.Util;

import com.fasterxml.jackson.databind.JsonNode;

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
		if( value.getValue().isXdmNode()  )
			setSystemId( value.getValue().asXdmNode().getBaseURI().toString() );
	}

	@Override
	public synchronized InputStream asInputStream(SerializeOpts opts)
			throws  CoreException{
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		try {

			XValue value = mVariable.getValue();
			if( value.isJson()) {
				return JSONUtils.asInputStream( value.asJson() , opts );
			}


			if (value.isXdmItem())
				Util.writeXdmItem(value.asXdmItem(), Util
						.streamToDestination(buf, opts)); // uses output xml encoding
			else
				buf.write(value.toByteArray(opts)); // Use output encoding
			return new ByteArrayInputStream(buf.toByteArray());
		} catch (SaxonApiException e) {
			throw new CoreException(e);
		} catch (IOException e) {
			throw new CoreException(e);
		}

	}

	@Override
	public synchronized void close()  {

	}

	@Override
	public synchronized Source asSource(SerializeOpts opts) throws CoreException{

		return mVariable.getValue().asSource();

	}

	@Override
	public synchronized InputSource asInputSource(SerializeOpts opts) throws CoreException{

		InputSource in = new InputSource(asInputStream(opts));
		in.setSystemId(getSystemId());
		return in;

	}



	@Override
	public synchronized XdmNode asXdmNode(SerializeOpts opts) throws CoreException {
		return mVariable.getValue().asXdmNode();

	}

	@Override
	public  synchronized XdmItem asXdmItem(SerializeOpts serializeOpts) throws CoreException
	{
		return mVariable.getValue().asXdmItem();
	}




	public boolean isStream() {
		return false;
	}

	@Override
	public void copyTo(OutputStream out, SerializeOpts opts) throws CoreException , IOException {
		XValue value = mVariable.getValue();
		assert ! value.isNull();
		if( value.isNull() )
			;
		else
			if (value.isXdmItem())
				try {
					Util.writeXdmItem(value.asXdmItem(), Util
							.streamToDestination(out, opts));
				} catch (SaxonApiException e) {
					throw new CoreException(e);
				}
			else
				out.write(value.toString().getBytes(opts.getOutputTextEncoding()));

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

		/*
		 * IF variable is an atomic value then treat as string and parse to XML
		 */

		if( value.isAtomic() ){

			try {
				XMLInputFactory factory = XMLInputFactory.newInstance();
				if( ! opts.isSupports_dtd())
					factory.setProperty(XMLInputFactory.SUPPORT_DTD, "false");
				XMLStreamReader reader =  factory.createXMLStreamReader(getSystemId() , asInputStream(opts));
				return reader;
			} catch (Exception e)
			{
				throw new CoreException( e );
			}


		}



		// SequenceIterator iter = value.asSequenceIterator();
		NodeInfo nodeInfo = value.asNodeInfo();

		/*
		 * 2010-05-19 - EventReaders assume documents, if not a document then wrap with one
		 */
		if( nodeInfo.getNodeKind() != net.sf.saxon.type.Type.DOCUMENT )
			nodeInfo = S9Util.wrapDocument( nodeInfo ).getUnderlyingNode(); ;




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

	@Override
	public IXdmItemInputStream asXdmItemInputStream(SerializeOpts serializeOpts)
			throws CoreException {
		return new ValueXdmItemInputStream( mVariable.getValue() , serializeOpts );
	}


	@Override
	public JsonNode asJson(SerializeOpts serializeOpts) throws IOException, CoreException {
		return mVariable.getValue().asJson();
	}

	@Override
	public boolean isFile() {
		return false;
	}


}

//
//
// Copyright (C) 2008-2014    David A. Lee.
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
