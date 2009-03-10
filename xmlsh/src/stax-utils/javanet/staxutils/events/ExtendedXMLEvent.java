/*
 * $Id: ExtendedXMLEvent.java,v 1.1 2004/07/05 23:08:10 cniles Exp $
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

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.XMLEvent;

/**
 * Extended {@link XMLEvent} interface that provides additional functionality.
 * 
 * @author Christian Niles
 * @version $Revision: 1.1 $
 */
public interface ExtendedXMLEvent extends XMLEvent {

    /**
     * Determines if this event matches another event, irrespective of document
     * location.
     * 
     * @param event The event to match against.
     * @return <code>true</code> if the two events match, <code>false</code>
     * 		otherwise.
     */
    public boolean matches(XMLEvent event);

    /**
     * Writes the event to the provided {@link XMLStreamWriter}.
     * 
     * @param writer The destination stream.
     * @throws XMLStreamException If an error occurs writing to the destination
     * 		stream.
     */
    public void writeEvent(XMLStreamWriter writer) throws XMLStreamException;
        
}