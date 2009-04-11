/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.SynchronizedInputStream;
import org.xmlsh.util.Util;

/*
 * An InputPort represents an input source of data, either Stream (bytes) or XML
 * data
 * 
 */

public class StreamInputPort extends InputPort {

	// An Input Port may be either a Stream or an XML value
	private InputStream mStream;

	public StreamInputPort(InputStream is, String systemId ) {
		mStream = is;
		this.setSystemId(systemId);

	}

	public synchronized InputStream asInputStream(SerializeOpts opts)
			throws InvalidArgumentException, SaxonApiException, IOException {

		return mStream == null ? null : new SynchronizedInputStream(mStream);

	}

	public synchronized void close() throws IOException {

		if (mStream != null)
			mStream.close();

	}

	public synchronized Source asSource(SerializeOpts opts) throws InvalidArgumentException,
			SaxonApiException, IOException {

		Source s = new StreamSource(asInputStream(opts));
		s.setSystemId(getSystemId());
		return s;
	}

	public synchronized XdmNode asXdmNode(SerializeOpts opts) throws SaxonApiException,
			InvalidArgumentException, IOException {

		net.sf.saxon.s9api.DocumentBuilder builder = Shell.getProcessor().newDocumentBuilder();
		return builder.build(asSource(opts));
	}

	
	public boolean isStream() {
		return true;
	}

	public void copyTo(OutputStream out, SerializeOpts opts) throws IOException, SaxonApiException,
			InvalidArgumentException {

		Util.copyStream(mStream, out);

	}

	@Override
	public XMLEventReader asXMLEventReader(SerializeOpts opts) throws CoreException {
		try {
		XMLInputFactory factory = XMLInputFactory.newInstance();
		if( ! opts.isSupports_dtd())
			factory.setProperty(XMLInputFactory.SUPPORT_DTD, "false");
		
		return factory.createXMLEventReader( getSystemId() , asInputStream(opts));
		} catch (Exception e)
		{
			throw new CoreException( e );
		}
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
