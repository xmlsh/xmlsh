/*
 * $Id: XMLStreamEventReader.java,v 1.3 2004/07/12 15:38:06 cniles Exp $
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
package javanet.staxutils;

import javanet.staxutils.events.EventAllocator;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.util.XMLEventAllocator;

/**
 * {@link XMLEventReader} implementation based on a {@link XMLStreamReader} and
 * an {@link XMLEventAllocator}.
 * 
 * @author Christian Niles
 * @version $Revision: 1.3 $
 */
public class XMLStreamEventReader implements XMLEventReader {

    /** The underlying stream reader. */
    private XMLStreamReader reader;

    /** The event allocator used to create events. */
    private XMLEventAllocator allocator;

    /** Field used to cache peek()ed events. */
    private XMLEvent nextEvent;

    /** Whether the reader is closed or not. */
    private boolean closed;

    public XMLStreamEventReader(XMLStreamReader reader) {

        this.reader = reader;
        this.allocator = new EventAllocator();
    
    }
    
    public XMLStreamEventReader(XMLStreamReader reader,
            XMLEventAllocator allocator) {

        this.reader = reader;
        this.allocator = (allocator == null ? new EventAllocator() : allocator);
        
    }
    
    /**
     * No properties are supported, so this always throws
     * {@link IllegalArgumentException}.
     */
    public Object getProperty(String name) throws IllegalArgumentException {

        throw new IllegalArgumentException("Unknown property: " + name);

    }

    public synchronized boolean hasNext() {

        if (closed) {

            return false;

        }

        try {

            return reader.hasNext();

        } catch (XMLStreamException e) {

            // TODO how to handle inconsistency?
            return false;

        }

    }

    public synchronized XMLEvent nextTag() throws XMLStreamException {

        if (closed) {

            throw new XMLStreamException("Stream has been closed");

        }

        nextEvent = null;
        reader.nextTag();
        return nextEvent();

    }

    public synchronized String getElementText() throws XMLStreamException {

        if (closed) {

            throw new XMLStreamException("Stream has been closed");

        }

        // null the peeked event
        this.nextEvent = null;
        return reader.getElementText();

    }

    public synchronized XMLEvent nextEvent() throws XMLStreamException {

        if (closed) {

            throw new XMLStreamException("Stream has been closed");

        }

        XMLEvent event;
        if (nextEvent != null) {

            event = nextEvent;
            nextEvent = null;

        } else {

            event = allocateEvent();
            reader.next();

        }

        return event;

    }

    public synchronized XMLEvent peek() throws XMLStreamException {

        if (closed) {

            throw new XMLStreamException("Stream has been closed");

        }

        if (nextEvent == null) {

            nextEvent = allocateEvent();
            reader.next();

        }

        return nextEvent;

    }

    public Object next() {

        try {

            return nextEvent();

        } catch (XMLStreamException e) {

            // TODO throw a more descriptive exception?
            throw new RuntimeException(e);

        }

    }

    public void remove() {

        throw new UnsupportedOperationException();

    }

    public synchronized void close() throws XMLStreamException {

        if (!closed) {

            reader.close();
            closed = true;
            nextEvent = null;
            reader = null;
            allocator = null;

        }

    }

    /**
     * Reads the next event from the underlying reader.
     * 
     * @return The allocated {@link XMLEvent}.
     * @throws XMLStreamException If an error occurs reading the underlying stream.
     */
    protected XMLEvent allocateEvent() throws XMLStreamException {

        return allocator.allocate(reader);

    }

}