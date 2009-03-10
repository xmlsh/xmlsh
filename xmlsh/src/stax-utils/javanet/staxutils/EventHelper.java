/* $Id: EventHelper.java,v 1.1 2004/06/21 18:59:46 ryan_shoemaker Exp $
 *
 * Copyright (c) 2004, Sun Microsystems, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *     * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *
 *     * Redistributions in binary form must reproduce the above
 *      copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 *
 *     * Neither the name of Sun Microsystems, Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package javanet.staxutils;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.Writer;

/**
 * Partial base class implementation of {@link javax.xml.stream.events.XMLEvent}.
 *
 * @author Ryan.Shoemaker@Sun.COM
 * @version 1.0
 */
abstract class EventHelper implements XMLEvent {
    
    private final Location location;

    protected EventHelper(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }

    public boolean isStartElement() {
        return false;
    }

    public boolean isAttribute() {
        return false;
    }

    public boolean isNamespace() {
        return false;
    }

    public boolean isEndElement() {
        return false;
    }

    public boolean isEntityReference() {
        return false;
    }

    public boolean isProcessingInstruction() {
        return false;
    }

    public boolean isCharacters() {
        return false;
    }

    public boolean isStartDocument() {
        return false;
    }

    public boolean isEndDocument() {
        return false;
    }

    public StartElement asStartElement() {
        throw new UnsupportedOperationException();
    }

    public EndElement asEndElement() {
        throw new UnsupportedOperationException();
    }

    public Characters asCharacters() {
        throw new UnsupportedOperationException();
    }

    public QName getSchemaType() {
        throw new UnsupportedOperationException();
    }

    public abstract void writeAsEncodedUnicode(Writer writer) throws XMLStreamException;

    public int getEventType() {
        throw new UnsupportedOperationException();
    }
}
