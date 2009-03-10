/*
 * $Id: AbstractXMLEvent.java,v 1.3 2004/07/15 02:11:02 cniles Exp $
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

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import javanet.staxutils.helpers.EventMatcher;
import javanet.staxutils.helpers.UnknownLocation;
import javanet.staxutils.io.XMLWriterUtils;

/**
 * Abstract base class for {@link XMLEvent} implementations.
 * 
 * @author Christian Niles
 * @version $Revision: 1.3 $
 */
public abstract class AbstractXMLEvent
        implements
            ExtendedXMLEvent,
            Serializable,
            Cloneable {

    /** The event location. */
    protected Location location;

    /** The schema type. */
    protected QName schemaType;

    public AbstractXMLEvent() {

    }

    public AbstractXMLEvent(Location location) {

        this.location = location;

    }

    public AbstractXMLEvent(Location location, QName schemaType) {

        this.location = location;
        this.schemaType = schemaType;

    }

    public AbstractXMLEvent(XMLEvent that) {

        this.location = that.getLocation();
        this.schemaType = that.getSchemaType();

    }

    public Location getLocation() {

        return (location == null ? UnknownLocation.INSTANCE : location);

    }

    public QName getSchemaType() {

        return schemaType;

    }

    public Characters asCharacters() {

        return (Characters) this;

    }

    public EndElement asEndElement() {

        return (EndElement) this;

    }

    public StartElement asStartElement() {

        return (StartElement) this;

    }

    public boolean isAttribute() {

        return getEventType() == ATTRIBUTE;

    }

    public boolean isCharacters() {

        switch (getEventType()) {

            case CHARACTERS :
            case SPACE :
            case CDATA :
                return true;

            default :
                return false;

        }

    }

    public boolean isEndDocument() {

        return getEventType() == END_DOCUMENT;

    }

    public boolean isEndElement() {

        return getEventType() == END_ELEMENT;

    }

    public boolean isEntityReference() {

        return getEventType() == ENTITY_REFERENCE;

    }

    public boolean isNamespace() {

        return getEventType() == NAMESPACE;

    }

    public boolean isProcessingInstruction() {

        return getEventType() == PROCESSING_INSTRUCTION;

    }

    public boolean isStartDocument() {

        return getEventType() == START_DOCUMENT;

    }

    public boolean isStartElement() {

        return getEventType() == START_ELEMENT;

    }

    public Object clone() {

        try {

            return super.clone();

        } catch (CloneNotSupportedException e) {

            // should never happen since we implement Cloneable
            throw new RuntimeException("Unexpected exception cloning XMLEvent",
                    e);

        }

    }

    public boolean matches(XMLEvent event) {

        return EventMatcher.eventsMatch(this, event);

    }

    public void writeEvent(XMLStreamWriter writer) throws XMLStreamException {

        XMLWriterUtils.writeEvent(this, writer);

    }

    public void writeAsEncodedUnicode(Writer writer) throws XMLStreamException {

        try {
        
            XMLWriterUtils.writeEvent(this, writer);
        
        } catch (IOException e) {
        
            throw new XMLStreamException(e);
        
        }

    }

    public String toString() {

        StringWriter writer = new StringWriter();
        try {

            this.writeAsEncodedUnicode(writer);

        } catch (XMLStreamException e) {
            // shouldn't happen?
        }

        return writer.toString();

    }

}