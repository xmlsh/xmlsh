/*
 * Copyright (c) 2006, John Kristian
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *      *   Redistributions of source code must retain the above copyright
 *          notice, this list of conditions and the following disclaimer.
 *
 *      *   Redistributions in binary form must reproduce the above copyright
 *          notice, this list of conditions and the following disclaimer in the
 *          documentation and/or other materials provided with the distribution.
 *
 *      *   Neither the name of StAX-Utils nor the names of its contributors
 *          may be used to endorse or promote products derived from this
 *          software without specific prior written permission.
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

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

/**
 * Abstract class for implementing XML filters. This class provides methods that
 * merely delegate to a contained XMLEventWriter. Subclasses should override
 * some of these methods, and may also provide additional methods and fields.
 * 
 * @author <a href="mailto:jk2006@engineer.com">John Kristian</a>
 */
public abstract class EventWriterDelegate implements XMLEventWriter {

    protected EventWriterDelegate(XMLEventWriter out) {
        this.out = out;
    }

    /** The downstream writer, to which events are delegated. */
    protected final XMLEventWriter out;

    public void setNamespaceContext(NamespaceContext context) throws XMLStreamException {
        out.setNamespaceContext(context);
    }

    public NamespaceContext getNamespaceContext() {
        return out.getNamespaceContext();
    }

    public void setDefaultNamespace(String uri) throws XMLStreamException {
        out.setDefaultNamespace(uri);
    }

    public void setPrefix(String prefix, String uri) throws XMLStreamException {
        out.setPrefix(prefix, uri);
    }

    public String getPrefix(String uri) throws XMLStreamException {
        return out.getPrefix(uri);
    }

    public void add(XMLEvent event) throws XMLStreamException {
        out.add(event);
    }

    /** Add events from the given reader, one by one. */
    public void add(XMLEventReader reader) throws XMLStreamException {
        while (reader.hasNext()) {
            add(reader.nextEvent());
        }
    }

    public void flush() throws XMLStreamException {
        out.flush();
    }

    public void close() throws XMLStreamException {
        out.close();
    }

}
