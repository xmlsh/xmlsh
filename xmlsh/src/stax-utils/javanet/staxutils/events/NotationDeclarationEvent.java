/*
 * $Id: NotationDeclarationEvent.java,v 1.2 2004/07/15 02:11:02 cniles Exp $
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
import javax.xml.stream.events.NotationDeclaration;

/**
 * {@link NotationDeclaration} implementation.
 * 
 * @author Christian Niles
 * @version $Revision: 1.2 $
 */
public class NotationDeclarationEvent extends AbstractXMLEvent
        implements
            NotationDeclaration {

    /** The notation name. */
    protected String name;

    /** The public id of the notation */
    protected String publicId;

    /** The system id of the notation */
    protected String systemId;

    public NotationDeclarationEvent(String name, String publicId,
            Location location) {

        super(location);
        this.name = name;
        this.publicId = publicId;

    }

    public NotationDeclarationEvent(String name, String publicId,
            String systemId, Location location) {

        super(location);
        this.name = name;
        this.publicId = publicId;
        this.systemId = systemId;

    }

    public NotationDeclarationEvent(NotationDeclaration that) {

        super(that);
        this.name = that.getName();
        this.publicId = that.getPublicId();
        this.systemId = that.getSystemId();

    }

    /**
     * Returns {@link #NOTATION_DECLARATION}.
     */
    public int getEventType() {

        return NOTATION_DECLARATION;

    }

    public String getName() {

        return this.name;

    }

    public String getPublicId() {

        return this.publicId;

    }

    public String getSystemId() {

        return this.systemId;

    }
    
}