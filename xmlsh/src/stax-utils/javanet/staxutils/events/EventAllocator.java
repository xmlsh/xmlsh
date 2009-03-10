/*
 * $Id: EventAllocator.java,v 1.1 2004/07/05 23:10:00 cniles Exp $
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
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
import javax.xml.stream.util.XMLEventAllocator;

/**
 * Allocates events within this package.
 * 
 * @author Christian Niles
 * @version $Revision: 1.1 $
 */
public class EventAllocator extends BaseXMLEventAllocator {

    public XMLEventAllocator newInstance() {

        return new EventAllocator();

    }

    public StartElement allocateStartElement(XMLStreamReader reader)
            throws XMLStreamException {

        Location location = createStableLocation(reader);
        QName name = reader.getName();
        List attributes = allocateAttributes(location, reader);
        List namespaces = allocateNamespaces(location, reader);
        NamespaceContext nsCtx = createStableNamespaceContext(reader);
        QName schemaType = determineSchemaType(reader);

        return new StartElementEvent(name, attributes.iterator(),
                namespaces.iterator(), nsCtx, location, schemaType);

    }

    public EndElement allocateEndElement(XMLStreamReader reader)
            throws XMLStreamException {

        Location location = createStableLocation(reader);
        QName name = reader.getName();
        List namespaces = allocateNamespaces(location, reader);
        QName schemaType = determineSchemaType(reader);

        return new EndElementEvent(name, namespaces.iterator(), location,
                schemaType);

    }

    public List allocateAttributes(Location location, XMLStreamReader reader)
            throws XMLStreamException {

        List attributes = null;
        for (int i = 0, s = reader.getAttributeCount(); i < s; i++) {

            QName name = reader.getAttributeName(i);
            String value = reader.getAttributeValue(i);
            String dtdType = reader.getAttributeType(i);
            boolean specified = reader.isAttributeSpecified(i);
            QName schemaType = determineAttributeSchemaType(reader, i);

            Attribute attr = new AttributeEvent(name, value, specified,
                    dtdType, location, schemaType);
            if (attributes == null) {

                attributes = new ArrayList();

            }
            attributes.add(attr);

        }

        return (attributes != null) ? attributes : Collections.EMPTY_LIST;

    }

    public List allocateNamespaces(Location location, XMLStreamReader reader)
            throws XMLStreamException {

        List namespaces = null;
        for (int i = 0, s = reader.getNamespaceCount(); i < s; i++) {

            String prefix = reader.getNamespacePrefix(i);
            String nsURI = reader.getNamespaceURI(i);

            Namespace ns = new NamespaceEvent(prefix, nsURI, location);
            if (namespaces == null) {

                namespaces = new ArrayList();

            }
            namespaces.add(ns);

        }

        return (namespaces != null) ? namespaces : Collections.EMPTY_LIST;

    }

    public Characters allocateCData(XMLStreamReader reader)
            throws XMLStreamException {

        Location location = createStableLocation(reader);
        String text = reader.getText();
        QName schemaType = determineSchemaType(reader);
        return new CDataEvent(text, location, schemaType);

    }

    public Characters allocateCharacters(XMLStreamReader reader)
            throws XMLStreamException {

        Location location = createStableLocation(reader);
        String text = reader.getText();
        QName schemaType = determineSchemaType(reader);
        return new CharactersEvent(text, location, schemaType);

    }

    public Characters allocateIgnorableSpace(XMLStreamReader reader)
            throws XMLStreamException {

        Location location = createStableLocation(reader);
        String text = reader.getText();
        QName schemaType = determineSchemaType(reader);
        return new IgnorableSpaceEvent(text, location, schemaType);

    }

    public Comment allocateComment(XMLStreamReader reader)
            throws XMLStreamException {

        Location location = createStableLocation(reader);
        String text = reader.getText();
        return new CommentEvent(text, location);

    }

    public DTD allocateDTD(XMLStreamReader reader) throws XMLStreamException {

        Location location = createStableLocation(reader);
        List entities = (List) reader.getProperty("javax.xml.stream.entities");
        List notations = (List) reader.getProperty("javax.xml.stream.notations");
        String text = reader.getText();

        return new DTDEvent(text, entities, notations, location);

    }

    public StartDocument allocateStartDocument(XMLStreamReader reader)
            throws XMLStreamException {

        Location location = createStableLocation(reader);
        String encoding = reader.getCharacterEncodingScheme();
        String version = reader.getVersion();
        Boolean standalone = reader.standaloneSet()
                ? Boolean.valueOf(reader.isStandalone())
                : null;
        QName schemaType = determineSchemaType(reader);

        return new StartDocumentEvent(encoding, standalone, version, location,
                schemaType);

    }

    public EndDocument allocateEndDocument(XMLStreamReader reader)
            throws XMLStreamException {

        Location location = createStableLocation(reader);
        QName schemaType = determineSchemaType(reader);
        return new EndDocumentEvent(location, schemaType);

    }

    public EntityReference allocateEntityReference(XMLStreamReader reader)
            throws XMLStreamException {

        Location location = createStableLocation(reader);
        String name = reader.getLocalName();
        EntityDeclaration decl = determineEntityDeclaration(name, reader);

        return new EntityReferenceEvent(name, decl, location);

    }

    public ProcessingInstruction allocateProcessingInstruction(
            XMLStreamReader reader) throws XMLStreamException {

        Location location = createStableLocation(reader);
        String target = reader.getPITarget();
        String data = reader.getPIData();

        return new ProcessingInstructionEvent(target, data, location);

    }

    public QName determineSchemaType(XMLStreamReader reader) {

        // TODO look for xsi:type?
        return null;

    }

    public QName determineAttributeSchemaType(XMLStreamReader reader, int index) {

        return null;

    }

    public EntityDeclaration determineEntityDeclaration(String name,
            XMLStreamReader reader) {

        return new EntityDeclarationEvent(name, reader.getText(), null);

    }

    public Location createStableLocation(XMLStreamReader reader) {

        // FIXME assume location is already stable?
        return reader.getLocation();

    }

    public NamespaceContext createStableNamespaceContext(XMLStreamReader reader) {

        // FIXME assume context is already stable
        return reader.getNamespaceContext();

    }

}