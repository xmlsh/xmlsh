/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javanet.staxutils.StAXSource;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.Source;

import com.jayway.jsonpath.JsonModel;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import org.xml.sax.InputSource;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.Util;
import org.xmlsh.util.XMLEventInputStream;
import org.xmlsh.util.XMLEventStreamReader;

/*
 * An InputPort represents an input source of data, either Stream (bytes) or XML
 * data
 * 
 */

public class XMLEventInputPort extends InputPort {

	// An Input Port may be either a Stream or an XML value
	private XMLEventReader mReader;

	public XMLEventInputPort(XMLEventReader is, String systemId ) {
		mReader = is;
		this.setSystemId(systemId);

	}

	public synchronized InputStream asInputStream(SerializeOpts opts)
			throws CoreException {

		return new XMLEventInputStream( mReader , opts ,true);
		

	}

	public synchronized void close() throws CoreException {

		try {
			mReader.close();
		} catch (XMLStreamException e) {
			throw new CoreException(e);
		}

	}

	public synchronized Source asSource(SerializeOpts opts) throws CoreException {

		
		Source s;
		s = new StAXSource( asXMLEventReader(opts) );
		s.setSystemId(getSystemId());
		return s;
	}


	public synchronized InputSource asInputSource(SerializeOpts opts) throws CoreException {

		
		InputSource in = new InputSource(asInputStream(opts));
		in.setSystemId(this.getSystemId());
		return in;

	}
	
	
	public synchronized XdmNode asXdmNode(SerializeOpts opts) throws CoreException {

		net.sf.saxon.s9api.DocumentBuilder builder = Shell.getProcessor().newDocumentBuilder();
		try {
			return builder.build(asSource(opts));
		} catch (SaxonApiException e) {
			throw new CoreException(e);
		}
	}
	

	
	/* (non-Javadoc)
	 * @see org.xmlsh.core.InputPort#asXdmItem(org.xmlsh.sh.shell.SerializeOpts)
	 */
	@Override
	public XdmItem asXdmItem(SerializeOpts serializeOpts) throws CoreException {
		return asXdmNode(serializeOpts);
	}

	public boolean isStream() {
		return true;
	}

	public void copyTo(OutputStream out, SerializeOpts opts) throws CoreException {
		InputStream in = new XMLEventInputStream( mReader , opts ,true);
		try {
			Util.copyStream(in,out);
		} catch (IOException e) {
			throw new CoreException(e);
		}
		
		
		/*
		StreamEventWriter writer = new StreamEventWriter(out);
		
		try {
			writer.add(asXMLEventReader(opts));
			writer.close();
		} catch (XMLStreamException e) {
			throw new CoreException(e);
		}
		*/
	
		

	}

	@Override
	public XMLEventReader asXMLEventReader(SerializeOpts opts) throws CoreException {
		/*
		 * In the case of writer writing text but reader expecting a node.
		 * Detect this (first event is Charactors) and put a parser between the charactors
		 * and the event reader.
		 */
		try {
			XMLEvent event = mReader.peek() ;
			if( event == null )
				// EOF - let fall through
				return mReader ;
			
			if( event.getEventType() != XMLEvent.CHARACTERS )
				return mReader ;
			
			// Text events in a XML pipe - layer with a InputStream then a parser
			InputStream 	is = asInputStream(opts);
			XMLInputFactory factory = XMLInputFactory.newInstance();
			if( ! opts.isSupports_dtd())
				factory.setProperty(XMLInputFactory.SUPPORT_DTD, "false");
			
			return factory.createXMLEventReader( getSystemId() , is);
			
			
			
				
		} catch (XMLStreamException e) {
			throw new CoreException(e);
		}
		// SNH
	}

	@Override
	public XMLStreamReader asXMLStreamReader(SerializeOpts opts) throws CoreException {
		return new XMLEventStreamReader(asXMLEventReader(opts));
		
		
	}
	
	@Override
	public JsonModel asJson(SerializeOpts serializeOpts) throws IOException, CoreException {
		return JsonModel.create( asInputStream(serializeOpts));
	}

	

}

//
//
// Copyright (C) 2008-2013    David A. Lee.
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
