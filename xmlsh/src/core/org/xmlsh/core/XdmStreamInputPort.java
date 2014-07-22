/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.core;

import net.sf.saxon.Configuration;
import net.sf.saxon.evpull.Decomposer;
import net.sf.saxon.evpull.EventToStaxBridge;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;
import org.xml.sax.InputSource;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.JSONUtils;
import org.xmlsh.util.NullInputStream;
import org.xmlsh.util.S9Util;
import org.xmlsh.util.Util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;

import com.fasterxml.jackson.databind.JsonNode;

public class XdmStreamInputPort extends InputPort {

	
	IXdmItemReader  mReader;

	public XdmStreamInputPort(IXdmItemReader reader, SerializeOpts opts) {
		mReader = reader ;
	}

	@Override
	public InputStream asInputStream(SerializeOpts opts) throws CoreException {
		
		XdmValue value = mReader.read();
		if( value == null )
			return new NullInputStream();
		
		
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		try {
			Util.writeXdmValue(value, Util
						.streamToDestination(buf, opts)); // uses output xml encoding
			
			return new ByteArrayInputStream(buf.toByteArray());
		} catch (SaxonApiException e) {
			throw new CoreException(e);
		} 
	}

	@Override
	public void close() {
		

	}

	@Override
	public Source asSource(SerializeOpts opts) throws CoreException {
		return new XValue( mReader.read()).asSource();
	}

	@Override
	public InputSource asInputSource(SerializeOpts opts) throws CoreException {
		InputSource in = new InputSource(asInputStream(opts));
		in.setSystemId(this.getSystemId());
		return in;
	}

	@Override
	public XdmNode asXdmNode(SerializeOpts opts) throws CoreException {
		return  (XdmNode) mReader.read() ;
	}

	@Override
	public void copyTo(OutputStream out, SerializeOpts opts) throws CoreException, IOException {
		XdmItem item ;
		while( ( item = mReader.read()) != null )
			try {
				Util.writeXdmValue(item, Util.streamToDestination(out, opts));
			} catch (SaxonApiException e) {
				throw new CoreException(e);
			}


	}

	@Override
	public XMLEventReader asXMLEventReader(SerializeOpts opts) throws CoreException {
		try {
			return XMLInputFactory.newInstance().createXMLEventReader(asXMLStreamReader(opts));
		} catch (Exception e){
			throw new CoreException(e);
		}
	}

	@Override
	public XMLStreamReader asXMLStreamReader(SerializeOpts opts) throws CoreException {
		
		// TODO: This code was copied from VariableInputPort 
		
         XValue value = new XValue( mReader.read() );
		
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
	public XdmItem asXdmItem(SerializeOpts serializeOpts) throws CoreException {
		return mReader.read();
	}

	// Default implementation uses a singleton as the input stream
	public IXdmItemInputStream asXdmItemInputStream(SerializeOpts serializeOpts)
			throws CoreException {
		
		return mReader ;
	}

	@Override
	public JsonNode asJson(SerializeOpts serializeOpts) throws IOException, CoreException {
		return JSONUtils.readJsonNode( asInputStream(serializeOpts));
	}


}



//
//
//Copyright (C) 2008-2014   David A. Lee.
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
