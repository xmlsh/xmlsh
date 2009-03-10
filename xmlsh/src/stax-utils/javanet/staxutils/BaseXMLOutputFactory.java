/*
 * $Id: BaseXMLOutputFactory.java,v 1.2 2004/07/15 02:51:33 cniles Exp $
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

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import javanet.staxutils.XMLStreamEventWriter;

import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;

/**
 * Base {@link XMLOutputFactory} that provides common event functionality.
 * 
 * @author Christian Niles
 * @version $Revision: 1.2 $
 */
public abstract class BaseXMLOutputFactory extends XMLOutputFactory {

    public XMLEventWriter createXMLEventWriter(OutputStream stream,
            String encoding) throws XMLStreamException {

        try {

            return createXMLEventWriter(new OutputStreamWriter(stream, encoding));

        } catch (UnsupportedEncodingException e) {

            throw new XMLStreamException(e);

        }

    }

    public XMLEventWriter createXMLEventWriter(OutputStream stream)
            throws XMLStreamException {

        return createXMLEventWriter(new OutputStreamWriter(stream));

    }

    public XMLStreamWriter createXMLStreamWriter(OutputStream stream,
            String encoding) throws XMLStreamException {

        try {

            return createXMLStreamWriter(new OutputStreamWriter(stream,
                    encoding));

        } catch (UnsupportedEncodingException e) {

            throw new XMLStreamException(e);

        }

    }

    public XMLStreamWriter createXMLStreamWriter(OutputStream stream)
            throws XMLStreamException {

        return createXMLStreamWriter(new OutputStreamWriter(stream));

    }

    public XMLEventWriter createXMLEventWriter(Result result)
            throws XMLStreamException {

        return createXMLEventWriter(createXMLStreamWriter(result));

    }

    /**
     * Creates an {@link XMLEventWriter} that writes to the provided
     * {@link XMLStreamWriter}.
     * 
     * @param writer The destination stream.
     * @return An {@link XMLEventWriter} that writes to the provided
     * 		{@link XMLStreamWriter}.
     */
    public XMLEventWriter createXMLEventWriter(XMLStreamWriter writer) {

        return new XMLStreamEventWriter(writer);

    }

    public XMLEventWriter createXMLEventWriter(Writer stream)
            throws XMLStreamException {

        return createXMLEventWriter(createXMLStreamWriter(stream));

    }

    public XMLStreamWriter createXMLStreamWriter(Result result)
            throws XMLStreamException {

        // FIXME Support TrAX
        throw new UnsupportedOperationException("TrAX result not supported");

    }

    public Object getProperty(String name) throws IllegalArgumentException {

        throw new IllegalArgumentException(name + " property isn't supported");

    }

    public boolean isPropertySupported(String name) {

        return false;

    }

    public void setProperty(String name, Object value)
            throws IllegalArgumentException {

        throw new IllegalArgumentException(name + " property isn't supported");

    }

}