/*
 * $Id: StAXStreamContentHandler.java,v 1.5 2004/07/05 23:15:11 cniles Exp $
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

import java.util.Iterator;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * SAX ContentHandler that writes events to a StAX {@link XMLStreamWriter}.
 * 
 * @author Christian Niles
 * @version $Revision: 1.5 $
 */
public class StAXStreamContentHandler extends StAXContentHandler {

	/** The StAX stream to which SAX events will be written. */
	private XMLStreamWriter writer;

	public StAXStreamContentHandler() {

	}

	/**
	 * Constructs an instance that writes SAX events to the specified StAX
	 * stream.
	 * 
	 * @param writer The StAX stream to which events will be written.
	 */
	public StAXStreamContentHandler(XMLStreamWriter writer) {

		this.writer = writer;

	}

	/**
	 * Returns a reference to the {@link XMLStreamWriter}to which SAX events
	 * are written.
	 * 
	 * @return The {@link XMLStreamWriter}to which SAX events are written.
	 */
	public XMLStreamWriter getStreamWriter() {

		return writer;

	}

	/**
	 * Sets the {@link XMLStreamWriter}to which SAX events will be written.
	 * 
	 * @param writer The {@link XMLStreamWriter}to which SAX events will be
	 *            written.
	 */
	public void setStreamWriter(XMLStreamWriter writer) {

		this.writer = writer;

	}

	public void startDocument() throws SAXException {

		super.startDocument();
		try {

			writer.writeStartDocument();

		} catch (XMLStreamException e) {

			throw new SAXException(e);

		}

	}

	public void endDocument() throws SAXException {

		try {

			writer.writeEndDocument();

		} catch (XMLStreamException e) {

			throw new SAXException(e);

		}

		super.endDocument();

	}

	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {

		try {

			String[] qname = {null, null};
			parseQName(qName, qname);

			writer.writeStartElement(qname[0], qname[1], uri);

			// copy namespaces
			if (namespaces != null) {

				Iterator prefixes = namespaces.getDeclaredPrefixes();
				while (prefixes.hasNext()) {

					String prefix = (String) prefixes.next();
					String nsURI = namespaces.getNamespaceURI(prefix);

					if (prefix.length() == 0) {

						writer.setDefaultNamespace(nsURI);

					} else {

						writer.setPrefix(prefix, nsURI);

					}

					writer.writeNamespace(prefix, nsURI);

				}

			}

			// write attributes
			for (int i = 0, s = attributes.getLength(); i < s; i++) {

				parseQName(attributes.getQName(i), qname);

				String attrPrefix = qname[0];
				String attrLocal = qname[1];

				String attrQName = attributes.getQName(i);
				String attrValue = attributes.getValue(i);
				String attrURI = attributes.getURI(i);

				if ("xmlns".equals(attrQName) || "xmlns".equals(attrPrefix)) {

					// namespace declaration disguised as an attribute. If the
					// namespace has already been declared, skip it, otherwise
					// write it as an namespace

					String nsURI = namespaces.getNamespaceURI(attrPrefix);
					if (nsURI == null) {

						if (attrPrefix.length() == 0) {

							writer.setDefaultNamespace(attrValue);

						} else {

							writer.setPrefix(attrPrefix, attrValue);

						}

						writer.writeNamespace(attrPrefix, attrValue);

					}

				} else if (attrPrefix.length() > 0) {

					writer.writeAttribute(attrPrefix, attrURI, attrLocal,
							attrValue);

				} else {

					writer.writeAttribute(attrQName, attrValue);

				}

			}

		} catch (XMLStreamException e) {

			throw new SAXException(e);

		} finally {

			super.startElement(uri, localName, qName, attributes);

		}

	}

	public void endElement(String uri, String localName, String qName)
			throws SAXException {

		try {

			writer.writeEndElement();

		} catch (XMLStreamException e) {

			throw new SAXException(e);

		} finally {

			super.endElement(uri, localName, qName);

		}

	}

	public void comment(char[] ch, int start, int length) throws SAXException {

		super.comment(ch, start, length);
		try {

			writer.writeComment(new String(ch, start, length));

		} catch (XMLStreamException e) {

			throw new SAXException(e);

		}

	}

	public void characters(char[] ch, int start, int length)
			throws SAXException {

		super.characters(ch, start, length);
		try {

			if (!isCDATA) {

				writer.writeCharacters(ch, start, length);

			}

		} catch (XMLStreamException e) {

			throw new SAXException(e);

		}

	}

	public void endCDATA() throws SAXException {

		try {

			writer.writeCData(CDATABuffer.toString());

		} catch (XMLStreamException e) {

			throw new SAXException(e);

		}

		super.endCDATA();

	}

	public void ignorableWhitespace(char[] ch, int start, int length)
			throws SAXException {

		super.ignorableWhitespace(ch, start, length);
		try {

			writer.writeCharacters(ch, start, length);

		} catch (XMLStreamException e) {

			throw new SAXException(e);

		}

	}

	public void processingInstruction(String target, String data)
			throws SAXException {

		super.processingInstruction(target, data);
		try {

			writer.writeProcessingInstruction(target, data);

		} catch (XMLStreamException e) {

			throw new SAXException(e);

		}

	}

}
