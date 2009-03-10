/*
 * $Id: EntityDeclarationEvent.java,v 1.2 2004/07/15 02:11:01 cniles Exp $
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

import javax.xml.stream.Location;
import javax.xml.stream.events.EntityDeclaration;

/**
 * {@link EntityDeclaration} event implementation.
 * 
 * @author Christian Niles
 * @version $Revision: 1.2 $
 */
public class EntityDeclarationEvent extends AbstractXMLEvent
        implements
            EntityDeclaration {

    /** The entity name. */
    protected String name;

    /** 
     * The replacement text, or <code>null</code> if this isn't an internal entity.
     */
    protected String replacementText;

    /** The entity base URI, or <code>null</code> if this isn't an external entity */
    protected String baseURI;

    /** The public id, or <code>null</code> if this isn't an external entity */
    protected String publicId;

    /** The system id, or <code>null</code> if this isn't an external entity. */
    protected String systemId;

    /**
     * The optional notation name.
     */
    protected String notationName;

    public EntityDeclarationEvent(String name, String replacementText,
            Location location) {

        super(location);
        this.name = name;
        this.replacementText = replacementText;

    }

    public EntityDeclarationEvent(String name, String replacementText,
            String notationName, Location location) {

        super(location);
        this.name = name;
        this.replacementText = replacementText;
        this.notationName = notationName;

    }

    public EntityDeclarationEvent(String name, String publicId,
            String systemId, String baseURI, String notationName,
            Location location) {

        super(location);
        this.name = name;
        this.publicId = publicId;
        this.systemId = systemId;
        this.baseURI = baseURI;
        this.notationName = notationName;

    }

    public EntityDeclarationEvent(EntityDeclaration that) {

        super(that);
        this.name = that.getName();
        this.replacementText = that.getReplacementText();
        this.publicId = that.getPublicId();
        this.systemId = that.getSystemId();
        this.baseURI = that.getBaseURI();
        this.notationName = that.getNotationName();

    }

    /**
     * Returns {@link #ENTITY_DECLARATION}.
     */
    public int getEventType() {

        return ENTITY_DECLARATION;

    }

    public String getBaseURI() {

        return this.baseURI;

    }

    public String getName() {

        return this.name;

    }

    public String getNotationName() {

        return this.notationName;

    }

    public String getPublicId() {

        return this.publicId;

    }

    public String getReplacementText() {

        return this.replacementText;

    }

    public String getSystemId() {

        return this.systemId;

    }

}