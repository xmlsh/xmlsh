/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.util;

import java.io.OutputStream;
import java.io.Writer;

import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;

import org.codehaus.stax2.XMLOutputFactory2;
import org.codehaus.stax2.XMLStreamWriter2;

public class XMLDelegateOutputFactory extends XMLOutputFactory
{


    private XMLOutputFactory mFactory;

    public XMLEventWriter createXMLEventWriter(OutputStream stream,
            String encoding) throws XMLStreamException {
        return mFactory.createXMLEventWriter(stream, encoding);
    }


    public XMLEventWriter createXMLEventWriter(OutputStream stream)
            throws XMLStreamException {
        return mFactory.createXMLEventWriter(stream);
    }


    public XMLEventWriter createXMLEventWriter(Result result)
            throws XMLStreamException {
        return mFactory.createXMLEventWriter(result);
    }


    public XMLEventWriter createXMLEventWriter(Writer stream)
            throws XMLStreamException {
        return mFactory.createXMLEventWriter(stream);
    }


    public XMLStreamWriter createXMLStreamWriter(OutputStream stream,
            String encoding) throws XMLStreamException {
        return mFactory.createXMLStreamWriter(stream, encoding);
    }


    public XMLStreamWriter createXMLStreamWriter(OutputStream stream)
            throws XMLStreamException {
        return mFactory.createXMLStreamWriter(stream);
    }


    public XMLStreamWriter createXMLStreamWriter(Result result)
            throws XMLStreamException {
        return mFactory.createXMLStreamWriter(result);
    }


    public XMLStreamWriter createXMLStreamWriter(Writer stream)
            throws XMLStreamException {
        return mFactory.createXMLStreamWriter(stream);
    }


    public boolean equals(Object arg0) {
        return mFactory.equals(arg0);
    }


    public Object getProperty(String name) throws IllegalArgumentException {
        return mFactory.getProperty(name);
    }


    public int hashCode() {
        return mFactory.hashCode();
    }


    public boolean isPropertySupported(String name) {
        return mFactory.isPropertySupported(name);
    }


    public void setProperty(String name, Object value)
            throws IllegalArgumentException {
        mFactory.setProperty(name, value);
    }


    public String toString() {
        return mFactory.toString();
    }


    public XMLDelegateOutputFactory(XMLOutputFactory factory)
    {
        super();
        mFactory = factory;
    }


}



/*
 * Copyright (C) 2008-2012 David A. Lee.
 * 
 * The contents of this file are subject to the "Simplified BSD License" (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.opensource.org/licenses/bsd-license.php 

 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied.
 * See the License for the specific language governing rights and limitations under the License.
 *
 * The Original Code is: all this file.
 *
 * The Initial Developer of the Original Code is David A. Lee
 *
 * Portions created by (your name) are Copyright (C) (your legal entity). All Rights Reserved.
 *
 * Contributor(s): David A. Lee
 * 
 */