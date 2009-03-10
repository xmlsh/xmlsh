/*
 * $Id: StAXContentHandler.java,v 1.3 2004/04/27 20:04:42 cniles Exp $
 * 
 * Copyright (c) 2004, Christian Niles, Unit12
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 *		*   Redistributions of source code must retain the above copyright
 *          notice, this list of conditions and the following disclaimer.
 * 
 *	    *	Redistributions in binary form must reproduce the above copyright
 *          notice, this list of conditions and the following disclaimer in the
 *          documentation and/or other materials provided with the distribution.
 * 
 *      *   Neither the name of Christian Niles, Unit12, nor the names of its
 *          contributors may be used to endorse or promote products derived from
 *          this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 */
package javanet.staxutils;

import javax.xml.stream.Location;
import javax.xml.stream.XMLReporter;
import javax.xml.stream.XMLStreamException;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Base class for SAX-to-STAX bridge classes that provides common
 * functionality.
 * 
 * @author Christian Niles
 * @version $Revision: 1.3 $
 */
public abstract class StAXContentHandler extends DefaultHandler
		implements
			LexicalHandler {

	/**
	 * Whether the parser is currently within a CDATA section.
	 */
	protected boolean isCDATA;

	/**
	 * Buffer containing text read within the current CDATA section.
	 */
	protected StringBuffer CDATABuffer;

	/**
	 * Stack used to store declared namespaces.
	 */
	protected SimpleNamespaceContext namespaces;

	/**
	 * The SAX {@link Locator}provided to the handler.
	 */
	protected Locator docLocator;

	/**
	 * The STAX {@link XMLReporter}registered to receive notifications.
	 */
	protected XMLReporter reporter;

	public StAXContentHandler() {

	}

	public StAXContentHandler(XMLReporter reporter) {

		this.reporter = reporter;

	}

	/**
	 * Sets the {@link XMLReporter}to which warning and error messages will be
	 * sent.
	 * 
	 * @param reporter The {@link XMLReporter}to notify of errors.
	 */
	public void setXMLReporter(XMLReporter reporter) {

		this.reporter = reporter;

	}

	public void setDocumentLocator(Locator locator) {

		this.docLocator = locator;

	}

	/**
	 * Calculates the STAX {@link Location}from the SAX {@link Locator}
	 * registered with this handler. If no {@link Locator}was provided, then
	 * this method will return <code>null</code>.
	 */
	public Location getCurrentLocation() {

		if (docLocator != null) {

			return new SAXLocation(docLocator);

		} else {

			return null;

		}

	}

	public void error(SAXParseException e) throws SAXException {

		reportException("ERROR", e);

	}

	public void fatalError(SAXParseException e) throws SAXException {

		reportException("FATAL", e);

	}

	public void warning(SAXParseException e) throws SAXException {

		reportException("WARNING", e);

	}

	public void startDocument() throws SAXException {

		namespaces = new SimpleNamespaceContext();

	}

	public void endDocument() throws SAXException {

		namespaces = null;

	}

	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {

		namespaces = null;

	}

	public void endElement(String uri, String localName, String qName)
			throws SAXException {

		namespaces = null;

	}

	public void startPrefixMapping(String prefix, String uri)
			throws SAXException {

		if (prefix == null) {

			prefix = "";

		} else if (prefix.equals("xml")) {

			return;

		}

		if (namespaces == null) {

			namespaces = new SimpleNamespaceContext();

		}
		namespaces.setPrefix(prefix, uri);

	}

	public void endPrefixMapping(String prefix) throws SAXException {

	}

	public void startCDATA() throws SAXException {

		isCDATA = true;
		if (CDATABuffer == null) {

			CDATABuffer = new StringBuffer();

		} else {

			CDATABuffer.setLength(0);

		}

	}

	public void characters(char[] ch, int start, int length)
			throws SAXException {

		if (isCDATA) {

			CDATABuffer.append(ch, start, length);

		}

	}

	public void endCDATA() throws SAXException {

		isCDATA = false;
		CDATABuffer.setLength(0);

	}

	public void comment(char[] ch, int start, int length) throws SAXException {

	}

	public void endDTD() throws SAXException {

	}

	public void endEntity(String name) throws SAXException {

	}

	public void startDTD(String name, String publicId, String systemId)
			throws SAXException {

	}

	public void startEntity(String name) throws SAXException {

	}

	/**
	 * Used to report a {@link SAXException}to the {@link XMLReporter}
	 * registered with this handler.
	 */
	protected void reportException(String type, SAXException e)
			throws SAXException {

		if (reporter != null) {

			try {

				reporter.report(e.getMessage(), type, e, getCurrentLocation());

			} catch (XMLStreamException e1) {

				throw new SAXException(e1);

			}

		}

	}

	/**
	 * Parses an XML qualified name, and places the resulting prefix and local
	 * name in the provided String array.
	 * 
	 * @param qName The qualified name to parse.
	 * @param results An array where parse results will be placed. The prefix
	 *            will be placed at <code>results[0]</code>, and the local
	 *            part at <code>results[1]</code>
	 */
	public static final void parseQName(String qName, String[] results) {

		String prefix, local;
		int idx = qName.indexOf(':');
		if (idx >= 0) {

			prefix = qName.substring(0, idx);
			local = qName.substring(idx + 1);

		} else {

			prefix = "";
			local = qName;

		}

		results[0] = prefix;
		results[1] = local;

	}

	/**
	 * {@Link Location}implementation used to expose details from a SAX
	 * {@link Locator}.
	 * 
	 * @author christian
	 * @version $Revision: 1.3 $
	 */
	private static final class SAXLocation implements Location {

		private int lineNumber;

		private int columnNumber;

		private String publicId;

		private String systemId;

		private SAXLocation(Locator locator) {

			lineNumber = locator.getLineNumber();
			columnNumber = locator.getColumnNumber();
			publicId = locator.getPublicId();
			systemId = locator.getSystemId();

		}

		public int getLineNumber() {

			return lineNumber;

		}

		public int getColumnNumber() {

			return columnNumber;

		}

		public int getCharacterOffset() {

			return -1;

		}

		public String getPublicId() {

			return publicId;

		}

		public String getSystemId() {

			return systemId;

		}

	}

}
