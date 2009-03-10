/*
 * $Id: BaseXMLEventAllocator.java,v 1.1 2004/07/05 23:09:31 cniles Exp $
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

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.*;
import javax.xml.stream.util.XMLEventAllocator;
import javax.xml.stream.util.XMLEventConsumer;

/**
 * Abstract base class for {@link XMLEventAllocator} implementations.
 * 
 * @author Christian Niles
 * @version $Revision: 1.1 $
 */
public abstract class BaseXMLEventAllocator implements XMLEventAllocator {

    public void allocate(XMLStreamReader reader, XMLEventConsumer consumer)
            throws XMLStreamException {

        consumer.add(allocate(reader));

    }

    /**
     * Delegates allocation to the appropriate <code>allocateXXX</code> method.
     */
    public XMLEvent allocate(XMLStreamReader reader) throws XMLStreamException {

        int eventType = reader.getEventType();
        switch (eventType) {

            case XMLEvent.START_ELEMENT :
                return allocateStartElement(reader);

            case XMLEvent.END_ELEMENT :
                return allocateEndElement(reader);

            case XMLEvent.CHARACTERS :
                return allocateCharacters(reader);

            case XMLEvent.CDATA :
                return allocateCData(reader);

            case XMLEvent.SPACE :
                return allocateIgnorableSpace(reader);

            case XMLEvent.COMMENT :
                return allocateComment(reader);

            case XMLEvent.DTD :
                return allocateDTD(reader);

            case XMLEvent.ENTITY_REFERENCE :
                return allocateEntityReference(reader);

            case XMLEvent.PROCESSING_INSTRUCTION :
                return allocateProcessingInstruction(reader);

            case XMLEvent.START_DOCUMENT :
                return allocateStartDocument(reader);

            case XMLEvent.END_DOCUMENT :
                return allocateEndDocument(reader);

            default :
                throw new XMLStreamException("Unexpected reader state: "
                        + eventType);

        }

    }

    public abstract StartElement allocateStartElement(XMLStreamReader reader)
            throws XMLStreamException;

    public abstract EndElement allocateEndElement(XMLStreamReader reader)
            throws XMLStreamException;

    public abstract Characters allocateCharacters(XMLStreamReader reader)
            throws XMLStreamException;

    public abstract Characters allocateCData(XMLStreamReader reader)
            throws XMLStreamException;

    public abstract Characters allocateIgnorableSpace(XMLStreamReader reader)
            throws XMLStreamException;

    public abstract EntityReference allocateEntityReference(
            XMLStreamReader reader) throws XMLStreamException;

    public abstract Comment allocateComment(XMLStreamReader reader)
            throws XMLStreamException;

    public abstract DTD allocateDTD(XMLStreamReader reader)
            throws XMLStreamException;

    public abstract StartDocument allocateStartDocument(XMLStreamReader reader)
            throws XMLStreamException;

    public abstract EndDocument allocateEndDocument(XMLStreamReader reader)
            throws XMLStreamException;

    public abstract ProcessingInstruction allocateProcessingInstruction(
            XMLStreamReader reader) throws XMLStreamException;

    public abstract NamespaceContext createStableNamespaceContext(
            XMLStreamReader reader);

    public abstract Location createStableLocation(XMLStreamReader reader);

}