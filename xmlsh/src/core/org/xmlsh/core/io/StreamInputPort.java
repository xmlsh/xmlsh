/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.core.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.InputSource;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.InputPort;
import org.xmlsh.json.JSONUtils;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.SynchronizedInputStream;
import org.xmlsh.util.Util;

import com.fasterxml.jackson.databind.JsonNode;

/*
 * An InputPort represents an input source of data, either Stream (bytes) or XML
 * data
 * 
 */

public class StreamInputPort extends InputPort  {

	// An Input Port may be either a Stream or an XML value
	private InputStream mStream;
	private Logger mLogger = LogManager.getLogger();

	public StreamInputPort(InputStream is, String systemId ) {
		this(is,systemId,false);
	}

	public StreamInputPort(InputStream is, String systemId, boolean system) {
		mStream = is;
		setSystemId(systemId);
		setSystem(system);;
	}

	@Override
	public synchronized InputStream asInputStream(SerializeOpts opts)
			throws CoreException  {

		return mStream == null ? null : new SynchronizedInputStream(mStream,false);


	}

	@Override
	public synchronized void close() throws IOException {

		if (mStream != null)
			mStream.close();

	}

	@Override
	public synchronized Source asSource(SerializeOpts opts) throws CoreException {

		Source s = new StreamSource(asInputStream(opts));
		s.setSystemId(getSystemId());


		/*
		 * Dont implement XInclude globally yet - problems in Saxon9
		 * See xinclude instead
		 *
		if( opts.isXinclude() ){
			AugmentedSource as  = AugmentedSource.makeAugmentedSource(s);
			as.setXIncludeAware(true);
			s = as;
		}
		 */


		return s;
	}

	@Override
	public synchronized InputSource asInputSource(SerializeOpts opts) throws CoreException {

		InputSource s = new InputSource(asInputStream(opts));
		s.setSystemId(getSystemId());

		return s;
	}



	@Override
	public synchronized XdmNode asXdmNode(SerializeOpts opts) throws CoreException  {

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

	@Override
	public void copyTo(OutputStream out, SerializeOpts opts) throws CoreException, IOException {

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

	@Override
	public  XdmItem asXdmItem(SerializeOpts serializeOpts) throws CoreException
	{
		return asXdmNode(serializeOpts);
	}

	@Override
	public JsonNode asJson(SerializeOpts serializeOpts) throws IOException, CoreException {
		return JSONUtils.readJsonNode( asInputStream(serializeOpts));
	}

	@Override
	public boolean isFile() {
		// TODO Auto-generated method stub
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
