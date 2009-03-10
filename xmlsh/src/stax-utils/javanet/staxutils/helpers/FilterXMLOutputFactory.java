/* Copyright (c) 2006, John Kristian
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

import java.io.OutputStream;
import java.io.Writer;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;

/**
 * An output factory that transforms each writer from a contained factory.
 * Subclasses are required to implement the transformation.
 */
public abstract class FilterXMLOutputFactory extends XMLOutputFactory
{
    public FilterXMLOutputFactory() {
        this(XMLOutputFactory.newInstance());
    }

    public FilterXMLOutputFactory(XMLOutputFactory source) {
        this.source = source;
    }

    protected XMLOutputFactory source;

    /** Transform the given writer. */
    protected abstract XMLEventWriter filter(XMLEventWriter writer);

    protected abstract XMLStreamWriter filter(XMLStreamWriter writer);

    /** Delegates to source. */
    public boolean isPropertySupported(String name) {
        return source.isPropertySupported(name);
    }

    /** Delegates to source. */
    public void setProperty(String name, Object value) throws IllegalArgumentException {
        source.setProperty(name, value);
    }

    /** Delegates to source. */
    public Object getProperty(String name) throws IllegalArgumentException {
        return source.getProperty(name);
    }

    public XMLEventWriter createXMLEventWriter(Result result) throws XMLStreamException {
        return filter(source.createXMLEventWriter(result));
    }

    public XMLEventWriter createXMLEventWriter(Writer writer) throws XMLStreamException {
        return filter(source.createXMLEventWriter(writer));
    }

    public XMLEventWriter createXMLEventWriter(OutputStream stream) throws XMLStreamException {
        return filter(source.createXMLEventWriter(stream));
    }

    public XMLEventWriter createXMLEventWriter(OutputStream stream, String encoding)
            throws XMLStreamException {
        return filter(source.createXMLEventWriter(stream, encoding));
    }

    public XMLStreamWriter createXMLStreamWriter(Result result) throws XMLStreamException {
        return filter(source.createXMLStreamWriter(result));
    }

    public XMLStreamWriter createXMLStreamWriter(Writer writer) throws XMLStreamException {
        return filter(source.createXMLStreamWriter(writer));
    }

    public XMLStreamWriter createXMLStreamWriter(OutputStream stream) throws XMLStreamException {
        return filter(source.createXMLStreamWriter(stream));
    }

    public XMLStreamWriter createXMLStreamWriter(OutputStream stream, String encoding)
            throws XMLStreamException {
        return filter(source.createXMLStreamWriter(stream, encoding));
    }

    public int hashCode() {
        return hashCode(source);
    }

    protected static int hashCode(Object o) {
        return (o == null) ? 0 : o.hashCode();
    }

    public boolean equals(Object o) {
        if (!(o instanceof FilterXMLOutputFactory))
            return false;
        FilterXMLOutputFactory that = (FilterXMLOutputFactory) o;
        return equals(source, that.source);
    }

    protected static boolean equals(Object x, Object y) {
        return (x == null) ? (y == null) : x.equals(y);
    }

}
