/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.util;

import javanet.staxutils.helpers.ElementContext;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/*
 * A XMLStreamWriter which writes to a SAX ContentHandler
 * DAL Note: This code borrows heavily from stax-utils class StAXStremWriter (author Christian Niles)
 * 
 */
public class XMLStreamWriterToContentHandler implements XMLStreamWriter {
	private ContentHandler mHandler;


	/** The root namespace context. */
	private NamespaceContext rootContext;

	/**
	 * The current {@link ElementContext}. used to keep track of opened
	 * elements.
	 */
	private ElementContext elementContext;

	public XMLStreamWriterToContentHandler(ContentHandler handler) {
		mHandler = handler;
	}

	public void close() throws XMLStreamException {
		;

	}

	public void flush() throws XMLStreamException {
	      closeElementContext();

	}

	public NamespaceContext getNamespaceContext() {

		return elementContext;

	}

	public String getPrefix(String uri) throws XMLStreamException {

		return getNamespaceContext().getPrefix(uri);

	}

	public Object getProperty(String name) throws IllegalArgumentException {
		// TODO provide access to properties?
		throw new IllegalArgumentException(name + " property not supported");

	}

	public void setDefaultNamespace(String uri) throws XMLStreamException {
		elementContext.putNamespace("", uri);

	}

	public void setNamespaceContext(NamespaceContext context) throws XMLStreamException {
		if (this.rootContext == null && elementContext == null) {

			this.rootContext = context;

		} else {

			throw new IllegalStateException(
					"NamespaceContext has already been set or document is already in progress");

		}

	}

	public void setPrefix(String prefix, String uri) throws XMLStreamException {
		elementContext.putNamespace(prefix, uri);

	}

	public synchronized void writeAttribute(QName name, String value) throws XMLStreamException {

		if (elementContext == null || elementContext.isReadOnly()) {

			throw new XMLStreamException(getCurrentPath()
					+ " : attributes must be written directly following a start element.");

		}

		elementContext.putAttribute(name, value);

	}

	public void writeAttribute(String prefix, String namespaceURI, String localName, String value)
			throws XMLStreamException {

		if (prefix == null) {

			throw new IllegalArgumentException("attribute prefix may not be null @ ["
					+ getCurrentPath() + "]");

		} else if (localName == null) {

			throw new IllegalArgumentException("attribute localName may not be null @ ["
					+ getCurrentPath() + "]");

		} else if (namespaceURI == null) {

			throw new IllegalArgumentException("attribute namespaceURI may not be null @ ["
					+ getCurrentPath() + "]");

		}

		writeAttribute(new QName(namespaceURI, localName, prefix), value);

	}

	public void writeAttribute(String namespaceURI, String localName, String value)
			throws XMLStreamException {

		writeAttribute("", namespaceURI, localName, value);

	}

	public void writeAttribute(String localName, String value) throws XMLStreamException {

		writeAttribute("", "", localName, value);

	}

	public void writeCharacters(String text) throws XMLStreamException {
	       // flush any cached start element content
        closeElementContext();

		try {
			mHandler.characters(text.toCharArray(), 0, text.length());
		} catch (SAXException e) {
			throw new XMLStreamException(e);
		}

	}

	public void writeCharacters(char[] text, int start, int len) throws XMLStreamException {
	       // flush any cached start element content
        closeElementContext();

		try {
			mHandler.characters(text, start , len );
		} catch (SAXException e) {
			throw new XMLStreamException(e);
		}	

	}

	public void writeComment(String data) throws XMLStreamException {
		// Ignore comments

	}

	public void writeDTD(String dtd) throws XMLStreamException {
		// ignore DTD

	}

	public void writeDefaultNamespace(String namespaceURI) throws XMLStreamException {
	       writeNamespace("", namespaceURI);

	}

	public void writeEmptyElement(String localName) throws XMLStreamException {

		writeEmptyElement("",localName,"");

	}

	public void writeEmptyElement(String namespaceURI, String localName) throws XMLStreamException {
		writeEmptyElement( "" , localName , namespaceURI );

	}

	public void writeEmptyElement(String prefix, String localName, String uri)
			throws XMLStreamException {
		
        writeStartElement( prefix , localName , uri , true );
	}

	public void writeEndDocument() throws XMLStreamException {
	     // flush any cached start element content
        closeElementContext();

		try {
			mHandler.endDocument();
		} catch (SAXException e) {
			throw new XMLStreamException(e);
		}

	}

	public void writeEndElement() throws XMLStreamException {
	      // flush any cached start element content
        closeElementContext();

        if (elementContext != null) {

            QName name = elementContext.getName();
            String rawname = getRawname(name);
            
            try {
				mHandler.endElement( name.getNamespaceURI() , name.getLocalPart() , rawname);
			} catch (SAXException e) {
				throw new XMLStreamException(e);
			}

            // pop the context
            elementContext = elementContext.getParentContext();

        } else {

            throw new XMLStreamException("Unmatched END_ELEMENT");

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

	public void writeEntityRef(String name) throws XMLStreamException {
	      // flush any cached start element content
        closeElementContext();
        // ???
	}

	public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException {

        if (prefix == null) {

            throw new IllegalArgumentException(
                    "Namespace prefix may not be null @ [" + getCurrentPath()
                            + "]");

        } else if (namespaceURI == null) {

            throw new IllegalArgumentException(
                    "Namespace URI may not be null @ [" + getCurrentPath()
                            + "]");

        }

        if (elementContext != null && !elementContext.isReadOnly()) {

            elementContext.putNamespace(prefix, namespaceURI);

        } else {

            throw new XMLStreamException(
                    getCurrentPath()
                            + ": Namespaces must be written directly following a start tag");
        }

	}

	public void writeProcessingInstruction(String target) throws XMLStreamException {
		writeProcessingInstruction(target,"");

	}

	public void writeProcessingInstruction(String target, String data) throws XMLStreamException {
	      // flush any cached start element content
        closeElementContext();

		
		try {
			mHandler.processingInstruction(target, data);
		} catch (SAXException e) {
			throw new XMLStreamException(e);
			
		}

	}

	public void writeStartDocument() throws XMLStreamException {
		writeStartDocument("","");

	}

	public void writeStartDocument(String version) throws XMLStreamException {
		writeStartDocument("",version);

	}

	public void writeStartDocument(String encoding, String version) throws XMLStreamException {
		try {
			mHandler.startDocument();
		} catch (SAXException e) {
			throw new XMLStreamException(e);
		}

	}

	public void writeStartElement(String localName) throws XMLStreamException {
		writeStartElement("",localName,"",false);

	}

	public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {
		writeStartElement("",localName,namespaceURI,false);

	}

	public void writeStartElement(String prefix, String localName, String namespaceURI)
			throws XMLStreamException {
		writeStartElement(prefix,localName,namespaceURI,false);


	}
	   /**
     * Core start tag output method called by all other <code>writeXXXElement</code>
     * methods.
     * 
     * @param prefix The tag prefix.
     * @param localName The tag local name.
     * @param namespaceURI The namespace URI of the prefix.
     * @param isEmpty Whether the tag is empty.
     * @throws XMLStreamException If an error occurs writing the tag to the stream.
     */
    public synchronized void writeStartElement(String prefix, String localName,
            String namespaceURI, boolean isEmpty) throws XMLStreamException {

        if (prefix == null) {

            throw new IllegalArgumentException("prefix may not be null @ ["
                    + getCurrentPath() + "]");

        } else if (localName == null) {

            throw new IllegalArgumentException("localName may not be null @ ["
                    + getCurrentPath() + "]");

        } else if (namespaceURI == null) {

            throw new IllegalArgumentException(
                    "namespaceURI may not be null @ [" + getCurrentPath() + "]");

        }
        
        // new context is beginning; close the current context if needed
        if (elementContext != null) {

            closeElementContext();

            // test if we just closed an empty root context
            if (elementContext == null) {

                throw new XMLStreamException(
                        "Writing start tag after close of root element");

            }

        }


        // create the new context
        QName name = new QName(namespaceURI, localName, prefix);
        elementContext = new ElementContext(name, elementContext, isEmpty);
        
        
    }


	/**
	 * 
	 * Returns the current position of the writer
	 * 
	 * @return The current position of the writer.
	 */
	public synchronized String getCurrentPath() {

		if (elementContext == null) {

			return "/";

		} else {

			return elementContext.getPath();

		}

	}

	public void writeCData(String data) throws XMLStreamException {
		
	       // flush any cached start element content
        closeElementContext();
        try {

			mHandler.characters(data.toCharArray(), 0, data.length());
		} catch (SAXException e) {
			throw new XMLStreamException(e);
		}
		
	}
	
	  /**
     * Closes the current {@link ElementContext}, writing any cached content and 
     * making it read-only. If the current context is empty, it will be popped and
     * replaced with its parent context. If no context is open, this method has no
     * effects.
     * 
     * @throws XMLStreamException If an error occurs flushing any element content.
     */
    protected void closeElementContext() throws XMLStreamException {

        if (elementContext != null && !elementContext.isReadOnly()) {

            elementContext.setReadOnly();

            // it hasn't been closed yet, so write it
            try {

           	 for (int i = 0, s = elementContext.namespaceCount(); i < s; i++) {

                 String prefix = elementContext.getNamespacePrefix(i);
                 String uri = elementContext.getNamespaceURI(i);

            	 
            	 mHandler.startPrefixMapping(prefix, uri);
            	 
           	 }
             
            	 
            	AttributesImpl attrs = new AttributesImpl();
            	 for (int i = 0, s = elementContext.attributeCount(); i < s; i++) {

                     QName name = elementContext.getAttributeName(i);
                     String value = elementContext.getAttribute(i);

                     
                     
                     attrs.addAttribute(name.getNamespaceURI(), name.getLocalPart(), getRawname(name), "CDATA", value);
                  
                 }
                 
            	 QName name = elementContext.getName();

                 
            	 mHandler.startElement(name.getNamespaceURI(), name.getLocalPart(), getRawname(name), attrs);
	 

            } catch (Exception e) {

                throw new XMLStreamException(getCurrentPath()
                        + ": error writing start tag to stream", e);

            }

        }

    }
	
}

//
//
// Copyright (C) 2008,2009,2010 , David A. Lee.
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
