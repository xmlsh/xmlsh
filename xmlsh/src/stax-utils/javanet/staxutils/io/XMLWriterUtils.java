/*
 * $Id: XMLWriterUtils.java,v 1.6 2005/03/03 18:37:05 ryan_shoemaker Exp $
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
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;

import javanet.staxutils.XMLStreamUtils;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.Comment;
import javax.xml.stream.events.DTD;
import javax.xml.stream.events.EndDocument;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.EntityDeclaration;
import javax.xml.stream.events.EntityReference;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.NotationDeclaration;
import javax.xml.stream.events.ProcessingInstruction;
import javax.xml.stream.events.StartDocument;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 * Set of utility methods useful when writing XML.
 * 
 * @author Christian Niles
 * @version $Revision: 1.6 $
 */
public final class XMLWriterUtils {

    /**
     * Prevent instantiation.
     */
    private XMLWriterUtils() {

    }

    /**
     * Writes a quoted version of the given value, automatically determining the
     * appropriate quote character. The value will not be encoded before being
     * written. This method is useful when writing quoted DTD values, such as system
     * IDs.
     * 
     * @param value The value to quote and output.
     * @param writer The destination stream.
     * @throws IOException If an error occurs writing to the stream.
     */
    public static final void writeQuotedValue(String value, Writer writer)
            throws IOException {

        char quoteChar = (value.indexOf('"') < 0 ? '"' : '\'');
        writer.write(quoteChar);
        writer.write(value);
        writer.write(quoteChar);

    }

    /**
     * Encodes the given value and writes it to the provided stream, wrapping it in
     * the appropriate quote character. This method is useful when writing attribute
     * values, and entity replacement text.
     * 
     * @param value The value to encode, quote, and output.
     * @param writer The destination stream.
     * @throws IOException If an error occurs writing to the stream.
     */
    public static final void writeEncodedQuotedValue(String value, Writer writer)
            throws IOException {

        char quoteChar = (value.indexOf('"') < 0 ? '"' : '\'');
        writer.write(quoteChar);
        writeEncodedValue(value, quoteChar, writer);
        writer.write(quoteChar);

    }

    /**
     * Encodes the given value, and writes it to the stream, but does not actually
     * wrap the value in the quote character. The provided quote character is used to
     * determine whether a character must be encoded or not.
     * 
     * @param value The value to encode and output.
     * @param quoteChar The quote character; used to determine which characters need
     * 		to be encoded.
     * @param writer The destination stream.
     * @throws IOException If an error occurs writing to the stream.
     */
    public static final void writeEncodedValue(String value, char quoteChar,
            Writer writer) throws IOException {

        // write value, encoding special characters along the way
        for (int i = 0, s = value.length(); i < s; i++) {

            char c = value.charAt(i);
            if (c == '\'') {

                writer.write(quoteChar == '\'' ? "&apos;" : "'");

            } else if (c == '\"') {

                writer.write(quoteChar == '\"' ? "&quot;" : "\"");

            } else if (c == '\n') {

                writer.write("&#xA;");

            } else {

                writeEncodedCharacter(c, writer);

            }

        }

    }

    /**
     * Encodes the provided text and writes it to the provided stream. This method is
     * useful when writing character data, such as element text or CData sections,
     * and will not encode single or double quotes.
     * 
     * @param text The text to write.
     * @param writer The destination stream.
     * @throws IOException If an error occurs writing to the stream.
     */
    public static final void writeEncodedText(CharSequence text, Writer writer)
            throws IOException {

        for (int i = 0, s = text.length(); i < s; i++) {

            writeEncodedCharacter(text.charAt(i), writer);

        }

    }

    public static final void writeEncodedText(char[] text, int start, int len,
            Writer writer) throws IOException {

        for (int i = start, s = start + len; i < s; i++) {

            writeEncodedCharacter(text[i], writer);

        }

    }

    /**
     * Encodes the provided character if needed, and writes it to an output stream.
     * 
     * @param c The character to encode and output.
     * @param writer The destination stream.
     * @throws IOException If an error occurs writing to the stream.
     */
    public static final void writeEncodedCharacter(char c, Writer writer)
            throws IOException {

        if (c == '&') {

            writer.write("&amp;");

        } else if (c == '<') {

            writer.write("&lt;");

        } else if (c == '>') {

            writer.write("&gt;");

        } else if (c == '\r') {

            writer.write("&#xD;");

        } else {

            writer.write(c);

        }

    }

    /**
     * Writes a qualified name to the provided stream.
     * 
     * @param name The name to write.
     * @param writer The destination stream.
     * @throws IOException If an error occurs writing to the stream.
     */
    public static final void writeQName(QName name, Writer writer)
            throws IOException {

        String prefix = name.getPrefix();
        if (prefix != null && prefix.length() > 0) {

            writer.write(prefix);
            writer.write(':');

        }

        writer.write(name.getLocalPart());

    }

    /**
     * Writes a qualified name to the provided stream.
     * 
     * @param prefix The prefix, or <code>null</code>
     * @param localPart The local part.
     * @param writer The destination stream.
     * @throws IOException If an error occurs writing to the stream.
     */
    public static final void writeQName(String prefix, String localPart,
            Writer writer) throws IOException {

        if (prefix != null && prefix.length() > 0) {

            writer.write(prefix);
            writer.write(':');

        }

        writer.write(localPart);

    }

    /**
     * Writes the given event to the provided {@link Writer}. This method
     * will delegate the actual task of writing to the stream to the appropriate
     * overloaded method.
     * 
     * @param event The event to write.
     * @param writer The destination XML stream.
     * @throws IOException If an error occurs writing to the stream.
     * @throws XMLStreamException If the event is a StartElement and an error occurs
     * 		writing the Namespace or Attribute events.
     * @throws IllegalArgumentException If the event is an unknown type.
     */
    public static final void writeEvent(XMLEvent event, Writer writer)
            throws IOException, XMLStreamException {

        int eventType = event.getEventType();
        switch (eventType) {

            case XMLEvent.START_ELEMENT :
                writeStartElement(event.asStartElement(), false, writer);
                break;

            case XMLEvent.END_ELEMENT :
                writeEndElement(event.asEndElement(), writer);
                break;

            case XMLEvent.CHARACTERS :
            case XMLEvent.SPACE :
            case XMLEvent.CDATA :
                writeCharacters(event.asCharacters(), writer);
                break;

            case XMLEvent.COMMENT :
                writeComment((Comment) event, writer);
                break;

            case XMLEvent.ENTITY_REFERENCE :
                writeEntityReference((EntityReference) event, writer);
                break;

            case XMLEvent.PROCESSING_INSTRUCTION :
                writeProcessingInstruction((ProcessingInstruction) event,
                        writer);
                break;

            case XMLEvent.DTD :
                writeDTD((DTD) event, writer);
                break;

            case XMLEvent.START_DOCUMENT :
                writeStartDocument((StartDocument) event, writer);
                break;

            case XMLEvent.END_DOCUMENT :
                writeEndDocument((EndDocument) event, writer);
                break;

            case XMLEvent.NAMESPACE :
                writeNamespace((Namespace) event, writer);
                break;

            case XMLEvent.ATTRIBUTE :
                writeAttribute((Attribute) event, writer);
                break;

            case XMLEvent.ENTITY_DECLARATION :
                writeEntityDeclaration((EntityDeclaration) event, writer);
                break;

            case XMLEvent.NOTATION_DECLARATION :
                writeNotationDeclaration((NotationDeclaration) event, writer);
                break;

            default :
                throw new IllegalArgumentException("Unrecognized event ("
                        + XMLStreamUtils.getEventTypeName(eventType) + "): "
                        + event);

        }

    }

    /**
     * Writes a {@link StartDocument} to the provided stream.
     * 
     * @param start The {@link StartDocument} to write.
     * @param writer The destination stream.
     * @throws IOException If an error occurs writing to the stream.
     */
    public static final void writeStartDocument(StartDocument start,
            Writer writer) throws IOException {

        String version = start.getVersion();
        String encoding = start.getCharacterEncodingScheme();

        if (start.standaloneSet()) {

            writeStartDocument(version, encoding, start.isStandalone(), writer);

        } else {

            writeStartDocument(version, encoding, writer);

        }

    }

    /**
     * Writes a default XML declaration to the provided stream.
     * 
     * @param writer The destination stream.
     * @throws IOException If an error occurs writing to the stream.
     */
    public static final void writeStartDocument(Writer writer)
            throws IOException {

        writeStartDocument("1.0", null, null, writer);

    }

    /**
     * Writes an XML declaration to the provided stream.
     * 
     * @param version The xml version definition.
     * @param writer The destination stream.
     * @throws IOException If an error occurs writing to the stream.
     */
    public static final void writeStartDocument(String version, Writer writer)
            throws IOException {

        writeStartDocument(version, null, null, writer);

    }

    /**
     * Writes an XML declaration to the provided stream.
     * 
     * @param version The xml version definition.
     * @param encoding The document encoding, or <code>null</code>
     * @param writer The destination stream.
     * @throws IOException If an error occurs writing to the stream.
     */
    public static final void writeStartDocument(String version,
            String encoding, Writer writer) throws IOException {

        writeStartDocument(version, encoding, null, writer);

    }

    /**
     * Writes an XML declaration to the provided stream.
     * 
     * @param version The xml version definition.
     * @param encoding The document encoding, or <code>null</code>
     * @param standalone The standalone definition
     * @param writer The destination stream.
     * @throws IOException If an error occurs writing to the stream.
     */
    public static final void writeStartDocument(String version,
            String encoding, boolean standalone, Writer writer)
            throws IOException {

        writeStartDocument(version, encoding, (standalone ? "yes" : "no"),
                writer);

    }

    /**
     * Writes an XML declaration to the provided stream.
     * 
     * @param version The xml version definition.
     * @param encoding The document encoding, or <code>null</code>
     * @param standalone The standalone definition, or <code>null</code>
     * @param writer The destination stream.
     * @throws IOException If an error occurs writing to the stream.
     */
    public static final void writeStartDocument(String version,
            String encoding, String standalone, Writer writer)
            throws IOException {

        writer.write("<?xml version=");
        XMLWriterUtils.writeQuotedValue(version, writer);

        if (encoding != null) {

            writer.write(" encoding=");
            XMLWriterUtils.writeQuotedValue(encoding, writer);

        }

        if (standalone != null) {

            writer.write(" standalone=");
            XMLWriterUtils.writeQuotedValue(standalone, writer);

        }

        writer.write("?>");

    }

    /**
     * Writes an {@link EndDocument} to the provided stream.
     * 
     * @param end The {@link EndDocument} to write.
     * @param writer The destination stream.
     * @throws IOException If an error occurs writing to the stream.
     */
    public static final void writeEndDocument(EndDocument end, Writer writer)
            throws IOException {

        writeEndDocument(writer);

    }

    /**
     * Writes an document ending to the provided stream.
     * 
     * @param writer The destination stream.
     * @throws IOException If an error occurs writing to the stream.
     */
    public static final void writeEndDocument(Writer writer) throws IOException {

        // nothing to write?

    }

    /**
     * Writes a {@link StartElement} event to the provided stream.
     * 
     * @param start The {@link StartElement} event to write.
     * @param writer The destination stream.
     * @throws IOException If an error occurs writing to the stream.
     * @throws XMLStreamException If an error occurs writing any namespaces or
     * 		attribute events.
     */
    public static final void writeStartElement(StartElement start, Writer writer)
            throws IOException, XMLStreamException {

        writeStartElement(start.getName(), start.getAttributes(),
                start.getNamespaces(), false, writer);

    }

    /**
     * Writes a {@link StartElement} event to the provided stream.
     * 
     * @param start The {@link StartElement} event to write.
     * @param empty Whether the element is empty.
     * @param writer The destination stream.
     * @throws IOException If an error occurs writing to the stream.
     * @throws XMLStreamException If an error occurs writing any namespaces or
     * 		attribute events.
     */
    public static final void writeStartElement(StartElement start,
            boolean empty, Writer writer) throws IOException,
            XMLStreamException {

        writeStartElement(start.getName(), start.getAttributes(),
                start.getNamespaces(), empty, writer);

    }

    /**
     * Writes a start tag and any associated namespaces and attributes to the
     * provided stream.
     * 
     * @param name The tag name.
     * @param attributes An {@link Attribute} iterator, or <code>null</code>.
     * @param namespaces A {@link Namespace} iterator, or <code>null</code>.
     * @param writer The destination stream.
     * @throws IOException If an error occurs writing to the stream.
     * @throws XMLStreamException If an error occurs writing any namespaces or
     * 		attribute events.
     */
    public static final void writeStartElement(QName name, Iterator attributes,
            Iterator namespaces, Writer writer) throws IOException,
            XMLStreamException {

        writeStartElement(name, attributes, namespaces, false, writer);

    }

    /**
     * Writes a start tag and any associated namespaces and attributes to the
     * provided stream.
     * 
     * @param name The tag name.
     * @param attributes An {@link Attribute} iterator, or <code>null</code>.
     * @param namespaces A {@link Namespace} iterator, or <code>null</code>.
     * @param empty Whether the element is empty.
     * @param writer The destination stream.
     * @throws IOException If an error occurs writing to the stream.
     * @throws XMLStreamException
     */
    public static final void writeStartElement(QName name, Iterator attributes,
            Iterator namespaces, boolean empty, Writer writer)
            throws IOException, XMLStreamException {

        writer.write('<');
        XMLWriterUtils.writeQName(name, writer);

        // send namespaces
        if (namespaces != null) {

            while (namespaces.hasNext()) {

                Namespace ns = (Namespace) namespaces.next();
                writer.write(' ');
                ns.writeAsEncodedUnicode(writer);

            }

        }

        // write attributes
        if (attributes != null) {

            while (attributes.hasNext()) {

                Attribute attr = (Attribute) attributes.next();
                writer.write(' ');
                attr.writeAsEncodedUnicode(writer);

            }

        }

        if (empty) {

            writer.write("/>");

        } else {

            writer.write('>');

        }

    }

    /**
     * Writes a start tag and any associated namespaces and attributes to the
     * provided stream.
     * 
     * @param name The tag name.
     * @param attributes A {@link Map} of attribute values, keyed by their
     * 		{@link QName}s. If no attributes are present, this may be null.
     * @param namespaces A {@link Map} of namespace values, keyed by their
     * 		prefixes. If no namespaces are present, this may be null.
     * @param empty Whether the element is empty.
     * @param writer The destination stream.
     * @throws IOException If an error occurs writing to the stream.
     */
    public static final void writeStartElement(QName name, Map attributes,
            Map namespaces, boolean empty, Writer writer) throws IOException {

        writer.write('<');
        XMLWriterUtils.writeQName(name, writer);

        // send namespaces
        if (namespaces != null) {

            for (Iterator i = namespaces.entrySet().iterator(); i.hasNext();) {

                Map.Entry entry = (Map.Entry) i.next();
                writer.write(' ');
                writeNamespace((String) entry.getKey(),
                        (String) entry.getValue(), writer);

            }

        }

        // write attributes
        if (attributes != null) {

            for (Iterator i = attributes.entrySet().iterator(); i.hasNext();) {

                Map.Entry entry = (Map.Entry) i.next();
                writer.write(' ');
                writeAttribute((QName) entry.getKey(),
                        (String) entry.getValue(), writer);

            }

        }

        if (empty) {

            writer.write("/>");

        } else {

            writer.write('>');

        }

    }

    /**
     * Writes an {@link Attribute} to the provided stream.
     * 
     * @param attr The {@link Attribute} to write.
     * @param writer The destination stream.
     * @throws IOException If an error occurs writing to the stream.
     */
    public static final void writeAttribute(Attribute attr, Writer writer)
            throws IOException {

        QName name = attr.getName();
        String value = attr.getValue();
        writeAttribute(name, value, writer);

    }

    /**
     * Writes an attribute to the provided stream.
     * 
     * @param name The attribute name.
     * @param value The attribute value.
     * @param writer The destination stream.
     * @throws IOException If an error occurs writing to the stream.
     */
    public static final void writeAttribute(QName name, String value,
            Writer writer) throws IOException {

        XMLWriterUtils.writeQName(name, writer);
        writer.write('=');
        XMLWriterUtils.writeEncodedQuotedValue(value, writer);

    }

    /**
     * Writes a {@link Namespace} to the provided stream.
     * 
     * @param ns The {@link Namespace} to write.
     * @param writer The destination stream.
     * @throws IOException If an error occurs writing to the stream.
     */
    public static final void writeNamespace(Namespace ns, Writer writer)
            throws IOException {

        String prefix = ns.getPrefix();
        String uri = ns.getNamespaceURI();
        writeNamespace(prefix, uri, writer);

    }

    /**
     * Writes a {@link Namespace} to the provided stream.
     * 
     * @param prefix The namespace prefix, which may be <code>null</code>.
     * @param uri The namespace uri.
     * @param writer The destination stream.
     * @throws IOException If an error occurs writing to the stream.
     */
    public static final void writeNamespace(String prefix, String uri,
            Writer writer) throws IOException {

        writer.write("xmlns");
        if (prefix != null && prefix.length() > 0) {

            writer.write(':');
            writer.write(prefix);

        }

        writer.write('=');
        XMLWriterUtils.writeEncodedQuotedValue(uri, writer);

    }

    /**
     * Writes an {@link EndElement} to the provided stream.
     * 
     * @param end The {@link EndElement} to write.
     * @param writer The destination stream.
     * @throws IOException If an error occurs writing to the stream.
     */
    public static final void writeEndElement(EndElement end, Writer writer)
            throws IOException {

        writeEndElement(end.getName(), writer);

    }

    /**
     * Writes an element end tag to the provided stream.
     * 
     * @param name The element name.
     * @param writer The destination stream.
     * @throws IOException If an error occurs writing to the stream.
     */
    public static final void writeEndElement(QName name, Writer writer)
            throws IOException {

        writer.write("</");
        XMLWriterUtils.writeQName(name, writer);
        writer.write('>');

    }

    /**
     * Writes a {@link Characters} to the provided stream.
     * 
     * @param chars The {@link Characters} to write.
     * @param writer The destination stream.
     * @throws IOException If an error occurs writing to the stream.
     */
    public static final void writeCharacters(Characters chars, Writer writer)
            throws IOException {

        if (chars.isCData()) {

            writeCData(chars.getData(), writer);

        } else {

            writeCharacters(chars.getData(), writer);

        }

    }

    /**
     * Writes a chunk of encoded text to the provided stream.
     * 
     * @param text The text to write.
     * @param writer The destination stream.
     * @throws IOException If an error occurs writing to the stream.
     */
    public static final void writeCharacters(CharSequence text, Writer writer)
            throws IOException {

        XMLWriterUtils.writeEncodedText(text, writer);

    }

    /**
     * Writes a chunk of encoded text to the provided stream.
     * 
     * @param data A character array containing the characters.
     * @param start The starting index into the array.
     * @param length The number of characters to write.
     * @param writer The destination stream.
     * @throws IOException If an error occurs writing to the stream.
     */
    public static final void writeCharacters(char[] data, int start,
            int length, Writer writer) throws IOException {

        XMLWriterUtils.writeEncodedText(data, start, length, writer);

    }

    /**
     * Writes a {@link Characters} to the provided stream.
     * 
     * @param text The CData text to write.
     * @param writer The destination stream.
     * @throws IOException If an error occurs writing to the stream.
     */
    public static final void writeCData(String text, Writer writer)
            throws IOException {

        writer.write("<![CDATA[");
        writer.write(text);
        writer.write("]]>");

    }

    /**
     * Writes a {@link Characters} to the provided stream.
     * 
     * @param data A character array containing the cdata text.
     * @param start The starting index into the array.
     * @param length The number of characters to write.
     * @param writer The destination stream.
     * @throws IOException If an error occurs writing to the stream.
     */
    public static final void writeCData(char[] data, int start, int length,
            Writer writer) throws IOException {

        writer.write("<![CDATA[");
        writer.write(data, start, length);
        writer.write("]]>");

    }

    /**
     * Writes a {@link Comment} to the provided stream.
     * 
     * @param comment The {@link Comment} to write.
     * @param writer The destination stream.
     * @throws XMLStreamException If an error occurs writing to the stream.
     */
    public static final void writeComment(Comment comment, Writer writer)
            throws IOException {

        writeComment(comment.getText(), writer);

    }

    /**
     * Writes a comment to the provided stream.
     * 
     * @param comment The comment text.
     * @param writer The destination stream.
     * @throws XMLStreamException If an error occurs writing to the stream.
     */
    public static final void writeComment(String comment, Writer writer)
            throws IOException {

        writer.write("<!--");
        writer.write(comment);
        writer.write("-->");

    }

    /**
     * Writes an {@link EntityReference} to the provided stream.
     * 
     * @param entityRef The {@link EntityReference} to write.
     * @param writer The destination stream.
     * @throws XMLStreamException If an error occurs writing to the stream.
     */
    public static final void writeEntityReference(EntityReference entityRef,
            Writer writer) throws IOException {

        writeEntityReference(entityRef.getName(), writer);

    }

    /**
     * Writes an entity reference to the provided stream.
     * 
     * @param entityRef The name of the entity reference.
     * @param writer The destination stream.
     * @throws XMLStreamException If an error occurs writing to the stream.
     */
    public static final void writeEntityReference(String entityRef,
            Writer writer) throws IOException {

        writer.write('&');
        writer.write(entityRef);
        writer.write(';');

    }

    /**
     * Writes an {@link EntityDeclaration} to the stream.
     * 
     * @param declaration The {@link EntityDeclaration} to write.
     * @param writer The destination stream.
     * @throws IOException If an error occurs writing to the stream.
     */
    public static final void writeEntityDeclaration(EntityDeclaration declaration,
            Writer writer) throws IOException {

        String name = declaration.getName();
        String notation = declaration.getNotationName();

        String text = declaration.getReplacementText();
        if (text != null) {

            writeEntityDeclaration(name, text, notation, writer);

        } else {

            String publicId = declaration.getPublicId();
            String systemId = declaration.getSystemId();

            writeEntityDeclaration(name, publicId, systemId, notation, writer);

        }

    }

    /**
     * Writes an external entity declaration to the stream. Either or both of the
     * <code>publicId</code> and <code>systemId</code> parameters must be
     * non-<code>null</code>.
     * 
     * @param name The entity name.
     * @param publicId The entity public ID, or <code>null</code>.
     * @param systemId The entity system ID, or <code>null</code>.
     * @param notation The notation name, or <code>null</code>.
     * @param writer The destination stream.
     * @throws IOException If an error occurs writing to the stream.
     */
    public static final void writeEntityDeclaration(String name, String publicId,
            String systemId, String notation, Writer writer) throws IOException {

        writer.write("<!ENTITY ");
        writer.write(name);

        if (publicId != null) {

            // write public/system ids
            writer.write("PUBLIC ");
            XMLWriterUtils.writeQuotedValue(publicId, writer);
            if (systemId != null) {

                writer.write(" ");
                XMLWriterUtils.writeQuotedValue(systemId, writer);

            }

        } else {

            // write system identifier
            writer.write("SYSTEM ");
            XMLWriterUtils.writeQuotedValue(systemId, writer);

        }

        // write optional notation
        if (notation != null) {

            writer.write(" NDATA");
            writer.write(notation);

        }

        writer.write(">");

    }

    /**
     * Writes an internal entity declaration to the stream.
     * 
     * @param name The entity name.
     * @param text The entity replacement text.
     * @param notation The notation name, or <code>null</code>.
     * @param writer
     * @throws IOException
     */
    public static final void writeEntityDeclaration(String name, String text,
            String notation, Writer writer) throws IOException {

        writer.write("<!ENTITY ");
        writer.write(name);

        XMLWriterUtils.writeEncodedQuotedValue(text, writer);

        // write optional notation
        if (notation != null) {

            writer.write(" NDATA");
            writer.write(notation);

        }

        writer.write(">");

    }

    /**
     * Writes a {@link NotationDeclaration} to the stream.
     * 
     * @param declaration The {@link NotationDeclaration} to write.
     * @param writer The destination stream.
     * @throws IOException If an error occurs writing to the stream.
     */
    public static final void writeNotationDeclaration(
            NotationDeclaration declaration, Writer writer) throws IOException {

        String name = declaration.getName();
        String publicId = declaration.getPublicId();
        String systemId = declaration.getSystemId();

        writeNotationDeclaration(name, publicId, systemId, writer);

    }

    /**
     * Writes a notation declaration to the stream. Either or both of the
     * <code>publicId</code> and <code>systemId</code> parameters must be
     * non-<code>null</code>.
     * 
     * @param name The notation name.
     * @param publicId The entity public ID, or <code>null</code>.
     * @param systemId The entity system ID, or <code>null</code>.
     * @param writer The destination stream.
     * @throws IOException If an error occurs writing to the stream.
     */
    public static final void writeNotationDeclaration(String name, String publicId,
            String systemId, Writer writer) throws IOException {

        writer.write("<!NOTATION ");
        writer.write(name);

        if (publicId != null) {

            // write public/system ids
            writer.write("PUBLIC ");
            XMLWriterUtils.writeQuotedValue(publicId, writer);
            if (systemId != null) {

                writer.write(" ");
                XMLWriterUtils.writeQuotedValue(systemId, writer);

            }

        } else {

            // write system identifier
            writer.write("SYSTEM ");
            XMLWriterUtils.writeQuotedValue(systemId, writer);

        }

        writer.write(">");

    }

    /**
     * Writes a {@link ProcessingInstruction} to the provided stream.
     * 
     * @param procInst The {@link ProcessingInstruction} to write.
     * @param writer The destination stream.
     * @throws XMLStreamException If an error occurs writing to the stream.
     */
    public static final void writeProcessingInstruction(
            ProcessingInstruction procInst, Writer writer) throws IOException {

        writeProcessingInstruction(procInst.getTarget(), procInst.getData(),
                writer);

    }

    /**
     * Writes a {@link ProcessingInstruction} to the provided stream.
     * 
     * @param target The instruction target.
     * @param data The instruction data, or <code>null</code>.
     * @param writer The destination stream.
     * @throws XMLStreamException If an error occurs writing to the stream.
     */
    public static final void writeProcessingInstruction(String target,
            String data, Writer writer) throws IOException {

        writer.write("<?");
        writer.write(target);

        if (data != null) {

            writer.write(' ');
            writer.write(data);

        }

        writer.write("?>");

    }

    /**
     * Writes a {@link DTD} to the provided stream.
     * 
     * @param dtd The {@link DTD} to write.
     * @param writer The destination stream.
     * @throws IOException If an error occurs writing to the stream.
     */
    public static final void writeDTD(DTD dtd, Writer writer)
            throws IOException {

        writeDTD(dtd, writer);

    }

    /**
     * Writes a dtd to the provided stream.
     * 
     * @param dtd The dtd to write.
     * @param writer The destination stream.
     * @throws IOException If an error occurs writing to the stream.
     */
    public static final void writeDTD(String dtd, Writer writer)
            throws IOException {

        writer.write(dtd);

    }

    /**
     * Writes the given event to the provided {@link XMLStreamWriter}. This method
     * will delegate the actual task of writing to the stream to the appropriate
     * overloaded method.
     * 
     * @param event The event to write.
     * @param writer The destination XML stream.
     * @throws XMLStreamException If an error occurs writing the event.
     */
    public static final void writeEvent(XMLEvent event, XMLStreamWriter writer)
            throws XMLStreamException {

        int eventType = event.getEventType();
        switch (eventType) {

            case XMLEvent.START_ELEMENT :
                writeStartElement(event.asStartElement(), false, writer);
                break;

            case XMLEvent.END_ELEMENT :
                writeEndElement(event.asEndElement(), writer);
                break;

            case XMLEvent.CHARACTERS :
            case XMLEvent.SPACE :
            case XMLEvent.CDATA :
                writeCharacters(event.asCharacters(), writer);
                break;

            case XMLEvent.COMMENT :
                writeComment((Comment) event, writer);
                break;

            case XMLEvent.ENTITY_REFERENCE :
                writeEntityReference((EntityReference) event, writer);
                break;

            case XMLEvent.PROCESSING_INSTRUCTION :
                writeProcessingInstruction((ProcessingInstruction) event,
                        writer);
                break;

            case XMLEvent.DTD :
                writeDTD((DTD) event, writer);
                break;

            case XMLEvent.START_DOCUMENT :
                writeStartDocument((StartDocument) event, writer);
                break;

            case XMLEvent.END_DOCUMENT :
                writeEndDocument((EndDocument) event, writer);
                break;

            case XMLEvent.NAMESPACE :
                writeNamespace((Namespace) event, writer);
                break;

            case XMLEvent.ATTRIBUTE :
                writeAttribute((Attribute) event, writer);
                break;

            default :
                throw new XMLStreamException("Unrecognized event ("
                        + XMLStreamUtils.getEventTypeName(eventType) + "): "
                        + event);

        }

    }

    /**
     * Writes a {@link StartElement} event to the provided stream.
     * 
     * @param start The {@link StartElement} event to write.
     * @param empty Whether the element is empty.
     * @param writer The destination stream.
     * @throws XMLStreamException If an error occurs writing to the stream.
     */
    public static final void writeStartElement(StartElement start,
            boolean empty, XMLStreamWriter writer) throws XMLStreamException {

        QName name = start.getName();
        String nsURI = name.getNamespaceURI();
        String localName = name.getLocalPart();
        String prefix = name.getPrefix();

        if (prefix != null && prefix.length() > 0) {

            if (empty) {

                writer.writeEmptyElement(prefix, localName, nsURI);

            } else {

                writer.writeStartElement(prefix, localName, nsURI);

            }

        } else if (nsURI != null && nsURI.length() > 0) {

            if (empty) {

                writer.writeEmptyElement(nsURI, localName);

            } else {

                writer.writeStartElement(nsURI, localName);

            }

        } else {

            if (empty) {

                writer.writeEmptyElement(localName);

            } else {

                writer.writeStartElement(localName);

            }

        }

        // send namespaces first
        Iterator nsIter = start.getNamespaces();
        while (nsIter.hasNext()) {

            Namespace ns = (Namespace) nsIter.next();
            writeNamespace(ns, writer);

        }

        // write attributes
        Iterator attrIter = start.getAttributes();
        while (attrIter.hasNext()) {

            Attribute attr = (Attribute) attrIter.next();
            writeAttribute(attr, writer);

        }

    }

    /**
     * Writes an {@link EndElement} to the provided stream.
     * 
     * @param end The {@link EndElement} to write.
     * @param writer The destination stream.
     * @throws XMLStreamException If an error occurs writing to the stream.
     */
    public static final void writeEndElement(EndElement end,
            XMLStreamWriter writer) throws XMLStreamException {

        writer.writeEndElement();

    }

    /**
     * Writes an {@link Attribute} to the provided stream.
     * 
     * @param attr The {@link Attribute} to write.
     * @param writer The destination stream.
     * @throws XMLStreamException If an error occurs writing to the stream.
     */
    public static final void writeAttribute(Attribute attr,
            XMLStreamWriter writer) throws XMLStreamException {

        QName name = attr.getName();
        String nsURI = name.getNamespaceURI();
        String localName = name.getLocalPart();
        String prefix = name.getPrefix();
        String value = attr.getValue();

        if (prefix != null) {

            writer.writeAttribute(prefix, nsURI, localName, value);

        } else if (nsURI != null) {

            writer.writeAttribute(nsURI, localName, value);

        } else {

            writer.writeAttribute(localName, value);

        }

    }

    /**
     * Writes a {@link Namespace} to the provided stream.
     * 
     * @param ns The {@link Namespace} to write.
     * @param writer The destination stream.
     * @throws XMLStreamException If an error occurs writing to the stream.
     */
    public static final void writeNamespace(Namespace ns, XMLStreamWriter writer)
            throws XMLStreamException {

        if (ns.isDefaultNamespaceDeclaration()) {

            writer.writeDefaultNamespace(ns.getNamespaceURI());

        } else {

            writer.writeNamespace(ns.getPrefix(), ns.getNamespaceURI());

        }

    }

    /**
     * Writes a {@link StartDocument} to the provided stream.
     * 
     * @param start The {@link StartDocument} to write.
     * @param writer The destination stream.
     * @throws XMLStreamException If an error occurs writing to the stream.
     */
    public static final void writeStartDocument(StartDocument start,
            XMLStreamWriter writer) throws XMLStreamException {

        String version = start.getVersion();
        if (start.encodingSet()) {

            String encoding = start.getCharacterEncodingScheme();
            writer.writeStartDocument(encoding, version);

        } else {

            writer.writeStartDocument(version);

        }

    }

    /**
     * Writes an {@link EndDocument} to the provided stream.
     * 
     * @param end The {@link EndDocument} to write.
     * @param writer The destination stream.
     * @throws XMLStreamException If an error occurs writing to the stream.
     */
    public static final void writeEndDocument(EndDocument end,
            XMLStreamWriter writer) throws XMLStreamException {

        writer.writeEndDocument();

    }

    /**
     * Writes a {@link Characters} to the provided stream.
     * 
     * @param chars The {@link Characters} to write.
     * @param writer The destination stream.
     * @throws XMLStreamException If an error occurs writing to the stream.
     */
    public static final void writeCharacters(Characters chars,
            XMLStreamWriter writer) throws XMLStreamException {

        if (chars.isCData()) {

            writer.writeCData(chars.getData());

        } else {

            writer.writeCharacters(chars.getData());

        }

    }

    /**
     * Writes a {@link Comment} to the provided stream.
     * 
     * @param comment The {@link Comment} to write.
     * @param writer The destination stream.
     * @throws XMLStreamException If an error occurs writing to the stream.
     */
    public static final void writeComment(Comment comment,
            XMLStreamWriter writer) throws XMLStreamException {

        writer.writeComment(comment.getText());

    }

    /**
     * Writes an {@link EntityReference} to the provided stream.
     * 
     * @param entityRef The {@link EntityReference} to write.
     * @param writer The destination stream.
     * @throws XMLStreamException If an error occurs writing to the stream.
     */
    public static final void writeEntityReference(EntityReference entityRef,
            XMLStreamWriter writer) throws XMLStreamException {

        writer.writeEntityRef(entityRef.getName());

    }

    /**
     * Writes a {@link ProcessingInstruction} to the provided stream.
     * 
     * @param procInst The {@link ProcessingInstruction} to write.
     * @param writer The destination stream.
     * @throws XMLStreamException If an error occurs writing to the stream.
     */
    public static final void writeProcessingInstruction(
            ProcessingInstruction procInst, XMLStreamWriter writer)
            throws XMLStreamException {

        String data = procInst.getData();
        if (data != null) {

            writer.writeProcessingInstruction(procInst.getTarget(), data);

        } else {

            writer.writeProcessingInstruction(procInst.getTarget());

        }

    }

    /**
     * Writes a {@link DTD} to the provided stream.
     * 
     * @param dtd The {@link DTD} to write.
     * @param writer The destination stream.
     * @throws XMLStreamException If an error occurs writing to the stream.
     */
    public static final void writeDTD(DTD dtd, XMLStreamWriter writer)
            throws XMLStreamException {

        writer.writeDTD(dtd.getDocumentTypeDeclaration());

    }

}