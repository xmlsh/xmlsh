/*
 * $Id: CharactersEvent.java,v 1.1 2004/07/05 23:09:56 cniles Exp $
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

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.events.Characters;

/**
 * Non-ignorable <code>Characters</code> event implementation.
 *
 * @author Christian Niles
 * @version $Revision: 1.1 $
 */
public class CharactersEvent extends AbstractCharactersEvent {

    public CharactersEvent(String data) {

        super(data);

    }

    public CharactersEvent(String data, Location location) {

        super(data, location);

    }

    public CharactersEvent(String data, Location location, QName schemaType) {

        super(data, location, schemaType);

    }

    public CharactersEvent(Characters that) {

        super(that);

    }

    public boolean isCData() {

        return false;

    }

    public boolean isIgnorableWhiteSpace() {

        return false;

    }

    /** Returns {@link #CHARACTERS}. */
    public int getEventType() {

        return CHARACTERS;

    }

}