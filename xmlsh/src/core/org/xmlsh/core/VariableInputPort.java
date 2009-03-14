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

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xmlsh.sh.shell.SerializeOpts;
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

	public synchronized Document asDocument(SerializeOpts opts)
			throws ParserConfigurationException, SAXException, IOException,
			InvalidArgumentException, SaxonApiException {

		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		domFactory.setNamespaceAware(true); // never forget this!
		javax.xml.parsers.DocumentBuilder builder = domFactory.newDocumentBuilder();
		return builder.parse(asInputStream(opts));

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

}

//
//
// Copyright (C) 2008, David A. Lee.
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
