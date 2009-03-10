/*
 * $Id: AbstractCharactersEvent.java,v 1.3 2004/07/15 02:11:02 cniles Exp $
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
 * Abstract base class for various {@link Characters} implementations.
 * 
 * @author Christian Niles
 * @version $Revision: 1.3 $
 */
public abstract class AbstractCharactersEvent extends AbstractXMLEvent
        implements
            Characters {

    /** Character content. */
    protected String data;

    public AbstractCharactersEvent(String data) {

        this.data = data;

    }

    public AbstractCharactersEvent(String data, Location location) {

        super(location);
        this.data = data;

    }

    public AbstractCharactersEvent(String data, Location location,
            QName schemaType) {

        super(location, schemaType);
        this.data = data;

    }

    public AbstractCharactersEvent(Characters that) {

        super(that);
        this.data = that.getData();

    }

    public String getData() {

        return data;

    }
    
    public boolean isCharacters() {
        
        return true;
        
    }

    public boolean isWhiteSpace() {

        String data = getData();
        for (int i = 0, s = data.length(); i < s; i++) {

            switch (data.charAt(i)) {
                
                case ' ':
                case '\n':
                case '\t':
                case '\r':
                    continue;
                default:
                    return false;
                
            }

        }

        return true;

    }

}