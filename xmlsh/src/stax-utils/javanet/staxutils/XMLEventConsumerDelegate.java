/*
 * $Id: XMLEventConsumerDelegate.java,v 1.4 2004/08/19 15:58:17 cniles Exp $
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

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.*;
import javax.xml.stream.util.XMLEventConsumer;

/**
 * Writes all events to a wrapped {@link XMLEventConsumer}, and provides
 * convenience methods for creating events written to the internal consumer.
 *
 * @author Christian Niles
 * @version $Revision: 1.4 $
 */
public class XMLEventConsumerDelegate implements XMLEventConsumer {

    /**
     * The consumer instance to which events are written.
     */
    private XMLEventConsumer consumer;

    /**
     * The factory used to create events.
     */
    private XMLEventFactory factory;

    public XMLEventConsumerDelegate(XMLEventConsumer consumer) {

        this.consumer = consumer;
        this.factory = XMLEventFactory.newInstance();

    }

    public XMLEventConsumerDelegate(XMLEventConsumer consumer,
            XMLEventFactory factory) {

        this.consumer = consumer;
        this.factory = (factory == null
                ? XMLEventFactory.newInstance()
                : factory);

    }

    /**
     * Returns a reference to the underlying {@link XMLEventConsumer} to which
     * events are added.
     * 
     * @return The underlying {@link XMLEventConsumer} to which events are
     * 		added.
     */
    public XMLEventConsumer getConsumer() {

        return consumer;

    }

    /**
     * Sets the underlying {@link XMLEventConsumer} to which events are added.
     * 
     * @param consumer The new {@link XMLEventConsumer}.
     */
    public void setConsumer(XMLEventConsumer consumer) {

        this.consumer = consumer;

    }

    /**
     * Returns a reference to the {@link XMLEventFactory} used to construct
     * events.
     * 
     * @return The {@link XMLEventFactory} used to construct events.
     */
    public XMLEventFactory getEventFactory() {

        return factory;

    }

    /**
     * Sets the {@link XMLEventFactory} used to construct events.
     *
     * @param factory The new {@link XMLEventFactory}.
     */
    public void setEventFactory(XMLEventFactory factory) {

        this.factory = factory;

    }

    public void add(XMLEvent event) throws XMLStreamException {

        consumer.add(event);

    }

    /**
     * Creates and adds a {@link DTD} event.
     * 
     * @param dtd The DTD content, as per
     * 		{@link XMLEventFactory#createDTD(String)}.
     * @throws XMLStreamException If an error occurs adding the event.
     */
    public void addDTD(String dtd) throws XMLStreamException {

        add(factory.createDTD(dtd));

    }

    /**
     * Creates and adds a CDATA {@link Characters} event.
     * 
     * @param content The CDATA content, as per
     * 		{@link XMLEventFactory#createCData(String)}.
     * @throws XMLStreamException If an error occurs adding the event.
     */
    public void addCData(String content) throws XMLStreamException {

        add(factory.createCData(content));

    }

    /**
     * Creates and adds a {@link Characters} event.
     * 
     * @param content The text content, as per
     * 		{@link XMLEventFactory#createCharacters(String)}.
     * @throws XMLStreamException If an error occurs adding the event.
     */
    public void addText(String content) throws XMLStreamException {

        add(factory.createCharacters(content));

    }

    /**
     * Creates and adds an ignorable space {@link Characters} event.
     * 
     * @param content The ignorable whitespace, as per
     * 		{@link XMLEventFactory#createIgnorableSpace(String)}.
     * @throws XMLStreamException If an error occurs adding the event.
     */
    public void addIgnorableSpace(String content) throws XMLStreamException {

        add(factory.createIgnorableSpace(content));

    }

    /**
     * Creates and adds a whitespace {@link Characters} event.
     * 
     * @param content The whitespace, as per
     * 		{@link XMLEventFactory#createIgnorableSpace(String)}.
     * @throws XMLStreamException If an error occurs adding the event.
     */
    public void addSpace(String content) throws XMLStreamException {

        add(factory.createSpace(content));

    }

    /**
     * Creates and adds a {@link Comment} event.
     * 
     * @param comment The comment text, as per
     * 		{@link XMLEventFactory#createComment(String)}.
     * @throws XMLStreamException If an error occurs adding the event.
     */
    public void addComment(String comment) throws XMLStreamException {

        add(factory.createComment(comment));

    }

    /**
     * Creates and adds a {@link StartDocument} event.
     * 
     * @see XMLEventFactory#createStartDocument()
     * @throws XMLStreamException If an error occurs adding the event.
     */
    public void addStartDocument() throws XMLStreamException {

        add(factory.createStartDocument());

    }

    /**
     * Creates and adds a {@link StartDocument} event.
     * 
     * @param encoding The encoding to specify in the xml declaration.
     * @see XMLEventFactory#createStartDocument(String)
     * @throws XMLStreamException If an error occurs adding the event.
     */
    public void addStartDocument(String encoding) throws XMLStreamException {

        add(factory.createStartDocument(encoding));

    }

    /**
     * Creates and adds a {@link StartDocument} event.
     * 
     * @param encoding The encoding to include in the xml declaration.
     * @param version The XML version to include in the xml declaration.
     * @see XMLEventFactory#createStartDocument(String, String)
     * @throws XMLStreamException If an error occurs adding the event.
     */
    public void addStartDocument(String encoding, String version)
            throws XMLStreamException {

        add(factory.createStartDocument(encoding, version));

    }

    /**
     * Creates and adds a {@link StartDocument} event.
     * 
     * @param encoding The encoding to include in the xml declaration.
     * @param version The XML version to include in the xml declaration.
     * @param standalone The standalone value to include in the xml declaration.
     * @see XMLEventFactory#createStartDocument(String, String, boolean)
     * @throws XMLStreamException If an error occurs adding the event.
     */
    public void addStartDocument(String encoding, String version,
            boolean standalone) throws XMLStreamException {

        add(factory.createStartDocument(encoding, version, standalone));

    }

    /**
     * Creates and adds an {@link EndDocument} event.
     * 
     * @see XMLEventFactory#createEndDocument()
     * @throws XMLStreamException If an error occurs adding the event.
     */
    public void addEndDocument() throws XMLStreamException {

        add(factory.createEndDocument());

    }

    /**
     * Creates and adds a {@link StartElement} event.
     * 
     * @param localName The local name of the element.
     * @param context The element's {@link NamespaceContext}, or <code>null</code>.
     * @throws XMLStreamException If an error occurs adding the event.
     */
    public void addStartElement(String localName, NamespaceContext context)
            throws XMLStreamException {

        addStartElement(localName, null, null, context);

    }

    /**
     * Creates and adds a {@link StartElement} event.
     * 
     * @param localName The local name of the element.
     * @param attributes An {@link Iterator} over the element's attributes.
     * @param namespaces An {@link Iterator} over the element's namespaces.
     * @param context The element's {@link NamespaceContext}, or <code>null</code>.
     * @throws XMLStreamException If an error occurs adding the event.
     */
    public void addStartElement(String localName, Iterator attributes,
            Iterator namespaces, NamespaceContext context)
            throws XMLStreamException {

        add(factory.createStartElement("", "", localName, attributes,
                namespaces, context));

    }

    /**
     * Creates and adds a {@link StartElement} event.
     * 
     * @param ns The element's namespace URI.
     * @param localName The local name of the element.
     * @param context The element's {@link NamespaceContext}, or <code>null</code>.
     * @throws XMLStreamException If an error occurs adding the event.
     */
    public void addStartElement(String ns, String localName,
            NamespaceContext context) throws XMLStreamException {

        addStartElement(ns, localName, null, null, context);

    }

    /**
     * Creates and adds a {@link StartElement} event.
     * 
     * @param ns The element's namespace URI.
     * @param localName The local name of the element.
     * @param attributes An {@link Iterator} over the element's attributes.
     * @param namespaces An {@link Iterator} over the element's namespaces.
     * @param context The element's {@link NamespaceContext}, or <code>null</code>.
     * @throws XMLStreamException If an error occurs adding the event.
     */
    public void addStartElement(String ns, String localName,
            Iterator attributes, Iterator namespaces, NamespaceContext context)
            throws XMLStreamException {

        add(factory.createStartElement("", ns, localName, attributes,
                namespaces, context));

    }

    /**
     * Creates and adds a {@link StartElement} event.
     * 
     * @param name The qualified element name.
     * @param context The element's {@link NamespaceContext}, or <code>null</code>.
     * @throws XMLStreamException If an error occurs adding the event.
     */
    public void addStartElement(QName name, NamespaceContext context)
            throws XMLStreamException {

        addStartElement(name, null, null, context);

    }

    /**
     * Creates and adds a {@link StartElement} event.
     * 
     * @param name The qualified element name.
     * @param attributes An {@link Iterator} over the element's attributes.
     * @param namespaces An {@link Iterator} over the element's namespaces.
     * @param context The element's {@link NamespaceContext}, or <code>null</code>.
     * @throws XMLStreamException If an error occurs adding the event.
     */
    public void addStartElement(QName name, Iterator attributes,
            Iterator namespaces, NamespaceContext context)
            throws XMLStreamException {

        add(factory.createStartElement(name.getPrefix(),
                name.getNamespaceURI(), name.getLocalPart(), attributes,
                namespaces, context));

    }

    /**
     * Creates and adds an {@link EndElement} event.
     * 
     * @param localName The unqualified element name.
     * @throws XMLStreamException If an error occurs adding the event.
     */
    public void addEndElement(String localName) throws XMLStreamException {

        addEndElement(localName, (Iterator) null);

    }

    /**
     * Creates and adds an {@link EndElement} event.
     * 
     * @param localName The unqualified element name.
     * @param namespaces An {@link Iterator} over the element's namespaces that
     * 		are going out of scope.
     * @throws XMLStreamException If an error occurs adding the event.
     */
    public void addEndElement(String localName, Iterator namespaces)
            throws XMLStreamException {

        add(factory.createEndElement(null, null, localName, namespaces));

    }

    /**
     * Creates and adds an {@link EndElement} event.
     * 
     * @param ns The element namespace.
     * @param localName The element name.
     * @throws XMLStreamException If an error occurs adding the event.
     */
    public void addEndElement(String ns, String localName)
            throws XMLStreamException {

        addEndElement(ns, localName, (Iterator) null);

    }

    /**
     * Creates and adds an {@link EndElement} event.
     * 
     * @param ns The element namespace.
     * @param localName The element name.
     * @param namespaces An {@link Iterator} over the element's namespaces that
     * 		are going out of scope.
     * @throws XMLStreamException If an error occurs adding the event.
     */
    public void addEndElement(String ns, String localName, Iterator namespaces)
            throws XMLStreamException {

        add(factory.createEndElement(null, ns, localName, namespaces));

    }

    /**
     * Creates and adds an {@link EndElement} event.
     * 
     * @param name The element name.
     * @see XMLEventFactory#createEndElement(QName, Iterator)
     * @throws XMLStreamException If an error occurs adding the event.
     */
    public void addEndElement(QName name) throws XMLStreamException {

        addEndElement(name, (Iterator) null);

    }

    /**
     * Creates and adds an {@link EndElement} event.
     * 
     * @param name The element name.
     * @param namespaces An {@link Iterator} over the element's namespaces that
     * 		are going out of scope.
     * @see XMLEventFactory#createEndElement(QName, Iterator)
     * @throws XMLStreamException If an error occurs adding the event.
     */
    public void addEndElement(QName name, Iterator namespaces)
            throws XMLStreamException {

        add(factory.createEndElement(name, namespaces));

    }

    /**
     * Adds a simple text element with no attributes or namespace declarations.
     * 
     * @param name The unqualified element name.
     * @param text The text content, which may be <code>null</code>
     * @param context The element's {@link NamespaceContext}, or <code>null</code>.
     * @throws XMLStreamException If an error occurs adding an event.
     */
    public void addTextElement(String name, String text,
            NamespaceContext context) throws XMLStreamException {

        addStartElement(name, context);
        if (text != null) {

            addText(text);

        }
        addEndElement(name);

    }

    /**
     * Adds a simple text element with no attributes or namespace declarations.
     * 
     * @param name The element name.
     * @param text The text content, which may be <code>null</code>
     * @param context The element's {@link NamespaceContext}, or <code>null</code>.
     * @throws XMLStreamException If an error occurs adding an event.
     */
    public void addTextElement(QName name, String text, NamespaceContext context)
            throws XMLStreamException {

        addStartElement(name, context);
        if (text != null) {

            addText(text);

        }
        addEndElement(name);

    }

    /**
     * Adds a boolean text element with no attributes or namespace declarations.
     * 
     * @param name The unqualified element name.
     * @param text The boolean content.
     * @param context The element's {@link NamespaceContext}, or <code>null</code>.
     * @throws XMLStreamException If an error occurs adding an event.
     */
    public void addTextElement(String name, boolean text,
            NamespaceContext context) throws XMLStreamException {

        addTextElement(name, Boolean.toString(text), context);

    }

    /**
     * Adds a boolean text element with no attributes or namespace declarations.
     * 
     * @param name The element name.
     * @param text The boolean content.
     * @param context The element's {@link NamespaceContext}, or <code>null</code>.
     * @throws XMLStreamException If an error occurs adding an event.
     */
    public void addTextElement(QName name, boolean text,
            NamespaceContext context) throws XMLStreamException {

        addTextElement(name, Boolean.toString(text), context);

    }

    /**
     * Adds a text element with no attributes or namespace declarations.
     * 
     * @param name The unqualified element name.
     * @param text The element content.
     * @param context The element's {@link NamespaceContext}, or <code>null</code>.
     * @throws XMLStreamException If an error occurs adding an event.
     */
    public void addTextElement(String name, int text, NamespaceContext context)
            throws XMLStreamException {

        addTextElement(name, Integer.toString(text), context);

    }

    /**
     * Adds a text element with no attributes or namespace declarations.
     * 
     * @param name The element name.
     * @param text The element content.
     * @param context The element's {@link NamespaceContext}, or <code>null</code>.
     * @throws XMLStreamException If an error occurs adding an event.
     */
    public void addTextElement(QName name, int text, NamespaceContext context)
            throws XMLStreamException {

        addTextElement(name, Integer.toString(text), context);

    }

    /**
     * Adds a text element with no attributes or namespace declarations.
     * 
     * @param name The unqualified element name.
     * @param text The element content.
     * @param context The element's {@link NamespaceContext}, or <code>null</code>.
     * @throws XMLStreamException If an error occurs adding an event.
     */
    public void addTextElement(String name, long text, NamespaceContext context)
            throws XMLStreamException {

        addTextElement(name, Long.toString(text), context);

    }

    /**
     * Adds a text element with no attributes or namespace declarations.
     * 
     * @param name The element name.
     * @param text The element content.
     * @param context The element's {@link NamespaceContext}, or <code>null</code>.
     * @throws XMLStreamException If an error occurs adding an event.
     */
    public void addTextElement(QName name, long text, NamespaceContext context)
            throws XMLStreamException {

        addTextElement(name, Long.toString(text), context);

    }

    /**
     * Adds a text element with no attributes or namespace declarations.
     * 
     * @param name The unqualified element name.
     * @param text The element content.
     * @param context The element's {@link NamespaceContext}, or <code>null</code>.
     * @throws XMLStreamException If an error occurs adding an event.
     */
    public void addTextElement(String name, float text, NamespaceContext context)
            throws XMLStreamException {

        addTextElement(name, Float.toString(text), context);

    }

    /**
     * Adds a text element with no attributes or namespace declarations.
     * 
     * @param name The element name.
     * @param text The element content.
     * @param context The element's {@link NamespaceContext}, or <code>null</code>.
     * @throws XMLStreamException If an error occurs adding an event.
     */
    public void addTextElement(QName name, float text, NamespaceContext context)
            throws XMLStreamException {

        addTextElement(name, Float.toString(text), context);

    }

    /**
     * Adds a text element with no attributes or namespace declarations.
     * 
     * @param name The unqualified element name.
     * @param text The element content.
     * @param context The element's {@link NamespaceContext}, or <code>null</code>.
     * @throws XMLStreamException If an error occurs adding an event.
     */
    public void addTextElement(String name, double text,
            NamespaceContext context) throws XMLStreamException {

        addTextElement(name, Double.toString(text), context);

    }

    /**
     * Adds a text element with no attributes or namespace declarations.
     * 
     * @param name The element name.
     * @param text The element content.
     * @param context The element's {@link NamespaceContext}, or <code>null</code>.
     * @throws XMLStreamException If an error occurs adding an event.
     */
    public void addTextElement(QName name, double text, NamespaceContext context)
            throws XMLStreamException {

        addTextElement(name, Double.toString(text), context);

    }

    /**
     * Adds a text element with no attributes or namespace declarations.
     * 
     * @param name The unqualified element name.
     * @param text The element content.
     * @param context The element's {@link NamespaceContext}, or <code>null</code>.
     * @throws XMLStreamException If an error occurs adding an event.
     */
    public void addTextElement(String name, Number text,
            NamespaceContext context) throws XMLStreamException {

        if (text != null) {

            addTextElement(name, text.toString(), context);

        } else {

            addTextElement(name, (String) null, context);

        }

    }

    /**
     * Adds a text element with no attributes or namespace declarations.
     * 
     * @param name The element name.
     * @param text The element content.
     * @param context The element's {@link NamespaceContext}, or <code>null</code>.
     * @throws XMLStreamException If an error occurs adding an event.
     */
    public void addTextElement(QName name, Number text, NamespaceContext context)
            throws XMLStreamException {

        if (text != null) {

            addTextElement(name, text.toString(), context);

        } else {

            addTextElement(name, (String) null, context);

        }

    }

}