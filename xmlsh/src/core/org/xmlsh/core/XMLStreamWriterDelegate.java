/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.core;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class XMLStreamWriterDelegate implements XMLStreamWriter
{
	private XMLStreamWriter mWriter  ;
	public XMLStreamWriterDelegate( XMLStreamWriter w ) {
		mWriter = w ;
	}
	/**
	 * @param localName
	 * @throws XMLStreamException
	 * @see javax.xml.stream.XMLStreamWriter#writeStartElement(java.lang.String)
	 */
    public void writeStartElement(String localName) throws XMLStreamException
    {
        mWriter.writeStartElement(localName);
    }
	/**
	 * @param namespaceURI
	 * @param localName
	 * @throws XMLStreamException
	 * @see javax.xml.stream.XMLStreamWriter#writeStartElement(java.lang.String, java.lang.String)
	 */
    public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException
    {
        mWriter.writeStartElement(namespaceURI, localName);
    }
	/**
	 * @param prefix
	 * @param localName
	 * @param namespaceURI
	 * @throws XMLStreamException
	 * @see javax.xml.stream.XMLStreamWriter#writeStartElement(java.lang.String, java.lang.String, java.lang.String)
	 */
    public void writeStartElement(String prefix, String localName, String namespaceURI) throws XMLStreamException
    {
        mWriter.writeStartElement(prefix, localName, namespaceURI);
    }
	/**
	 * @param namespaceURI
	 * @param localName
	 * @throws XMLStreamException
	 * @see javax.xml.stream.XMLStreamWriter#writeEmptyElement(java.lang.String, java.lang.String)
	 */
    public void writeEmptyElement(String namespaceURI, String localName) throws XMLStreamException
    {
        mWriter.writeEmptyElement(namespaceURI, localName);
    }
	/**
	 * @param prefix
	 * @param localName
	 * @param namespaceURI
	 * @throws XMLStreamException
	 * @see javax.xml.stream.XMLStreamWriter#writeEmptyElement(java.lang.String, java.lang.String, java.lang.String)
	 */
    public void writeEmptyElement(String prefix, String localName, String namespaceURI) throws XMLStreamException
    {
        mWriter.writeEmptyElement(prefix, localName, namespaceURI);
    }
	/**
	 * @param localName
	 * @throws XMLStreamException
	 * @see javax.xml.stream.XMLStreamWriter#writeEmptyElement(java.lang.String)
	 */
    public void writeEmptyElement(String localName) throws XMLStreamException
    {
        mWriter.writeEmptyElement(localName);
    }
	/**
	 * @throws XMLStreamException
	 * @see javax.xml.stream.XMLStreamWriter#writeEndElement()
	 */
    public void writeEndElement() throws XMLStreamException
    {
        mWriter.writeEndElement();
    }
	/**
	 * @throws XMLStreamException
	 * @see javax.xml.stream.XMLStreamWriter#writeEndDocument()
	 */
    public void writeEndDocument() throws XMLStreamException
    {
        mWriter.writeEndDocument();
    }
	/**
	 * @throws XMLStreamException
	 * @see javax.xml.stream.XMLStreamWriter#close()
	 */
    public void close() throws XMLStreamException
    {
        mWriter.close();
    }
	/**
	 * @throws XMLStreamException
	 * @see javax.xml.stream.XMLStreamWriter#flush()
	 */
    public void flush() throws XMLStreamException
    {
        mWriter.flush();
    }
	/**
	 * @param localName
	 * @param value
	 * @throws XMLStreamException
	 * @see javax.xml.stream.XMLStreamWriter#writeAttribute(java.lang.String, java.lang.String)
	 */
    public void writeAttribute(String localName, String value) throws XMLStreamException
    {
        mWriter.writeAttribute(localName, value);
    }
	/**
	 * @param prefix
	 * @param namespaceURI
	 * @param localName
	 * @param value
	 * @throws XMLStreamException
	 * @see javax.xml.stream.XMLStreamWriter#writeAttribute(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
    public void writeAttribute(String prefix, String namespaceURI, String localName, String value)
            throws XMLStreamException
    {
        mWriter.writeAttribute(prefix, namespaceURI, localName, value);
    }
	/**
	 * @param namespaceURI
	 * @param localName
	 * @param value
	 * @throws XMLStreamException
	 * @see javax.xml.stream.XMLStreamWriter#writeAttribute(java.lang.String, java.lang.String, java.lang.String)
	 */
    public void writeAttribute(String namespaceURI, String localName, String value) throws XMLStreamException
    {
        mWriter.writeAttribute(namespaceURI, localName, value);
    }
	/**
	 * @param prefix
	 * @param namespaceURI
	 * @throws XMLStreamException
	 * @see javax.xml.stream.XMLStreamWriter#writeNamespace(java.lang.String, java.lang.String)
	 */
    public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException
    {
        mWriter.writeNamespace(prefix, namespaceURI);
    }
	/**
	 * @param namespaceURI
	 * @throws XMLStreamException
	 * @see javax.xml.stream.XMLStreamWriter#writeDefaultNamespace(java.lang.String)
	 */
    public void writeDefaultNamespace(String namespaceURI) throws XMLStreamException
    {
        mWriter.writeDefaultNamespace(namespaceURI);
    }
	/**
	 * @param data
	 * @throws XMLStreamException
	 * @see javax.xml.stream.XMLStreamWriter#writeComment(java.lang.String)
	 */
    public void writeComment(String data) throws XMLStreamException
    {
        mWriter.writeComment(data);
    }
	/**
	 * @param target
	 * @throws XMLStreamException
	 * @see javax.xml.stream.XMLStreamWriter#writeProcessingInstruction(java.lang.String)
	 */
    public void writeProcessingInstruction(String target) throws XMLStreamException
    {
        mWriter.writeProcessingInstruction(target);
    }
	/**
	 * @param target
	 * @param data
	 * @throws XMLStreamException
	 * @see javax.xml.stream.XMLStreamWriter#writeProcessingInstruction(java.lang.String, java.lang.String)
	 */
    public void writeProcessingInstruction(String target, String data) throws XMLStreamException
    {
        mWriter.writeProcessingInstruction(target, data);
    }
	/**
	 * @param data
	 * @throws XMLStreamException
	 * @see javax.xml.stream.XMLStreamWriter#writeCData(java.lang.String)
	 */
    public void writeCData(String data) throws XMLStreamException
    {
        mWriter.writeCData(data);
    }
	/**
	 * @param dtd
	 * @throws XMLStreamException
	 * @see javax.xml.stream.XMLStreamWriter#writeDTD(java.lang.String)
	 */
    public void writeDTD(String dtd) throws XMLStreamException
    {
        mWriter.writeDTD(dtd);
    }
	/**
	 * @param name
	 * @throws XMLStreamException
	 * @see javax.xml.stream.XMLStreamWriter#writeEntityRef(java.lang.String)
	 */
    public void writeEntityRef(String name) throws XMLStreamException
    {
        mWriter.writeEntityRef(name);
    }
	/**
	 * @throws XMLStreamException
	 * @see javax.xml.stream.XMLStreamWriter#writeStartDocument()
	 */
    public void writeStartDocument() throws XMLStreamException
    {
        mWriter.writeStartDocument();
    }
	/**
	 * @param version
	 * @throws XMLStreamException
	 * @see javax.xml.stream.XMLStreamWriter#writeStartDocument(java.lang.String)
	 */
    public void writeStartDocument(String version) throws XMLStreamException
    {
        mWriter.writeStartDocument(version);
    }
	/**
	 * @param encoding
	 * @param version
	 * @throws XMLStreamException
	 * @see javax.xml.stream.XMLStreamWriter#writeStartDocument(java.lang.String, java.lang.String)
	 */
    public void writeStartDocument(String encoding, String version) throws XMLStreamException
    {
        mWriter.writeStartDocument(encoding, version);
    }
	/**
	 * @param text
	 * @throws XMLStreamException
	 * @see javax.xml.stream.XMLStreamWriter#writeCharacters(java.lang.String)
	 */
    public void writeCharacters(String text) throws XMLStreamException
    {
        mWriter.writeCharacters(text);
    }
	/**
	 * @param text
	 * @param start
	 * @param len
	 * @throws XMLStreamException
	 * @see javax.xml.stream.XMLStreamWriter#writeCharacters(char[], int, int)
	 */
    public void writeCharacters(char[] text, int start, int len) throws XMLStreamException
    {
        mWriter.writeCharacters(text, start, len);
    }
	/**
	 * @param uri
	 * @return
	 * @throws XMLStreamException
	 * @see javax.xml.stream.XMLStreamWriter#getPrefix(java.lang.String)
	 */
    public String getPrefix(String uri) throws XMLStreamException
    {
        return mWriter.getPrefix(uri);
    }
	/**
	 * @param prefix
	 * @param uri
	 * @throws XMLStreamException
	 * @see javax.xml.stream.XMLStreamWriter#setPrefix(java.lang.String, java.lang.String)
	 */
    public void setPrefix(String prefix, String uri) throws XMLStreamException
    {
        mWriter.setPrefix(prefix, uri);
    }
	/**
	 * @param uri
	 * @throws XMLStreamException
	 * @see javax.xml.stream.XMLStreamWriter#setDefaultNamespace(java.lang.String)
	 */
    public void setDefaultNamespace(String uri) throws XMLStreamException
    {
        mWriter.setDefaultNamespace(uri);
    }
	/**
	 * @param context
	 * @throws XMLStreamException
	 * @see javax.xml.stream.XMLStreamWriter#setNamespaceContext(javax.xml.namespace.NamespaceContext)
	 */
    public void setNamespaceContext(NamespaceContext context) throws XMLStreamException
    {
        mWriter.setNamespaceContext(context);
    }
	/**
	 * @return
	 * @see javax.xml.stream.XMLStreamWriter#getNamespaceContext()
	 */
    public NamespaceContext getNamespaceContext()
    {
        return mWriter.getNamespaceContext();
    }
	/**
	 * @param name
	 * @return
	 * @throws IllegalArgumentException
	 * @see javax.xml.stream.XMLStreamWriter#getProperty(java.lang.String)
	 */
    public Object getProperty(String name) throws IllegalArgumentException
    {
        return Boolean.FALSE;
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