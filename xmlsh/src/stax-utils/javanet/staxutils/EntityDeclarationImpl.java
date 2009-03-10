/* $Id: EntityDeclarationImpl.java,v 1.1 2004/06/21 18:59:45 ryan_shoemaker Exp $
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

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EntityDeclaration;
import java.io.IOException;
import java.io.Writer;

/**
 * Implementation of {@link javax.xml.stream.events.EntityDeclaration}.
 *
 * The only reason this class exists is because {@link javax.xml.stream.XMLEventFactory}
 * doesn't have a factory for this event type and the {@link ContentHandlerToXMLEventWriter}
 * needs to create one of these event types to handle skippedEntity events.
 *
 * @author Ryan.Shoemaker@Sun.COM
 * @version 1.0
 */
class EntityDeclarationImpl extends EventHelper implements EntityDeclaration {

    private final String entityName;
    private final String publicId;
    private final String systemId;
    private final String notationName;
    private final String replacementText;

    public EntityDeclarationImpl(Location location, String entityName, String publicId, String systemId, String notationName, String replacementText) {
        super(location);
        this.entityName = entityName;
        this.publicId = publicId;
        this.systemId = systemId;
        this.notationName = notationName;
        this.replacementText = replacementText;
    }

    public String getBaseURI() {
        // TODO: ??
        return null;
    }

    public String getName() {
        return entityName;
    }

    public String getNotationName() {
        return notationName;
    }

    public String getPublicId() {
        return publicId;
    }

    public String getReplacementText() {
        return replacementText;
    }

    public String getSystemId() {
        return systemId;
    }

    public int getEventType() {
        return ENTITY_DECLARATION;
    }

    public boolean isEntityReference() {
        return true;
    }

    public void writeAsEncodedUnicode(Writer w) throws XMLStreamException {
        try {
            w.write('&');
            w.write(entityName);
            w.write(';');
        } catch (IOException ie) {
            throw new XMLStreamException(ie);
        }
    }
}
