/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.util;

import org.codehaus.stax2.XMLOutputFactory2;
import org.codehaus.stax2.XMLStreamWriter2;

import java.io.OutputStream;
import java.io.Writer;

import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;

public class XMLDelegateOutputFactory extends XMLOutputFactory2
{
	private XMLOutputFactory2 mFactory;

	protected XMLDelegateOutputFactory(XMLOutputFactory2 factory)
    {
	    super();
	    mFactory = factory;
    }

	/**
	 * @return
	 * @see java.lang.Object#hashCode()
	 */
    public int hashCode()
    {
	    return mFactory.hashCode();
    }

	/**
	 * @param obj
	 * @return
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
    public boolean equals(Object obj)
    {
	    return mFactory.equals(obj);
    }

	/**
	 * @param w
	 * @param encoding
	 * @return
	 * @throws XMLStreamException
	 * @see org.codehaus.stax2.XMLOutputFactory2#createXMLEventWriter(java.io.Writer, java.lang.String)
	 */
    public XMLEventWriter createXMLEventWriter(Writer w, String encoding) throws XMLStreamException
    {
	    return mFactory.createXMLEventWriter(w, encoding);
    }

	/**
	 * @param sw
	 * @return
	 * @throws XMLStreamException
	 * @see org.codehaus.stax2.XMLOutputFactory2#createXMLEventWriter(javax.xml.stream.XMLStreamWriter)
	 */
    public XMLEventWriter createXMLEventWriter(XMLStreamWriter sw) throws XMLStreamException
    {
	    return mFactory.createXMLEventWriter(sw);
    }

	/**
	 * @param w
	 * @param encoding
	 * @return
	 * @throws XMLStreamException
	 * @see org.codehaus.stax2.XMLOutputFactory2#createXMLStreamWriter(java.io.Writer, java.lang.String)
	 */
    public XMLStreamWriter2 createXMLStreamWriter(Writer w, String encoding) throws XMLStreamException
    {
	    return mFactory.createXMLStreamWriter(w, encoding);
    }

	/**
	 * 
	 * @see org.codehaus.stax2.XMLOutputFactory2#configureForXmlConformance()
	 */
    public void configureForXmlConformance()
    {
	    mFactory.configureForXmlConformance();
    }

	/**
	 * 
	 * @see org.codehaus.stax2.XMLOutputFactory2#configureForRobustness()
	 */
    public void configureForRobustness()
    {
	    mFactory.configureForRobustness();
    }

	/**
	 * 
	 * @see org.codehaus.stax2.XMLOutputFactory2#configureForSpeed()
	 */
    public void configureForSpeed()
    {
	    mFactory.configureForSpeed();
    }

	/**
	 * @param stream
	 * @return
	 * @throws XMLStreamException
	 * @see javax.xml.stream.XMLOutputFactory#createXMLStreamWriter(java.io.Writer)
	 */
    public XMLStreamWriter createXMLStreamWriter(Writer stream) throws XMLStreamException
    {
	    return mFactory.createXMLStreamWriter(stream);
    }

	/**
	 * @param stream
	 * @return
	 * @throws XMLStreamException
	 * @see javax.xml.stream.XMLOutputFactory#createXMLStreamWriter(java.io.OutputStream)
	 */
    public XMLStreamWriter createXMLStreamWriter(OutputStream stream) throws XMLStreamException
    {
	    return mFactory.createXMLStreamWriter(stream);
    }

	/**
	 * @param stream
	 * @param encoding
	 * @return
	 * @throws XMLStreamException
	 * @see javax.xml.stream.XMLOutputFactory#createXMLStreamWriter(java.io.OutputStream, java.lang.String)
	 */
    public XMLStreamWriter createXMLStreamWriter(OutputStream stream, String encoding) throws XMLStreamException
    {
	    return mFactory.createXMLStreamWriter(stream, encoding);
    }

	/**
	 * @param result
	 * @return
	 * @throws XMLStreamException
	 * @see javax.xml.stream.XMLOutputFactory#createXMLStreamWriter(javax.xml.transform.Result)
	 */
    public XMLStreamWriter createXMLStreamWriter(Result result) throws XMLStreamException
    {
	    return mFactory.createXMLStreamWriter(result);
    }

	/**
	 * @return
	 * @see java.lang.Object#toString()
	 */
    public String toString()
    {
	    return mFactory.toString();
    }

	/**
	 * @param result
	 * @return
	 * @throws XMLStreamException
	 * @see javax.xml.stream.XMLOutputFactory#createXMLEventWriter(javax.xml.transform.Result)
	 */
    public XMLEventWriter createXMLEventWriter(Result result) throws XMLStreamException
    {
	    return mFactory.createXMLEventWriter(result);
    }

	/**
	 * @param stream
	 * @return
	 * @throws XMLStreamException
	 * @see javax.xml.stream.XMLOutputFactory#createXMLEventWriter(java.io.OutputStream)
	 */
    public XMLEventWriter createXMLEventWriter(OutputStream stream) throws XMLStreamException
    {
	    return mFactory.createXMLEventWriter(stream);
    }

	/**
	 * @param stream
	 * @param encoding
	 * @return
	 * @throws XMLStreamException
	 * @see javax.xml.stream.XMLOutputFactory#createXMLEventWriter(java.io.OutputStream, java.lang.String)
	 */
    public XMLEventWriter createXMLEventWriter(OutputStream stream, String encoding) throws XMLStreamException
    {
	    return mFactory.createXMLEventWriter(stream, encoding);
    }

	/**
	 * @param stream
	 * @return
	 * @throws XMLStreamException
	 * @see javax.xml.stream.XMLOutputFactory#createXMLEventWriter(java.io.Writer)
	 */
    public XMLEventWriter createXMLEventWriter(Writer stream) throws XMLStreamException
    {
	    return mFactory.createXMLEventWriter(stream);
    }

	/**
	 * @param name
	 * @param value
	 * @throws IllegalArgumentException
	 * @see javax.xml.stream.XMLOutputFactory#setProperty(java.lang.String, java.lang.Object)
	 */
    public void setProperty(String name, Object value) throws IllegalArgumentException
    {
	    mFactory.setProperty(name, value);
    }

	/**
	 * @param name
	 * @return
	 * @throws IllegalArgumentException
	 * @see javax.xml.stream.XMLOutputFactory#getProperty(java.lang.String)
	 */
    public Object getProperty(String name) throws IllegalArgumentException
    {
	    return mFactory.getProperty(name);
    }

	/**
	 * @param name
	 * @return
	 * @see javax.xml.stream.XMLOutputFactory#isPropertySupported(java.lang.String)
	 */
    public boolean isPropertySupported(String name)
    {
	    return mFactory.isPropertySupported(name);
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