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
package javanet.staxutils;

import javanet.staxutils.helpers.FilterXMLOutputFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

/**
 * An output factory that optionally wraps a filter around each writer. The
 * property {@link #INDENTING} controls indentation of output.
 */
public class StaxUtilsXMLOutputFactory extends FilterXMLOutputFactory
{
    /**
     * A Boolean property controlling whether to indent output. If true, output
     * is indented by {@link IndentingXMLEventWriter} or
     * {@link IndentingXMLStreamWriter}. The default is false.
     */
    public static final String INDENTING = "net.java.staxutils.indenting";

    /**
     * A String property whose value indents one level. The default is
     * {@link Indentation#DEFAULT_INDENT}.
     */
    public static final String INDENT = "net.java.staxutils.indent";

    /**
     * A String property whose value introduces a new line for indentation. The
     * default is {@link Indentation#NORMAL_END_OF_LINE}.
     */
    public static final String NEW_LINE = "net.java.staxutils.newLine";

    public StaxUtilsXMLOutputFactory() {
    }

    public StaxUtilsXMLOutputFactory(XMLOutputFactory source) {
        super(source);
    }

    private boolean indenting = false;

    private String indent = Indentation.DEFAULT_INDENT;

    private String newLine = Indentation.NORMAL_END_OF_LINE;

    protected XMLEventWriter filter(XMLEventWriter writer) {
        if (indenting) {
            IndentingXMLEventWriter indenter = new IndentingXMLEventWriter(writer);
            indenter.setNewLine(newLine);
            indenter.setIndent(indent);
            writer = indenter;
        }
        return writer;
    }

    protected XMLStreamWriter filter(XMLStreamWriter writer) {
        if (indenting) {
            IndentingXMLStreamWriter indenter = new IndentingXMLStreamWriter(writer);
            indenter.setNewLine(newLine);
            indenter.setIndent(indent);
            writer = indenter;
        }
        return writer;
    }

    public boolean isPropertySupported(String name) {
        return INDENTING.equals(name) || INDENT.equals(name) || NEW_LINE.equals(name) //
                || super.isPropertySupported(name);
    }

    public void setProperty(String name, Object value) throws IllegalArgumentException {
        if (INDENTING.equals(name)) {
            indenting = ((Boolean) value).booleanValue();
        } else if (INDENT.equals(name)) {
            indent = (String) value;
        } else if (NEW_LINE.equals(name)) {
            newLine = (String) value;
        } else {
            super.setProperty(name, value);
        }
    }

    public Object getProperty(String name) throws IllegalArgumentException {
        if (INDENTING.equals(name)) {
            return indenting ? Boolean.TRUE : Boolean.FALSE;
        } else if (INDENT.equals(name)) {
            return indent;
        } else if (NEW_LINE.equals(name)) {
            return newLine;
        } else {
            return super.getProperty(name);
        }
    }

    public int hashCode() {
        return super.hashCode() + (indenting ? 1 : 0) + hashCode(indent) + hashCode(newLine);
    }

    public boolean equals(Object o) {
        if (!(o instanceof StaxUtilsXMLOutputFactory))
            return false;
        StaxUtilsXMLOutputFactory that = (StaxUtilsXMLOutputFactory) o;
        return super.equals(that) && (indenting == that.indenting) && equals(indent, that.indent)
                && equals(newLine, that.newLine);
    }

}
