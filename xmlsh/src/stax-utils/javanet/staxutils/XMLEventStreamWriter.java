/*
 * Copyright (c) 2006, John Kristian
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *      *   Redistributions of source code must retain the above copyright
 *          notice, this list of conditions and the following disclaimer.
 *
 *      *   Redistributions in binary form must reproduce the above copyright
 *          notice, this list of conditions and the following disclaimer in the
 *          documentation and/or other materials provided with the distribution.
 *
 *      *   Neither the name of StAX-Utils nor the names of its contributors
 *          may be used to endorse or promote products derived from this
 *          software without specific prior written permission.
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
import java.util.Iterator;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.StartElement;

/**
 * An {@link javax.xml.stream.XMLStreamWriter} that writes events to a
 * {@link javax.xml.stream.XMLEventWriter}.
 *
 * @author <a href="mailto:jk2006@engineer.com">John Kristian</a>
 */
public class XMLEventStreamWriter implements XMLStreamWriter {

    public XMLEventStreamWriter(XMLEventWriter out) {
        this(out, XMLEventFactory.newInstance());
    }

    public XMLEventStreamWriter(XMLEventWriter out, XMLEventFactory factory) {
        this.out = out;
        this.factory = factory;
    }

    private XMLEventWriter out;

    private XMLEventFactory factory;

    private static final String DEFAULT_ENCODING = "UTF-8";

    /** The number of elements that have been started but not ended. */
    private int depth = 0;

    /**
     * Markup that matches previously started elements. stack[depth - 1] matches
     * the element that was started most recently.
     */
    private EndElement[] stack = { null, null, null, null };

    private void write(StartElement start) throws XMLStreamException {
        if (stack.length <= depth) {
            // Grow the stack:
            EndElement[] newStack = new EndElement[stack.length * 2];
            System.arraycopy(stack, 0, newStack, 0, stack.length);
            stack = newStack;
        }
        out.add(start);
        // If that succeeded:
        stack[depth++] = factory.createEndElement(start.getName(), null);
    }

    private void write(Namespace space) throws XMLStreamException {
        EndElement end;
        {
            Collection spaces = new ArrayList();
            EndElement oldEnd = stack[depth - 1];
            Iterator oldSpaces = oldEnd.getNamespaces();
            if (oldSpaces != null) {
                while (oldSpaces.hasNext()) {
                    spaces.add(oldSpaces.next());
                }
            }
            spaces.add(space);
            end = factory.createEndElement(oldEnd.getName(), spaces.iterator());
        }
        out.add(space);
        // If that succeeded:
        stack[depth - 1] = end;
    }

    public Object getProperty(String name) throws IllegalArgumentException {
        throw new IllegalArgumentException(); // not supported
    }

    public void setNamespaceContext(NamespaceContext context) throws XMLStreamException {
        out.setNamespaceContext(context);
    }

    public NamespaceContext getNamespaceContext() {
        return out.getNamespaceContext();
    }

    public void setDefaultNamespace(String uri) throws XMLStreamException {
        out.setDefaultNamespace(uri);
    }

    public String getPrefix(String uri) throws XMLStreamException {
        return out.getPrefix(uri);
    }

    public void setPrefix(String prefix, String uri) throws XMLStreamException {
        out.setPrefix(prefix, uri);
    }

    public void writeStartDocument() throws XMLStreamException {
        out.add(factory.createStartDocument(DEFAULT_ENCODING));
    }

    public void writeStartDocument(String version) throws XMLStreamException {
        writeStartDocument(DEFAULT_ENCODING, version);
    }

    public void writeStartDocument(String encoding, String version) throws XMLStreamException {
        out.add(factory.createStartDocument(encoding, version));
    }

    public void writeDTD(String dtd) throws XMLStreamException {
        out.add(factory.createDTD(dtd));
    }

    public void writeComment(String data) throws XMLStreamException {
        out.add(factory.createComment(data));
    }

    public void writeProcessingInstruction(String target) throws XMLStreamException {
        writeProcessingInstruction(target, "");
    }

    public void writeProcessingInstruction(String target, String data) throws XMLStreamException {
        out.add(factory.createProcessingInstruction(target, data));
    }

    public void writeEmptyElement(String localName) throws XMLStreamException {
        writeStartElement(localName);
        writeEndElement();
    }

    public void writeEmptyElement(String namespaceURI, String localName) throws XMLStreamException {
        writeStartElement(namespaceURI, localName);
        writeEndElement();
    }

    public void writeEmptyElement(String prefix, String localName, String namespaceURI)
            throws XMLStreamException {
        writeStartElement(prefix, localName, namespaceURI);
        writeEndElement();
    }

    public void writeStartElement(String localName) throws XMLStreamException {
        write(factory.createStartElement(new QName(localName), null, null));
    }

    public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {
        write(factory.createStartElement(new QName(namespaceURI, localName), null, null));
    }

    public void writeStartElement(String prefix, String localName, String namespaceURI)
            throws XMLStreamException {
        write(factory.createStartElement(new QName(namespaceURI, localName, prefix), null, null));
    }

    public void writeAttribute(String localName, String value) throws XMLStreamException {
        out.add(factory.createAttribute(localName, value));
    }

    public void writeAttribute(String namespaceURI, String localName, String value)
            throws XMLStreamException {
        out.add(factory.createAttribute(new QName(namespaceURI, localName), value));
    }

    public void writeAttribute(String prefix, String namespaceURI, String localName, String value)
            throws XMLStreamException {
        out.add(factory.createAttribute(prefix, namespaceURI, localName, value));
    }

    public void writeDefaultNamespace(String namespaceURI) throws XMLStreamException {
        write(factory.createNamespace(namespaceURI));
    }

    public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException {
        write(factory.createNamespace(prefix, namespaceURI));
    }

    public void writeCharacters(String text) throws XMLStreamException {
        out.add(factory.createCharacters(text));
    }

    public void writeCharacters(char[] text, int start, int len) throws XMLStreamException {
        writeCharacters(new String(text, start, len));
    }

    public void writeCData(String data) throws XMLStreamException {
        out.add(factory.createCData(data));
    }

    public void writeEntityRef(String name) throws XMLStreamException {
        out.add(factory.createEntityReference(name, null /* EventDeclaration */));
    }

    public void writeEndElement() throws XMLStreamException {
        if (depth <= 0) { // erroneous
            // Let this.out throw the exception:
            out.add(factory.createEndElement(new QName("unknown"), null));
        } else {
            out.add(stack[depth - 1]);
            // If that succeeded:
            --depth;
            stack[depth] = null; // to enable garbage collection
        }
    }

    public void writeEndDocument() throws XMLStreamException {
        try {
            while (depth > 0) {
                writeEndElement();
            }
        } catch (Exception ignored) {
        }
        out.add(factory.createEndDocument());
        // If that succeeded:
        depth = 0;
    }

    public void flush() throws XMLStreamException {
        out.flush();
    }

    public void close() throws XMLStreamException {
        out.close();
    }

}
