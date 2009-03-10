/*
 * $Id: BaseXMLInputFactory.java,v 1.2 2004/07/15 02:51:32 cniles Exp $
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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import javanet.staxutils.XMLStreamEventReader;

import javax.xml.stream.EventFilter;
import javax.xml.stream.StreamFilter;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLReporter;
import javax.xml.stream.XMLResolver;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.util.XMLEventAllocator;
import javax.xml.transform.Source;

/**
 *
 * @author Christian Niles
 * @version $Revision: 1.2 $
 */
public abstract class BaseXMLInputFactory extends XMLInputFactory {

    protected XMLEventAllocator eventAllocator;

    protected XMLReporter xmlReporter;

    protected XMLResolver xmlResolver;

    public Object getProperty(String name) throws IllegalArgumentException {

        // TODO provide base support for well-known properties?
        throw new IllegalArgumentException(name + " property not supported");

    }

    public boolean isPropertySupported(String name) {

        return false;

    }

    public void setProperty(String name, Object value)
            throws IllegalArgumentException {

        throw new IllegalArgumentException(name + " property not supported");

    }

    public XMLEventAllocator getEventAllocator() {

        return this.eventAllocator;

    }

    public void setEventAllocator(XMLEventAllocator eventAllocator) {

        this.eventAllocator = eventAllocator;

    }

    public XMLReporter getXMLReporter() {

        return this.xmlReporter;

    }

    public void setXMLReporter(XMLReporter xmlReporter) {

        this.xmlReporter = xmlReporter;

    }

    public XMLResolver getXMLResolver() {

        return this.xmlResolver;

    }

    public void setXMLResolver(XMLResolver xmlResolver) {

        this.xmlResolver = xmlResolver;

    }

    public XMLEventReader createXMLEventReader(InputStream stream,
            String encoding) throws XMLStreamException {

        try {

            if (encoding != null) {

                return createXMLEventReader(new InputStreamReader(stream,
                        encoding), encoding);

            } else {

                return createXMLEventReader(new InputStreamReader(stream));

            }

        } catch (UnsupportedEncodingException e) {

            throw new XMLStreamException(e);

        }

    }

    public XMLEventReader createXMLEventReader(InputStream stream)
            throws XMLStreamException {

        return createXMLEventReader(new InputStreamReader(stream));

    }

    public XMLEventReader createXMLEventReader(String systemId,
            InputStream stream) throws XMLStreamException {

        return createXMLEventReader(systemId, new InputStreamReader(stream));

    }

    public XMLEventReader createXMLEventReader(XMLStreamReader reader)
            throws XMLStreamException {

        return new XMLStreamEventReader(reader);

    }

    public XMLStreamReader createXMLStreamReader(InputStream stream,
            String encoding) throws XMLStreamException {

        try {

            if (encoding != null) {

                return createXMLStreamReader(new InputStreamReader(stream,
                        encoding), encoding);

            } else {

                return createXMLStreamReader(new InputStreamReader(stream));

            }

        } catch (UnsupportedEncodingException e) {

            throw new XMLStreamException(e);

        }

    }

    public XMLStreamReader createXMLStreamReader(InputStream stream)
            throws XMLStreamException {

        return createXMLStreamReader(new InputStreamReader(stream));

    }

    public XMLStreamReader createXMLStreamReader(String systemId,
            InputStream stream) throws XMLStreamException {

        return createXMLStreamReader(systemId, new InputStreamReader(stream));

    }

    public XMLEventReader createXMLEventReader(Reader reader)
            throws XMLStreamException {

        return createXMLEventReader(createXMLStreamReader(reader));

    }

    public XMLEventReader createXMLEventReader(Reader reader, String encoding)
            throws XMLStreamException {

        return createXMLEventReader(createXMLStreamReader(reader, encoding));

    }

    public XMLEventReader createXMLEventReader(Source source)
            throws XMLStreamException {

        return createXMLEventReader(createXMLStreamReader(source));

    }

    public XMLEventReader createXMLEventReader(String systemId, Reader reader)
            throws XMLStreamException {

        return createXMLEventReader(createXMLStreamReader(systemId, reader));

    }

    public XMLEventReader createXMLEventReader(String systemId, Reader reader,
            String encoding) throws XMLStreamException {

        return createXMLEventReader(createXMLStreamReader(systemId, reader,
                encoding));

    }

    public XMLStreamReader createXMLStreamReader(Source source)
            throws XMLStreamException {

        // TODO implement TrAX support
        throw new UnsupportedOperationException();

    }

    public XMLStreamReader createXMLStreamReader(Reader reader)
            throws XMLStreamException {

        return createXMLStreamReader(null, reader, null);

    }

    public XMLStreamReader createXMLStreamReader(Reader reader, String encoding)
            throws XMLStreamException {

        return createXMLStreamReader(null, reader, encoding);

    }

    public XMLStreamReader createXMLStreamReader(String systemId, Reader reader)
            throws XMLStreamException {

        String encoding = null;
        if (reader instanceof InputStreamReader) {

            encoding = ((InputStreamReader) reader).getEncoding();

        }

        return createXMLStreamReader(systemId, reader, encoding);

    }

    public XMLEventReader createFilteredReader(XMLEventReader reader,
            EventFilter filter) throws XMLStreamException {

        // TODO implement filter support
        throw new UnsupportedOperationException();

    }

    public XMLStreamReader createFilteredReader(XMLStreamReader reader,
            StreamFilter filter) throws XMLStreamException {

        // TODO implement filter support
        throw new UnsupportedOperationException();

    }

    /**
     * Called by all other methods to construct an {@link XMLStreamReader}.
     * 
     * @param systemId The system ID of the provided reader, or <code>null</code>
     * @param reader The character stream from which to construct the StAX stream.
     * @param encoding The underlying encoding of the reader, or <code>null</code>.
     * @return The newly constructed {@link XMLStreamReader}.
     * @throws XMLStreamException If an error occurs constructing the reader.
     */
    public abstract XMLStreamReader createXMLStreamReader(String systemId,
            Reader reader, String encoding) throws XMLStreamException;

}