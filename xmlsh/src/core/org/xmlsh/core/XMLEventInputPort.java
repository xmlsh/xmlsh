/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.core;

import java.io.InputStream;
import java.io.OutputStream;

import javanet.staxutils.io.StreamEventWriter;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.stax.StAXSource;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.XMLEventInputStream;

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

		return new XMLEventInputStream( mReader , opts );
		

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
		try {
			s = new StAXSource( mReader );
		} catch (XMLStreamException e) {
			throw new CoreException(e);
		}
		s.setSystemId(getSystemId());
		return s;
	}

	public synchronized XdmNode asXdmNode(SerializeOpts opts) throws CoreException {

		net.sf.saxon.s9api.DocumentBuilder builder = Shell.getProcessor().newDocumentBuilder();
		try {
			return builder.build(asSource(opts));
		} catch (SaxonApiException e) {
			throw new CoreException(e);
		}
	}

	
	public boolean isStream() {
		return true;
	}

	public void copyTo(OutputStream out, SerializeOpts opts) throws CoreException {

		StreamEventWriter writer = new StreamEventWriter(out);
		
		try {
			writer.add(mReader);
			writer.close();
		} catch (XMLStreamException e) {
			throw new CoreException(e);
		}
	
		

	}

	@Override
	public XMLEventReader asXMLEventReader(SerializeOpts opts) throws CoreException {
		return mReader;
	}

	@Override
	public XMLStreamReader asXMLStreamReader(SerializeOpts opts) throws CoreException {
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
