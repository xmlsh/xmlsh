/*
 * $Id: StAXStreamWriter.java,v 1.1 2004/07/15 02:15:56 cniles Exp $
 * 
 * Copyright (c) 2004, Christian Niles, unit12.net
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
package javanet.staxutils.io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import javanet.staxutils.helpers.ElementContext;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * An {@link XMLStreamWriter} implementation that writes to a character stream.
 * 
 * @author Christian Niles
 * @version $Revision: 1.1 $
 */
public class StAXStreamWriter implements XMLStreamWriter {

    /** The destination stream. */
    private Writer writer;

    /** Whether the stream has been closed or not. */
    private boolean closed;

    /** The root namespace context. */
    private NamespaceContext rootContext;

    /** The current {@link ElementContext}. used to keep track of opened elements. */
    private ElementContext elementContext;

    /**
     * Constructs a <code>StAXStreamWriter</code> that writes to the provided
     * {@link OutputStream} using the platform default encoding.
     * 
     * @param stream The destination stream.
     */
    public StAXStreamWriter(OutputStream stream) {

        this(new OutputStreamWriter(stream));

    }

    /**
     * Constructs a <code>StAXStreamWriter</code> that writes to the provided
     * {@link OutputStream} using the specified encoding.
     * 
     * @param stream The destination stream.
     * @param encoding The output encoding.
     * @throws UnsupportedEncodingException If the encoding isn't supported.
     */
    public StAXStreamWriter(OutputStream stream, String encoding)
            throws UnsupportedEncodingException {

        this(new OutputStreamWriter(stream, encoding));

    }

    /**
     * Constructs a <code>StAXStreamWriter</code> that writes to the provided
     * {@link Writer}.
     * 
     * @param writer The destination {@link Writer} instance.
     */
    public StAXStreamWriter(Writer writer) {

        this.writer = writer;

    }

    /**
     * Constructs a <code>StAXStreamWriter</code> that writes to the provided
     * {@link Writer}.
     * 
     * @param writer The destination {@link Writer} instance.
     * @param rootContext The root namespace context.
     */
    public StAXStreamWriter(Writer writer, NamespaceContext rootContext) {

        this.writer = writer;
        this.rootContext = rootContext;

    }

    public synchronized void close() throws XMLStreamException {

        if (!closed) {

            flush();

            closed = true;
            writer = null;

        }

    }

    public synchronized void flush() throws XMLStreamException {

        // if cached element info exists, send it
        closeElementContext();

        try {

            writer.flush();

        } catch (IOException e) {

            throw new XMLStreamException(e);

        }

    }

    public String getPrefix(String uri) throws XMLStreamException {

        return getNamespaceContext().getPrefix(uri);

    }

    public Object getProperty(String name) throws IllegalArgumentException {

        // TODO provide access to properties?
        throw new IllegalArgumentException(name + " property not supported");

    }

    public void writeStartDocument() throws XMLStreamException {

        try {

            XMLWriterUtils.writeStartDocument(writer);

        } catch (IOException e) {

            throw new XMLStreamException(e);

        }

    }

    public void writeStartDocument(String version) throws XMLStreamException {

        try {

            XMLWriterUtils.writeStartDocument(version, writer);

        } catch (IOException e) {

            throw new XMLStreamException(e);

        }

    }

    public synchronized void writeStartDocument(String encoding, String version)
            throws XMLStreamException {

        // TODO perform check that StartDocument can be entered
        try {

            XMLWriterUtils.writeStartDocument(version, encoding, writer);

        } catch (IOException e) {

            throw new XMLStreamException(e);

        }

    }

    public synchronized void writeEndDocument() throws XMLStreamException {

        // flush any cached start element content
        closeElementContext();

        // close any tags
        while (elementContext != null) {

            writeEndElement();

        }

    }

    public synchronized void writeCData(String data) throws XMLStreamException {

        if (data == null) {

            throw new IllegalArgumentException("CDATA argument was null");

        }

        // flush any cached start element content
        closeElementContext();

        // TODO verify data is appropriate for CDATA
        try {

            XMLWriterUtils.writeCData(data, writer);

        } catch (IOException e) {

            throw new XMLStreamException(e);

        }

    }

    public synchronized void writeCharacters(char[] text, int start, int len)
            throws XMLStreamException {

        if (text == null) {

            throw new IllegalArgumentException(
                    "Character text argument was null");

        }

        // flush any cached start element content
        closeElementContext();

        try {

            XMLWriterUtils.writeCharacters(text, start, len, writer);

        } catch (IOException e) {

            throw new XMLStreamException(e);

        }

    }

    public synchronized void writeCharacters(String text)
            throws XMLStreamException {

        if (text == null) {

            throw new IllegalArgumentException(
                    "Character text argument was null");

        }

        // flush any cached start element content
        closeElementContext();

        try {

            XMLWriterUtils.writeCharacters(text, writer);

        } catch (IOException e) {

            throw new XMLStreamException(e);

        }

    }

    public synchronized void writeComment(String data)
            throws XMLStreamException {

        if (data == null) {

            throw new IllegalArgumentException("Comment data argument was null");

        }

        // flush any cached start element content
        closeElementContext();

        // TODO check comment for non-SGML compatible characters?
        try {

            XMLWriterUtils.writeComment(data, writer);

        } catch (IOException e) {

            throw new XMLStreamException(e);

        }

    }

    public synchronized void writeDTD(String dtd) throws XMLStreamException {

        if (dtd == null) {

            throw new IllegalArgumentException("dtd argument was null");

        }

        try {

            XMLWriterUtils.writeDTD(dtd, writer);

        } catch (IOException e) {

            throw new XMLStreamException(e);

        }

    }

    public synchronized void writeEntityRef(String name)
            throws XMLStreamException {

        // flush any cached start element content
        closeElementContext();

        try {

            XMLWriterUtils.writeEntityReference(name, writer);

        } catch (IOException e) {

            throw new XMLStreamException(e);

        }

    }

    public synchronized void writeProcessingInstruction(String target,
            String data) throws XMLStreamException {

        // flush any cached start element content
        closeElementContext();

        // TODO test processing instruction validity?
        try {

            XMLWriterUtils.writeProcessingInstruction(target, data, writer);

        } catch (IOException e) {

            throw new XMLStreamException(e);

        }

    }

    public void writeProcessingInstruction(String target)
            throws XMLStreamException {

        writeProcessingInstruction(target, null);

    }

    public NamespaceContext getNamespaceContext() {

        return elementContext;

    }

    public void setNamespaceContext(NamespaceContext context)
            throws XMLStreamException {

        if (this.rootContext == null && elementContext == null) {

            this.rootContext = context;

        } else {

            throw new IllegalStateException(
                    "NamespaceContext has already been set or document is already in progress");

        }

    }

    public synchronized void setDefaultNamespace(String uri)
            throws XMLStreamException {

        elementContext.putNamespace("", uri);

    }

    public synchronized void setPrefix(String prefix, String uri)
            throws XMLStreamException {

        elementContext.putNamespace(prefix, uri);

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

    public void writeStartElement(String prefix, String localName,
            String namespaceURI) throws XMLStreamException {

        writeStartElement(prefix, localName, namespaceURI, false);

    }

    public void writeStartElement(String namespaceURI, String localName)
            throws XMLStreamException {

        writeStartElement("", localName, namespaceURI, false);

    }

    public void writeStartElement(String localName) throws XMLStreamException {

        writeStartElement("", localName, "", false);

    }

    public void writeEmptyElement(String prefix, String localName,
            String namespaceURI) throws XMLStreamException {

        writeStartElement(prefix, localName, namespaceURI, true);

    }

    public void writeEmptyElement(String namespaceURI, String localName)
            throws XMLStreamException {

        writeStartElement("", localName, namespaceURI, true);

    }

    public void writeEmptyElement(String localName) throws XMLStreamException {

        writeStartElement("", localName, "", true);

    }

    public synchronized void writeAttribute(QName name, String value)
            throws XMLStreamException {

        if (elementContext == null || elementContext.isReadOnly()) {

            throw new XMLStreamException(
                    getCurrentPath()
                            + ": attributes must be written directly following a start element.");

        }

        elementContext.putAttribute(name, value);

    }

    public void writeAttribute(String prefix, String namespaceURI,
            String localName, String value) throws XMLStreamException {

        if (prefix == null) {

            throw new IllegalArgumentException(
                    "attribute prefix may not be null @ [" + getCurrentPath()
                            + "]");

        } else if (localName == null) {

            throw new IllegalArgumentException(
                    "attribute localName may not be null @ ["
                            + getCurrentPath() + "]");

        } else if (namespaceURI == null) {

            throw new IllegalArgumentException(
                    "attribute namespaceURI may not be null @ ["
                            + getCurrentPath() + "]");

        }

        writeAttribute(new QName(namespaceURI, localName, prefix), value);

    }

    public void writeAttribute(String namespaceURI, String localName,
            String value) throws XMLStreamException {

        writeAttribute("", namespaceURI, localName, value);

    }

    public void writeAttribute(String localName, String value)
            throws XMLStreamException {

        writeAttribute("", "", localName, value);

    }

    public void writeDefaultNamespace(String namespaceURI)
            throws XMLStreamException {

        writeNamespace("", namespaceURI);

    }

    public synchronized void writeNamespace(String prefix, String namespaceURI)
            throws XMLStreamException {

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

    public synchronized void writeEndElement() throws XMLStreamException {

        // flush any cached start element content
        closeElementContext();

        if (elementContext != null) {

            QName name = elementContext.getName();
            try {

                XMLWriterUtils.writeEndElement(name, writer);

            } catch (IOException e) {

                throw new XMLStreamException(getCurrentPath()
                        + ": Error writing end element to stream", e);

            }

            // pop the context
            elementContext = elementContext.getParentContext();

        } else {

            throw new XMLStreamException("Unmatched END_ELEMENT");

        }

    }

    /**
     * Returns the current position of the writer as a path of {@link QName} strings.
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

                writer.write('<');
                XMLWriterUtils.writeQName(elementContext.getName(), writer);

                for (int i = 0, s = elementContext.attributeCount(); i < s; i++) {

                    QName name = elementContext.getAttributeName(i);
                    String value = elementContext.getAttribute(i);

                    writer.write(' ');
                    XMLWriterUtils.writeAttribute(name, value, writer);

                }

                for (int i = 0, s = elementContext.namespaceCount(); i < s; i++) {

                    String prefix = elementContext.getNamespacePrefix(i);
                    String uri = elementContext.getNamespaceURI(i);

                    writer.write(' ');
                    XMLWriterUtils.writeNamespace(prefix, uri, writer);

                }

                if (elementContext.isEmpty()) {

                    writer.write("/>");
                    elementContext = elementContext.getParentContext();

                } else {

                    writer.write('>');

                }

            } catch (IOException e) {

                throw new XMLStreamException(getCurrentPath()
                        + ": error writing start tag to stream", e);

            }

        }

    }
}