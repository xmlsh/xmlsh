/*
 * $Id: XMLStreamUtils.java,v 1.8 2004/07/09 17:30:50 cniles Exp $
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.util.XMLEventConsumer;
import javax.xml.transform.Result;
import javax.xml.transform.Source;

/**
 * Static utility methods useful when handling XML Streams.
 * 
 * @author Christian Niles
 * @version $Revision: 1.8 $
 */
public class XMLStreamUtils {

    private static XMLInputFactory inputFactory = XMLInputFactory.newInstance();

    private static XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();

    private static final String[] EVENT_NAMES = new String[16];
    static {

        EVENT_NAMES[0] = ""; // no event has 0 index
        EVENT_NAMES[XMLStreamConstants.ATTRIBUTE] = "ATTRIBUTE";
        EVENT_NAMES[XMLStreamConstants.CDATA] = "CDATA";
        EVENT_NAMES[XMLStreamConstants.CHARACTERS] = "CHARACTERS";
        EVENT_NAMES[XMLStreamConstants.COMMENT] = "COMMENT";
        EVENT_NAMES[XMLStreamConstants.DTD] = "DTD";
        EVENT_NAMES[XMLStreamConstants.END_DOCUMENT] = "END_DOCUMENT";
        EVENT_NAMES[XMLStreamConstants.END_ELEMENT] = "END_ELEMENT";
        EVENT_NAMES[XMLStreamConstants.ENTITY_DECLARATION] = "ENTITY_DECLARATION";
        EVENT_NAMES[XMLStreamConstants.ENTITY_REFERENCE] = "ENTITY_REFERENCE";
        EVENT_NAMES[XMLStreamConstants.NAMESPACE] = "NAMESPACE";
        EVENT_NAMES[XMLStreamConstants.NOTATION_DECLARATION] = "NOTATION_DECLARATION";
        EVENT_NAMES[XMLStreamConstants.PROCESSING_INSTRUCTION] = "PROCESSING_INSTRUCTION";
        EVENT_NAMES[XMLStreamConstants.SPACE] = "SPACE";
        EVENT_NAMES[XMLStreamConstants.START_DOCUMENT] = "START_DOCUMENT";
        EVENT_NAMES[XMLStreamConstants.START_ELEMENT] = "START_ELEMENT";

    }

    /**
     * Returns the name of the specified stream event constant.
     * 
     * @param eventType The event constant, such as
     *     {@link XMLStreamConstants#START_DOCUMENT}.
     * @return The name of the specified event, or <code>"UNKNOWN"</code> if the
     *     constant isn't valid.
     */
    public static final String getEventTypeName(int eventType) {

        if (eventType > 0 || eventType < EVENT_NAMES.length) {

            return EVENT_NAMES[eventType];

        } else {

            return "UNKNOWN";

        }

    }

    /**
     * Returns the value of the attribute with the given non-qualified name.
     * 
     * @param reader The xml stream reader
     * @param name The name of the attribute.
     * @return The value of the unqualified attribute, or <code>null</code>
     *         if the attribute wasn't present.
     */
    public static final String attributeValue(XMLStreamReader reader,
            String name) {

        return reader.getAttributeValue("", name);

    }

    /**
     * Returns the value of the attribute with the given name.
     * 
     * @param reader The xml stream reader
     * @param name The name of the attribute.
     * @return The value of the attribute, or <code>null</code> if the
     *         attribute wasn't present.
     */
    public static final String attributeValue(XMLStreamReader reader, QName name) {

        return reader.getAttributeValue(name.getNamespaceURI(),
                name.getLocalPart());

    }

    /**
     * Skips all events within a single element, including its start and end
     * tags. The provided reader must be positioned directly in front of a
     * <code>StartElement</code> event or it will have no effect. After this
     * method completes, the reader will be positioned before the event
     * following the end tag (the end tag will have been read).
     * 
     * @param reader The event stream to read.
     * @throws XMLStreamException If an error occurs reading events.
     */
    public static final void skipElement(XMLEventReader reader)
            throws XMLStreamException {

        copyElement(reader, null);

    }

    /**
     * Copies an element and all its content from the provided event reader, to
     * the provided event consumer. The event reader must be positioned before a
     * start element event, or this method has no effect.
     * 
     * @param reader The reader from which to read the events.
     * @param consumer The destination for read events, or <code>null</code> to
     * 		ignore all events.
     * @throws XMLStreamException If an error occurs reading or writing the
     *             events.
     */
    public static final void copyElement(XMLEventReader reader,
            XMLEventConsumer consumer) throws XMLStreamException {

        if (!reader.hasNext())
            return;

        XMLEvent event = reader.peek();
        if (!event.isStartElement())
            return;

        int depth = 0;
        do {

            XMLEvent currEvt = reader.nextEvent();
            if (currEvt.isStartElement()) {

                depth++;

            } else if (currEvt.isEndElement()) {

                depth--;

            }

            if (consumer != null) {

                consumer.add(currEvt);

            }

        } while (depth > 0 && reader.hasNext());

    }

    /**
     * Skips all events within a <code>StartElement</code> until the matching
     * <code>EndElement</code> is reached. This method assumes that the reader
     * is positioned after the <code>StartElement</code> event, and when the
     * method completes, the stream will be positioned before the
     * <code>EndElement</code> event, but it will not consume the end tag.
     * 
     * @param reader The event stream to read, positioned after the
     * 		<code>StartElement</code>
     * @throws XMLStreamException If an error occurs reading events.
     */
    public static final void skipElementContent(XMLEventReader reader)
            throws XMLStreamException {

        copyElementContent(reader, null);

    }

    /**
     * Copies all events within a <code>StartElement</code> until the matching
     * <code>EndElement</code> is reached. This method assumes that the reader
     * is positioned after the <code>StartElement</code> event, and when the
     * method completes, the stream will be positioned before the 
     * <code>EndElement</code> event, but it will not consume the end tag.
     * 
     * @param reader The event stream to read, positioned after the
     * 		<code>StartElement</code>
     * @param consumer The destination for events read from teh stream, or
     * 		<code>null</code> to ignore the events completely.
     * @throws XMLStreamException If an error occurs reading events.
     */
    public static final void copyElementContent(XMLEventReader reader,
            XMLEventConsumer consumer) throws XMLStreamException {

        if (!reader.hasNext())
            return;

        for (int depth = 1; true;) {

            // peek and see if we're at the end element
            XMLEvent currEvt = reader.peek();
            if (currEvt.isEndElement()) {

                depth--;
                if (depth == 0) {

                    break;

                }

            } else if (currEvt.isStartElement()) {

                depth++;

            }

            // consume the event
            currEvt = reader.nextEvent();

            if (consumer != null) {

                consumer.add(currEvt);

            }

        }

    }

    /**
     * Skips the complete content of the element at the specified reader's
     * cursor. The reader's current event type must be START_ELEMENT, otherwise
     * this method will have no effect. Upon completion, the reader's cursor
     * will be at the END_ELEMENT event for the skipped element.
     * 
     * @param reader An XML stream reader currently in the START_ELEMENT event.
     */
    public static final void skipElement(XMLStreamReader reader)
            throws XMLStreamException {

        if (reader.isStartElement()) {

            skipElementContent(reader);

        }

    }

    /**
     * Skips an element's complete content. This method assumes that the
     * <code>START_ELEMENT</code> has already be passed, and when it terminates,
     * the stream will be positioned at the <code>END_ELEMENT</code>.
     * 
     * @param reader The stream reader to read.
     * @throws XMLStreamException If an error occurs reading the stream.
     */
    public static final void skipElementContent(XMLStreamReader reader)
            throws XMLStreamException {

        int depth = 0;
        while (depth >= 0) {

            reader.next();
            if (reader.isStartElement()) {

                depth++;

            } else if (reader.isEndElement()) {

                depth--;

            }

        }

    }

    /**
     * Static utility method that throws an exception if the supplied reader's
     * cursor doesn't point to a START_ELEMENT with the given name.
     * 
     * @param reader The reader to test.
     * @param name The name of the element to require.
     * @throws XMLStreamException If the reader state is an element with the
     *             specified name.
     */
    public static final void requireElement(XMLStreamReader reader, QName name)
            throws XMLStreamException {

        reader.require(XMLStreamReader.START_ELEMENT, name.getNamespaceURI(),
                name.getLocalPart());

    }

    /**
     * Copies the content read from the specified source stream to the provided
     * result stream. This method is exactly the same as calling
     * {@link XMLEventWriter#add(XMLEventReader)}, and is provided only for
     * completeness.
     * 
     * @param reader The source stream.
     * @param consumer The destination stream.
     * @throws XMLStreamException If an error occurs copying the stream
     *             contents.
     */
    public static final void copy(XMLEventReader reader,
            XMLEventConsumer consumer) throws XMLStreamException {

        if (consumer instanceof XMLEventWriter) {

            copy(reader, (XMLEventWriter) consumer);

        } else {

            while (reader.hasNext()) {

                consumer.add(reader.nextEvent());

            }

        }

    }

    /**
     * Copies the content read from the specified source stream to the provided
     * result stream. This method is exactly the same as calling
     * {@link XMLEventWriter#add(XMLEventReader)}, and is provided only for
     * completeness.
     * 
     * @param reader The source stream.
     * @param writer The destination stream.
     * @throws XMLStreamException If an error occurs copying the stream
     *             contents.
     */
    public static final void copy(XMLEventReader reader, XMLEventWriter writer)
            throws XMLStreamException {

        writer.add(reader);

    }

    /**
     * Copies the content read from the specified source stream to the provided
     * result stream.
     * 
     * @param reader The source stream.
     * @param writer The destination stream.
     * @throws XMLStreamException If an error occurs copying the stream
     *             contents.
     */
    public static final void copy(XMLStreamReader reader, XMLStreamWriter writer)
            throws XMLStreamException {

        XMLEventReader r = inputFactory.createXMLEventReader(reader);
        XMLEventWriter w = new XMLStreamEventWriter(writer);

        try {

            w.add(r);

        } finally {

            // force any cached events to the underlying writer
            w.flush();

        }

    }

    /**
     * Copies the content read from the specified source stream to the provided
     * result stream.
     * 
     * @param reader The source stream.
     * @param writer The destination stream.
     * @param factory An optional input factory used to create any intermediate
     *            streams.
     * @throws XMLStreamException If an error occurs copying the stream
     *             contents.
     */
    public static final void copy(XMLStreamReader reader,
            XMLStreamWriter writer, XMLInputFactory factory)
            throws XMLStreamException {

        if (factory == null) {

            factory = inputFactory;

        }

        XMLEventReader r = factory.createXMLEventReader(reader);
        XMLEventWriter w = new XMLStreamEventWriter(writer);

        try {

            w.add(r);

        } finally {

            // force any cached events to the underlying writer
            w.flush();

        }

    }

    /**
     * Copies the content read from a TrAX {@link Source} to a StAX
     * {@link XMLStreamWriter}.
     * 
     * @param source The content source.
     * @param writer The destination stream.
     * @throws XMLStreamException If an error occurs copying the content to the
     * 		stream.
     */
    public static final void copy(Source source, XMLStreamWriter writer)
            throws XMLStreamException {

        XMLStreamReader reader = inputFactory.createXMLStreamReader(source);
        copy(reader, writer);

    }

    /**
     * Copies the content read from a TrAX {@link Source} to a StAX
     * {@link XMLEventWriter}.
     * 
     * @param source The content source.
     * @param writer The destination event stream.
     * @throws XMLStreamException If an error occurs copying the content to the
     * 		event stream.
     */
    public static final void copy(Source source, XMLEventWriter writer)
            throws XMLStreamException {

        XMLEventReader reader = inputFactory.createXMLEventReader(source);
        copy(reader, writer);

    }

    /**
     * Copies the content read from a StAX {@link XMLEventReader} to a TrAX
     * {@link Result}.
     * 
     * @param reader The source event stream.
     * @param result The destination {@link Result}.
     * @throws XMLStreamException If an error occurs copying the content to the
     * 		result.
     */
    public static final void copy(XMLEventReader reader, Result result)
            throws XMLStreamException {

        XMLEventWriter writer = outputFactory.createXMLEventWriter(result);

        copy(reader, writer);

        // force any cached events to the result
        writer.flush();

    }

    /**
     * Copies the content read from a StAX {@link XMLStreamReader} to a TrAX
     * {@link Result}.
     * 
     * @param reader The source stream.
     * @param result The destination {@link Result}.
     * @throws XMLStreamException If an error occurs copying the content to the
     * 		result.
     */
    public static final void copy(XMLStreamReader reader, Result result)
            throws XMLStreamException {

        XMLStreamWriter writer = outputFactory.createXMLStreamWriter(result);

        copy(reader, writer);

        // force any cached content to the result
        writer.flush();

    }

    /**
     * Utility method that throws an exception if the provided reader is not
     * positioned before a StartElement event with the specified tag name.
     * 
     * @param reader The reader to test.
     * @param qname The required name of the start-tag. If <code>null</code>,
     *            any start tag is accepted.
     * @throws XMLStreamException If an error occurs reading from the stream.
     */
    public static final void requireStartElement(XMLEventReader reader,
            QName qname) throws XMLStreamException {

        if (reader.hasNext()) {

            XMLEvent nextEvent = reader.peek();
            if (nextEvent.isStartElement()) {

                if (qname != null) {

                    StartElement start = nextEvent.asStartElement();
                    QName name = start.getName();
                    if (!name.equals(qname)) {

                        throw new XMLStreamException(
                                "Encountered unexpected element; expected "
                                        + qname + ", but found " + name);

                    }

                }

            } else {

                throw new XMLStreamException(
                        "Encountered unexpected event; expected " + qname
                                + " start-tag, but found event " + nextEvent);

            }

        } else {

            throw new XMLStreamException(
                    "Encountered unexpected end of stream; expected element "
                            + qname);

        }

    }

    /**
     * Constructs a new StartElement that merges the attributes and namespaces
     * found in the specified StartElement, with the provided attributes. The
     * returned StartElement will contain all the attributes and namespaces of
     * the original, plus those defined in the map.
     * 
     * @param tag The original StartElement
     * @param attrs An iterator of Atributes to add to the element.
     * @return A new StartElement that contains all the original attributes and
     *         namespaces, plus the provided attributes.
     */
    public static StartElement mergeAttributes(StartElement tag,
            Iterator attrs, XMLEventFactory factory) {

        // create Attribute map
        Map attributes = new HashMap();

        // iterate through start tag's attributes
        for (Iterator i = tag.getAttributes(); i.hasNext();) {

            Attribute attr = (Attribute) i.next();
            attributes.put(attr.getName(), attr);

        }

        // iterate through new attributes
        while (attrs.hasNext()) {

            Attribute attr = (Attribute) attrs.next();
            attributes.put(attr.getName(), attr);

        }

        factory.setLocation(tag.getLocation());

        QName tagName = tag.getName();
        return factory.createStartElement(tagName.getPrefix(),
                tagName.getNamespaceURI(), tagName.getLocalPart(),
                attributes.values().iterator(), tag.getNamespaces(),
                tag.getNamespaceContext());

    }

    /**
     * Reads the text content of an element. The reader should be positioned in
     * front of a StartElement event, and will be read up to and including the
     * end element tag.
     * 
     * @param reader The event stream from which to read the element text.
     * @param elemName The optional name of the element being read. If this
     *            paramter is non- <code>null</code> then an exception will
     *            be thrown if the element read doesn't have the same name.
     * @return The text read from the element.
     * @throws XMLStreamException If an error occurs reading the stream, or if
     *             the read element doesn't match the provided QName.
     */
    public static final String readTextElement(XMLEventReader reader,
            QName elemName) throws XMLStreamException {

        if (elemName != null) {

            requireStartElement(reader, elemName);

        }

        // read text
        String text = reader.getElementText();

        // consume the end tag
        reader.nextEvent();

        return text;

    }

    /**
     * Advances the event stream until it encounters a start or end tag, but
     * does not actaully read the event.
     * 
     * @param reader The reader to peek.
     * @return The next StartElement or EndElement event, retrieved using
     *         <code>peek()</code>, or <code>null</code> if the end of the
     *         stream was encountered before any tag event.
     * @throws XMLStreamException If an error occurs reading the stream.
     */
    public static final XMLEvent nextTag(XMLEventReader reader)
            throws XMLStreamException {

        while (reader.hasNext()) {

            XMLEvent nextEvent = reader.peek();
            if (nextEvent.isStartElement() || nextEvent.isEndElement()) {

                return nextEvent;

            } else {

                // eat the event.
                reader.nextEvent();

            }

        }

        return null;

    }

    /**
     * Reads the events from the provided event stream until either a start or
     * end tag is encountered. In the former case, the start tag will be
     * returned, but if an end tag is encountered, <code>null</code> will be
     * returned. After returning, the stream will be positioned just before the
     * returned start element. The start element will not be consumed by this
     * method.
     * 
     * @param reader The event stream from which to read.
     * @return The StartElement read from the stream, or <code>null</code> if
     *         an end tag was found first, or the stream ended before a start
     *         element was found.
     * @throws XMLStreamException If an error occurs reading the stream.
     */
    public static final StartElement nextElement(XMLEventReader reader)
            throws XMLStreamException {

        return nextElement(reader, null);

    }

    /**
     * Reads the events from the provided event stream until either a start or
     * end tag is encountered. In the former case, the start tag will be
     * returned if it matches the specified QName, but if it doesn't match, an
     * end tag is encountered, or the stream ends, <code>null</code> will be
     * returned. After returning, the stream will be positioned just before the
     * start element. The start element will not be consumed by this method.
     * 
     * @param reader The event stream from which to read.
     * @param name The name of the element to read, or <code>null</code> to
     *            read any start tag.
     * @return The StartElement read from the stream, or <code>null</code> if
     *         the encountered start tag didn't match the specified QName, an
     *         end tag was found first, or the stream ended before a start
     *         element was found.
     * @throws XMLStreamException If an error occurs reading the stream.
     */
    public static final StartElement nextElement(XMLEventReader reader,
            QName name) throws XMLStreamException {

        while (reader.hasNext()) {

            XMLEvent nextEvent = reader.peek();
            if (nextEvent.isStartElement()) {

                StartElement start = nextEvent.asStartElement();
                if (name == null || start.getName().equals(name)) {

                    return start;

                } else {

                    break;

                }

            } else if (nextEvent.isEndElement()) {

                break;

            } else {

                // consume the event.
                reader.nextEvent();

            }

        }

        return null;

    }

}