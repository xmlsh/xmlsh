/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.util;

import java.util.Iterator;

import javanet.staxutils.BaseXMLEventWriter;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.ProcessingInstruction;
import javax.xml.stream.events.StartDocument;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class XMLEventWriterToContentHandler extends BaseXMLEventWriter {
	private ContentHandler mHandler;

	public XMLEventWriterToContentHandler(ContentHandler handler) {
		mHandler = handler;
	}

	public XMLEventWriterToContentHandler(ContentHandler handler, XMLEventFactory eventFactory,
			NamespaceContext nsCtx) {
		super(eventFactory, nsCtx);
		mHandler = handler;
	}

	@Override
	protected void sendEvent(XMLEvent event) throws XMLStreamException {
		try {

			switch (event.getEventType()) {
			case XMLEvent.START_ELEMENT: {
				StartElement start = event.asStartElement();
				
				for( Iterator iter = start.getNamespaces() ; iter.hasNext() ; ){
					Namespace ns = (Namespace) iter.next();
					
			       	 
	            	 mHandler.startPrefixMapping(ns.getPrefix(), ns.getNamespaceURI());
					
				}

				AttributesImpl attrs = new AttributesImpl();
				for (Iterator iter = start.getAttributes(); iter.hasNext();) {
					Attribute a = (Attribute) iter.next();
					QName aname = a.getName();

					attrs.addAttribute(aname.getNamespaceURI(), aname.getLocalPart(),
							getRawname(aname), "CDATA", a.getValue());

				}
				QName name = start.getName();

				mHandler.startElement(name.getNamespaceURI(), name.getLocalPart(),
						getRawname(name), attrs);

			}
				break;

			case XMLEvent.END_ELEMENT: {
				QName name = event.asEndElement().getName();

				mHandler.endElement(name.getNamespaceURI(), name.getLocalPart(), getRawname(name));

			}
				break;

			case XMLEvent.CHARACTERS: {
				String data = event.asCharacters().getData();

				mHandler.characters(data.toCharArray(), 0, data.length());

			}
				break;
			case XMLEvent.ATTRIBUTE: {
				// SNH
				

			}
				break;

			case XMLEvent.NAMESPACE: {
				// SNH

			}
				break;
			case XMLEvent.PROCESSING_INSTRUCTION: {
				ProcessingInstruction pi = (ProcessingInstruction) event ;
				mHandler.processingInstruction(pi.getTarget(), pi.getData());
				
				
			}
				break;
			case XMLEvent.COMMENT: {
				//

			}
				break;
			case XMLEvent.START_DOCUMENT: {
				StartDocument start = (StartDocument)event;
				mHandler.startDocument();
				

			}
				break;
			case XMLEvent.END_DOCUMENT: {
				mHandler.endDocument();

			}
				break;
			case XMLEvent.DTD: {
				

			}
				break;
			}

		} catch (SAXException e) {
			throw new XMLStreamException(e);
		}

	}

	private String getRawname(QName name) {
		String prefix = name.getPrefix();
		String rawname;
		if (prefix == null || prefix.length() == 0) {
			rawname = name.getLocalPart();
		} else {
			rawname = prefix + ':' + name.getLocalPart();
		}
		return rawname;
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
