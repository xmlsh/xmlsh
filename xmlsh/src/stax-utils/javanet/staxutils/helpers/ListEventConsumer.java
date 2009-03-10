/*
 * $Id: ListEventConsumer.java,v 1.1 2004/07/14 22:58:58 cniles Exp $
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

import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.util.XMLEventConsumer;

/**
 * {@link XMLEventConsumer} that stores all added events in a {@link List}.
 * 
 * @author Christian Niles
 * @version $Revision: 1.1 $
 */
public class ListEventConsumer implements XMLEventConsumer {

    /** The list in which to store events. */
    private List events;

    public ListEventConsumer() {

    }

    /**
     * Constructs an instance that adds events to the provided list.
     * 
     * @param events The list to which events will be added, or <code>null</code>.
     */
    public ListEventConsumer(List events) {

        this.events = events;

    }

    /**
     * Adds the event to the internal list.
     */
    public void add(XMLEvent event) throws XMLStreamException {

        if (events == null) {

            events = new ArrayList();

        }

        events.add(event);

    }

    /**
     * Returns the {@link List} of events added to this consumer.
     * 
     * @return The {@link List} of events added to this consumer.
     */
    public List getEvents() {

        return events;

    }

    /**
     * Sets the {@link List} to which events will be written.
     * 
     * @param events The {@link List} to which events will be written.
     */
    public void setEvents(List events) {

        this.events = events;

    }

    /**
     * Removes all events from the internal list, making it available for reuse.
     */
    public void reset() {

        if (events != null) {

            events.clear();

        }

    }

}