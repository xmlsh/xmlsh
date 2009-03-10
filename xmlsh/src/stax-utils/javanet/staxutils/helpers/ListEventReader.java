/*
 * $Id: ListEventReader.java,v 1.1 2004/07/14 22:58:58 cniles Exp $
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
package javanet.staxutils.helpers;

import java.util.List;
import java.util.NoSuchElementException;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import javanet.staxutils.BaseXMLEventReader;

/**
 * {@link XMLEventReader} that reads events from a {@link List}.
 * 
 * @author Christian Niles
 * @version $Revision: 1.1 $
 */
public class ListEventReader extends BaseXMLEventReader {

    /** The index of the next event. */
    private int nextEvent = 0;

    /** The {@link List} from which events are read. */
    private List events;

    /**
     * Constructs a <code>ListEventReader</code> that reads events from the
     * provided {@link List}.
     * 
     * @param events The {@link List} of events to read.
     */
    public ListEventReader(List events) {

        this.events = events;

    }

    public XMLEvent nextEvent() throws XMLStreamException {

        if (hasNext()) {

            XMLEvent event = (XMLEvent) events.get(nextEvent);
            nextEvent++;
            return event;

        } else {

            throw new NoSuchElementException("End of stream reached");

        }

    }

    public boolean hasNext() {

        return (nextEvent < events.size());

    }

    public XMLEvent peek() throws XMLStreamException {

        if (hasNext()) {

            return (XMLEvent) events.get(nextEvent);

        } else {

            return null;

        }

    }

}