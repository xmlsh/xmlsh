/*
 * $Id: StAXEventContentHandler.java,v 1.5 2004/07/05 23:15:11 cniles Exp $
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.*;
import javax.xml.stream.util.XMLEventConsumer;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * SAX ContentHandler that writes events to a StAX {@link XMLEventConsumer}.
 * 
 * @author Christian Niles
 * @version $Revision: 1.5 $
 */
public class StAXEventContentHandler extends StAXContentHandler {

    /** The consumer to which events will be written. */
    private XMLEventConsumer consumer;

    /** The factory used to construct events. */
    private XMLEventFactory eventFactory;

    /**
     * A stack of {@link List}s, each containing {@link Namespace}events
     * constructed from a {@link StartElement}event. It is necessary to keep
     * these namespaces so we can report them to the {@link EndElement}event.
     */
    private List namespaceStack = new ArrayList();

    /**
     * Constructs a default instance with a default event factory. You must set
     * the {@link XMLEventConsumer}via the
     * {@link #setEventConsumer(XMLEventConsumer)}method.
     */
    public StAXEventContentHandler() {

        eventFactory = XMLEventFactory.newInstance();

    }

    /**
     * Constructs an instance that writes events to the provided
     * XMLEventConsumer. Events will be constructed from a default
     * XMLEventFactory instance.
     * 
     * @param consumer The {@link XMLEventConsumer}to which events will be
     *            written.
     */
    public StAXEventContentHandler(XMLEventConsumer consumer) {

        this.consumer = consumer;
        eventFactory = XMLEventFactory.newInstance();

    }

    /**
     * Constructs an instance that writes events constructed with the provided
     * XMLEventFactory to the provided XMLEventConsumer
     * 
     * @param consumer The {@link XMLEventConsumer} to which events will be
     *            written.
     * @param factory The {@link XMLEventFactory} used to construct events. If
     *            <code>null</code>, a default instance will be constructed.
     */
    public StAXEventContentHandler(XMLEventConsumer consumer,
            XMLEventFactory factory) {

        this.consumer = consumer;
        if (factory != null) {

            this.eventFactory = factory;

        } else {

            eventFactory = XMLEventFactory.newInstance();

        }

    }

    /**
     * Returns a reference to the {@link XMLEventConsumer} to which events will
     * be written.
     * 
     * @return The {@link XMLEventConsumer} to which events will be written.
     */
    public XMLEventConsumer getEventConsumer() {

        return consumer;

    }

    /**
     * Sets the {@link XMLEventConsumer} to which events are written.
     * 
     * @param consumer The {@link XMLEventConsumer} to which events will be
     *            written.
     */
    public void setEventConsumer(XMLEventConsumer consumer) {

        this.consumer = consumer;

    }

    /**
     * Returns a reference to the {@link XMLEventFactory} used to construct
     * events.
     * 
     * @return The {@link XMLEventFactory} used to construct events.
     */
    public XMLEventFactory getEventFactory() {

        return eventFactory;

    }

    /**
     * Sets the {@link XMLEventFactory} used to create events.
     * 
     * @param factory The {@link XMLEventFactory} used to create events.
     */
    public void setEventFactory(XMLEventFactory factory) {

        this.eventFactory = factory;

    }

    public void startDocument() throws SAXException {

        super.startDocument();

        // clear the namespaces in case we ended in error before.
        namespaceStack.clear();

        eventFactory.setLocation(getCurrentLocation());
        try {

            consumer.add(eventFactory.createStartDocument());

        } catch (XMLStreamException e) {

            throw new SAXException(e);

        }

    }

    public void endDocument() throws SAXException {

        eventFactory.setLocation(getCurrentLocation());

        try {

            consumer.add(eventFactory.createEndDocument());

        } catch (XMLStreamException e) {

            throw new SAXException(e);

        }

        super.endDocument();

        // clear the namespaces
        namespaceStack.clear();

    }

    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {

        // set document location
        eventFactory.setLocation(getCurrentLocation());

        // create attribute and namespace events
        Collection[] events = {null, null};
        createStartEvents(attributes, events);

        // save a reference to the namespace collection so we can use them
        // again
        // in the end element
        namespaceStack.add(events[0]);

        try {

            String[] qname = {null, null};
            parseQName(qName, qname);

            consumer.add(eventFactory.createStartElement(qname[0], uri,
                    qname[1], events[1].iterator(), events[0].iterator()));

        } catch (XMLStreamException e) {

            throw new SAXException(e);

        } finally {

            super.startElement(uri, localName, qName, attributes);

        }

    }

    public void endElement(String uri, String localName, String qName)
            throws SAXException {

        super.endElement(uri, localName, qName);

        eventFactory.setLocation(getCurrentLocation());

        // parse name
        String[] qname = {null, null};
        parseQName(qName, qname);

        // get namespaces
        Collection nsList = (Collection) namespaceStack.remove(namespaceStack.size() - 1);
        Iterator nsIter = nsList.iterator();

        try {

            consumer.add(eventFactory.createEndElement(qname[0], uri, qname[1],
                    nsIter));

        } catch (XMLStreamException e) {

            throw new SAXException(e);

        }

    }

    public void comment(char[] ch, int start, int length) throws SAXException {

        super.comment(ch, start, length);

        eventFactory.setLocation(getCurrentLocation());
        try {

            consumer.add(eventFactory.createComment(new String(ch, start,
                    length)));

        } catch (XMLStreamException e) {

            throw new SAXException(e);

        }

    }

    public void characters(char[] ch, int start, int length)
            throws SAXException {

        super.characters(ch, start, length);

        try {

            if (!isCDATA) {

                eventFactory.setLocation(getCurrentLocation());
                consumer.add(eventFactory.createCharacters(new String(ch,
                        start, length)));

            }

        } catch (XMLStreamException e) {

            throw new SAXException(e);

        }

    }

    public void ignorableWhitespace(char[] ch, int start, int length)
            throws SAXException {

        super.ignorableWhitespace(ch, start, length);
        characters(ch, start, length);

    }

    public void processingInstruction(String target, String data)
            throws SAXException {

        super.processingInstruction(target, data);
        try {

            consumer.add(eventFactory.createProcessingInstruction(target, data));

        } catch (XMLStreamException e) {

            throw new SAXException(e);

        }

    }

    public void endCDATA() throws SAXException {

        eventFactory.setLocation(getCurrentLocation());
        try {

            consumer.add(eventFactory.createCData(CDATABuffer.toString()));

        } catch (XMLStreamException e) {

            throw new SAXException(e);

        }

        super.endCDATA();

    }

    /**
     * Creates the {@link Namespace}and {@link Attribute}events associated
     * with a {@link StartElement}.
     * 
     * @param attributes The SAX attributes object.
     * @param events An array used to return the two collections of
     *            {@link Namespace}and {@link Attribute}events. The
     *            namespaces will be placed at <code>events[0]</code> and the
     *            attributes as <code>events[1]</code>.
     */
    protected void createStartEvents(Attributes attributes, Collection[] events) {

        Map nsMap = null;
        List attrs = null;

        // create namespaces
        if (namespaces != null) {

            Iterator prefixes = namespaces.getDeclaredPrefixes();
            while (prefixes.hasNext()) {

                String prefix = (String) prefixes.next();
                String uri = namespaces.getNamespaceURI(prefix);

                Namespace ns = createNamespace(prefix, uri);
                if (nsMap == null) {

                    nsMap = new HashMap();

                }
                nsMap.put(prefix, ns);

            }

        }

        // create attributes
        String[] qname = {null, null};
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

                if (!nsMap.containsKey(attrPrefix)) {

                    Namespace ns = createNamespace(attrPrefix, attrValue);
                    if (nsMap == null) {

                        nsMap = new HashMap();

                    }
                    nsMap.put(attrPrefix, ns);

                }

            } else {

                Attribute attribute;
                if (attrPrefix.length() > 0) {

                    attribute = eventFactory.createAttribute(attrPrefix,
                            attrURI, attrLocal, attrValue);

                } else {

                    attribute = eventFactory.createAttribute(attrLocal,
                            attrValue);

                }

                if (attrs == null) {

                    attrs = new ArrayList();

                }
                attrs.add(attribute);

            }

        }

        events[0] = (nsMap == null ? Collections.EMPTY_LIST : nsMap.values());
        events[1] = (attrs == null ? Collections.EMPTY_LIST : attrs);

    }

    protected Namespace createNamespace(String prefix, String uri) {

        if (prefix == null || prefix.length() == 0) {

            return eventFactory.createNamespace(uri);

        } else {

            return eventFactory.createNamespace(prefix, uri);

        }

    }

}