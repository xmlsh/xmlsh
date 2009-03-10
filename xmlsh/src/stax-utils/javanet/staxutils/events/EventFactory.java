/*
 * $Id: EventFactory.java,v 1.1 2004/07/05 23:09:59 cniles Exp $
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
 * Factory for events within this package.
 * 
 * @author Christian Niles
 * @version $Revision: 1.1 $
 */
public class EventFactory extends BaseXMLEventFactory {

    public Attribute createAttribute(QName name, String value,
            Location location, QName schemaType) {

        return new AttributeEvent(name, value, location, schemaType);

    }

    public Characters createCData(String content, Location location,
            QName schemaType) {

        return new CDataEvent(content, location, schemaType);

    }

    public Characters createCharacters(String content, Location location,
            QName schemaType) {

        return new CharactersEvent(content, location, schemaType);

    }

    public Comment createComment(String text, Location location) {

        return new CommentEvent(text, location);

    }

    public DTD createDTD(String dtd, Location location) {

        return new DTDEvent(dtd, location);

    }

    public EndDocument createEndDocument(Location location) {

        return new EndDocumentEvent(location);

    }

    public EndElement createEndElement(QName name, Iterator namespaces,
            Location location, QName schemaType) {

        return new EndElementEvent(name, namespaces, location, schemaType);

    }

    public EntityReference createEntityReference(String name,
            EntityDeclaration declaration, Location location) {

        return new EntityReferenceEvent(name, declaration, location);

    }

    public Characters createIgnorableSpace(String content, Location location) {

        return new IgnorableSpaceEvent(content, location);

    }

    public Namespace createNamespace(String prefix, String namespaceUri,
            Location location) {

        return new NamespaceEvent(prefix, namespaceUri, location);

    }

    public ProcessingInstruction createProcessingInstruction(String target,
            String data, Location location) {

        return new ProcessingInstructionEvent(target, data, location);

    }

    public Characters createSpace(String content, Location location) {

        return new IgnorableSpaceEvent(content, location);

    }

    public StartDocument createStartDocument(String encoding, String version,
            Boolean standalone, Location location, QName schemaType) {

        return new StartDocumentEvent(encoding, standalone, version, location,
                schemaType);

    }

    public StartElement createStartElement(QName name, Iterator attributes,
            Iterator namespaces, NamespaceContext namespaceCtx,
            Location location, QName schemaType) {

        return new StartElementEvent(name, attributes, namespaces,
                namespaceCtx, location, schemaType);

    }
}