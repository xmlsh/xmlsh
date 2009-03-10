/*
 * $Id: XMLStreamEventWriter.java,v 1.1 2004/07/05 23:16:16 cniles Exp $
 * 
 * Copyright (c) 2004, Christian Niles, Unit12
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

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import javanet.staxutils.events.ExtendedXMLEvent;
import javanet.staxutils.io.XMLWriterUtils;

/**
 * {@link javax.xml.stream.XMLEventWriter} that writes events to a
 * {@link XMLStreamWriter}.
 * 
 * @author Christian Niles
 * @version $Revision: 1.1 $
 */
public class XMLStreamEventWriter extends BaseXMLEventWriter {

    /** The underlying stream. */
    private XMLStreamWriter writer;

    /**
     * Constructs a <code>XMLEventStreamWriter</code> that writes events to the
     * given stream.
     * 
     * @param writer The {@link XMLStreamWriter} to which the events will be
     * 		written.
     */
    public XMLStreamEventWriter(XMLStreamWriter writer) {

        super(null, writer.getNamespaceContext());
        this.writer = writer;

    }

    public synchronized void flush() throws XMLStreamException {

        super.flush();

        // send any cached start element
        if (savedStart != null) {

            XMLWriterUtils.writeStartElement(savedStart, false, writer);

        }

        writer.flush();

    }

    public synchronized void close() throws XMLStreamException {

        super.close();
        writer.close();

    }

    /**
     * Saved reference to most recent start element. This is used to properly
     * write empty elements. Each startElement event is saved here until the
     * next event is received, at which point it will be written as a start or
     * empty element if it is followed directly by an EndElement.
     */
    private StartElement savedStart;

    protected synchronized void sendEvent(XMLEvent event)
            throws XMLStreamException {

        // Check if we have a cached start tag. If we do, then we should
        // check if this event is actually an end tag, so we can properly
        // write an empty element.
        if (savedStart != null) {

            StartElement start = savedStart;
            savedStart = null;

            if (event.getEventType() == XMLEvent.END_ELEMENT) {

                // this end tag directly followed a start tag, so send
                // the underlying writer an empty start element
                XMLWriterUtils.writeStartElement(start, true, writer);
                return;

            } else {

                // element has content, so send a regular start tag
                XMLWriterUtils.writeStartElement(start, false, writer);

            }

        }

        if (event.isStartElement()) {

            // cache the event
            savedStart = event.asStartElement();

        } else if (event instanceof ExtendedXMLEvent) {

            // let the event handle itself
            ((ExtendedXMLEvent) event).writeEvent(writer);

        } else {

            // write the event to the stream
            XMLWriterUtils.writeEvent(event, writer);

        }

    }

}