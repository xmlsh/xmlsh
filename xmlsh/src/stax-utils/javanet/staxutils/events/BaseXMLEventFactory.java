/*
 * $Id: BaseXMLEventFactory.java,v 1.1 2004/07/05 23:09:32 cniles Exp $
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
package javanet.staxutils.events;

import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.Comment;
import javax.xml.stream.events.DTD;
import javax.xml.stream.events.EndDocument;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.EntityDeclaration;
import javax.xml.stream.events.EntityReference;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.ProcessingInstruction;
import javax.xml.stream.events.StartDocument;
import javax.xml.stream.events.StartElement;

/**
 * Abstract base class for {@link XMLEventFactory} implementations. This class
 * makes it easier to implement by coalesing the various forms of each method into
 * a single creation method, such as
 * {@link #createAttribute(QName, String, Location, QName)}.
 * 
 * @author Christian Niles
 * @version $Revision: 1.1 $
 */
public abstract class BaseXMLEventFactory extends XMLEventFactory {

    /** The current location registered with the factory. */
    protected Location location;

    public void setLocation(Location location) {

        this.location = location;

    }

    public Attribute createAttribute(QName name, String value) {

        return createAttribute(name, value, location, null);

    }

    public Attribute createAttribute(String prefix, String namespaceURI,
            String localName, String value) {

        return createAttribute(new QName(namespaceURI, localName, prefix),
                value, location, null);

    }

    public Attribute createAttribute(String localName, String value) {

        return createAttribute(new QName(localName), value, location, null);

    }

    public abstract Attribute createAttribute(QName name, String value,
            Location location, QName schemaType);

    public Characters createCData(String content) {

        return createCData(content, location, null);

    }

    public abstract Characters createCData(String content, Location location,
            QName schemaType);

    public Characters createCharacters(String content) {

        return createCharacters(content, location, null);

    }

    public abstract Characters createCharacters(String content,
            Location location, QName schemaType);

    public Comment createComment(String text) {

        return createComment(text, location);

    }

    public abstract Comment createComment(String text, Location location);

    public DTD createDTD(String dtd) {

        return createDTD(dtd, location);

    }

    public abstract DTD createDTD(String dtd, Location location);

    public EndDocument createEndDocument() {

        return createEndDocument(location);

    }

    public abstract EndDocument createEndDocument(Location location);

    public EndElement createEndElement(QName name, Iterator namespaces) {

        return createEndElement(name, namespaces, location, null);

    }

    public EndElement createEndElement(String prefix, String namespaceUri,
            String localName, Iterator namespaces) {

        return createEndElement(new QName(namespaceUri, localName, prefix),
                namespaces, location, null);

    }

    public EndElement createEndElement(String prefix, String namespaceUri,
            String localName) {

        return createEndElement(new QName(namespaceUri, localName, prefix),
                null, location, null);

    }

    public abstract EndElement createEndElement(QName name,
            Iterator namespaces, Location location, QName schemaType);

    public EntityReference createEntityReference(String name,
            EntityDeclaration declaration) {

        return createEntityReference(name, declaration, location);

    }

    public abstract EntityReference createEntityReference(String name,
            EntityDeclaration declaration, Location location);

    public Characters createIgnorableSpace(String content) {

        return createIgnorableSpace(content, location);

    }

    public abstract Characters createIgnorableSpace(String content,
            Location location);

    public Namespace createNamespace(String prefix, String namespaceUri) {

        return createNamespace(prefix, namespaceUri, location);

    }

    public Namespace createNamespace(String namespaceUri) {

        return createNamespace("", namespaceUri, location);

    }

    public abstract Namespace createNamespace(String prefix,
            String namespaceUri, Location location);

    public ProcessingInstruction createProcessingInstruction(String target,
            String data) {

        return createProcessingInstruction(target, data, location);

    }

    public abstract ProcessingInstruction createProcessingInstruction(
            String target, String data, Location location);

    public Characters createSpace(String content) {

        return createSpace(content, location);

    }

    public abstract Characters createSpace(String content, Location location);

    public StartDocument createStartDocument() {

        return createStartDocument(null, null, null, location, null);

    }

    public StartDocument createStartDocument(String encoding, String version,
            boolean standalone) {

        return createStartDocument(encoding, version,
                Boolean.valueOf(standalone), location, null);

    }

    public StartDocument createStartDocument(String encoding, String version) {

        return createStartDocument(encoding, version, null, location, null);

    }

    public StartDocument createStartDocument(String encoding) {

        return createStartDocument(encoding, null, null, location, null);

    }

    public abstract StartDocument createStartDocument(String encoding,
            String version, Boolean standalone, Location location,
            QName schemaType);

    public StartElement createStartElement(QName name, Iterator attributes,
            Iterator namespaces) {

        return createStartElement(name, attributes, namespaces, null, location,
                null);

    }

    public StartElement createStartElement(String prefix, String namespaceUri,
            String localName, Iterator attributes, Iterator namespaces,
            NamespaceContext context) {

        return createStartElement(new QName(namespaceUri, localName, prefix),
                attributes, namespaces, context, location, null);

    }

    public StartElement createStartElement(String prefix, String namespaceUri,
            String localName, Iterator attributes, Iterator namespaces) {

        return createStartElement(new QName(namespaceUri, localName, prefix),
                attributes, namespaces, null, location, null);

    }

    public StartElement createStartElement(String prefix, String namespaceUri,
            String localName) {

        return createStartElement(new QName(namespaceUri, localName, prefix),
                null, null, null, location, null);

    }

    public abstract StartElement createStartElement(QName name,
            Iterator attributes, Iterator namespaces,
            NamespaceContext namespaceCtx, Location location, QName schemaType);

}