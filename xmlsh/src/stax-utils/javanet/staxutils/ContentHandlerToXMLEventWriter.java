/* $Id: ContentHandlerToXMLEventWriter.java,v 1.4 2005/10/25 18:36:24 ryan_shoemaker Exp $
 *
 * Copyright (c) 2004, Sun Microsystems, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *     * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *
 *     * Redistributions in binary form must reproduce the above
 *      copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 *
 *     * Neither the name of Sun Microsystems, Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package javanet.staxutils;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.stream.Location;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import java.util.HashMap;
import java.util.Stack;


/**
 * This is a simple utility class that adapts SAX events into StAX
 * {@link javax.xml.stream.XMLEventWriter} events, bridging between
 * the two parser technologies.
 * 
 * This ContentHandler does not own the XMLEventWriter.  Therefore, it will
 * not close or flush the writer at any point.
 * 
 * @author Ryan.Shoemaker@Sun.COM
 * @version 1.0
 */
public class ContentHandlerToXMLEventWriter extends DefaultHandler {

    // SAX events will be sent to this XMLEventWriter
    private final XMLEventWriter staxWriter;

    // factory for StAX events
    private final XMLEventFactory staxEventFactory;

    // SAX locator
    private Locator locator = null;

    // StAX location
    private Location location = null;

    // storage for prefix bindings
    private final Stack prefixBindings;

    // name to entity decl map
    // map<String:EntityDeclaration>
    private final HashMap entityMap;

    public ContentHandlerToXMLEventWriter(XMLEventWriter staxCore) {
        this.staxWriter = staxCore;
        staxEventFactory = XMLEventFactory.newInstance();
        
        prefixBindings = new Stack(); // default of 10 seems reasonable

        entityMap = new HashMap();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#endDocument()
     */
    public void endDocument() throws SAXException {
        try {
            staxWriter.add(staxEventFactory.createEndDocument());
            staxWriter.flush();
        } catch (XMLStreamException e) {
            throw new SAXException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#startDocument()
     */
    public void startDocument() throws SAXException {
        try {
            staxWriter.add(staxEventFactory.createStartDocument());
        } catch (XMLStreamException e) {
            throw new SAXException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    public void characters(char[] ch, int start, int length)
        throws SAXException {

        try {
            // TODO: is there a way to reuse an event?
            Characters event = staxEventFactory.createCharacters(
	        new String(ch,start,length));
            staxWriter.add(event);
        } catch (XMLStreamException e) {
            throw new SAXException(e);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#ignorableWhitespace(char[], int, int)
     */
    public void ignorableWhitespace(char[] ch, int start, int length)
        throws SAXException {
        
        characters(ch,start,length);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#endPrefixMapping(java.lang.String)
     */
    public void endPrefixMapping(String prefix) throws SAXException {
        // TODO: no-op?

        // I think we can ignore these SAX events because StAX
        // automatically scopes the prefix bindings.
    }

    public void unparsedEntityDecl(String name, String publicId, String systemId, String notationName) throws SAXException {
        // store unparsed entity decls so we can report them properly in skippedEntity
        entityMap.put(name, new EntityDeclarationImpl(location, name, publicId, systemId, notationName, null));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#skippedEntity(java.lang.String)
     */
    public void skippedEntity(String name) throws SAXException {
        try {
            // if the entity isn't knows, then pass null through
            // for the EntityDeclaration
            staxWriter.add(staxEventFactory.createEntityReference(name,
                    (EntityDeclarationImpl)entityMap.get(name)));
        } catch (XMLStreamException e) {
            throw new SAXException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#setDocumentLocator(org.xml.sax.Locator)
     */
    public void setDocumentLocator(final Locator locator) {
        this.locator = locator;
        staxEventFactory.setLocation( new Location() {
            public int getLineNumber() { return locator.getLineNumber(); }
            public int getColumnNumber() { return locator.getColumnNumber(); }
            public int getCharacterOffset() { return -1; }
            public String getPublicId() { return locator.getPublicId(); }
            public String getSystemId() { return locator.getSystemId(); }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#processingInstruction(java.lang.String,
     *      java.lang.String)
     */
    public void processingInstruction(String target, String data)
        throws SAXException {

        try {
            staxWriter.add(
                staxEventFactory.createProcessingInstruction(target, data));
        } catch (XMLStreamException e) {
            throw new SAXException(e);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#startPrefixMapping(java.lang.String,
     *      java.lang.String)
     */
    public void startPrefixMapping(String prefix, String uri)
        throws SAXException {

        if (prefix.equals("xml")) {
            return;
        }

        // defend against parsers that pass null in for "xmlns" prefix
        if (prefix == null) {
            prefix = "";
        }

        prefixBindings.add(prefix);
        prefixBindings.add(uri);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#endElement(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    public void endElement(String namespaceURI, String localName, String qName)
        throws SAXException {

        try {
            // TODO: is this all we have to do?
            staxWriter
                .add(
                    staxEventFactory.createEndElement(
                        getPrefix(qName),
                        namespaceURI,
                        localName));
        } catch (XMLStreamException e) {
            throw new SAXException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#startElement(java.lang.String,
     *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    public void startElement(
        String namespaceURI,
        String localName,
        String qName,
        Attributes atts)
        throws SAXException {

        try {
            staxWriter.add(
                staxEventFactory.createStartElement(
                    getPrefix(qName),
                    namespaceURI,
                    localName));

            String uri, prefix;
            while (prefixBindings.size() != 0) {
                uri = (String)prefixBindings.pop();
                prefix = (String)prefixBindings.pop();
                if (prefix.length() == 0) {
                    staxWriter.setDefaultNamespace(uri);
                } else {
                    staxWriter.setPrefix(prefix, uri);
                }

                // this method handles "", null, and "xmlns" prefixes properly
                if( prefix == null || "".equals(prefix) || "xmlns".equals(prefix) ) {
                    staxWriter.add(staxEventFactory.createNamespace(uri));
                } else {
                    staxWriter.add(staxEventFactory.createNamespace(prefix, uri));
                }
            }

            writeAttributes(atts);
        } catch (XMLStreamException e) {
            throw new SAXException(e);
        }

    }

    /**
     * Generate a StAX writeAttribute event for each attribute
     * 
     * @param atts
     *                attributes from the SAX event
     */
    private void writeAttributes(Attributes atts) throws XMLStreamException {
        for (int i = 0; i < atts.getLength(); i++) {
            final String prefix = getPrefix(atts.getQName(i));
            if(!prefix.equals("xmlns")) { // defend againts broken transformers that report xmlns decls as attrs
                staxWriter.add(
                    staxEventFactory.createAttribute(
                        prefix,
                        atts.getURI(i),
                        atts.getLocalName(i),
                        atts.getValue(i)));
            }
        }
    }

    /**
     * Pull the prefix off of the specified QName.
     * 
     * @param qName
     *                the QName
     * @return the prefix or the empty string if it doesn't exist.
     */
    private String getPrefix(String qName) {
        int idx = qName.indexOf(':');
        if (idx == -1) {
            return "";
        } else {
            return qName.substring(0, idx);
        }
    }

}
