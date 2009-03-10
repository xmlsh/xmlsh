/**
 * $Id: $
 * $Date: $
 *
 */

/**
 * XMLStreamWriterToDestination bridges XMLStreamWriter to Destination
 * 
 */



package org.xmlsh.util;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class XMLStreamWriterToDestination implements XMLStreamWriter {

	public void close() throws XMLStreamException {
		// TODO Auto-generated method stub

	}

	public void flush() throws XMLStreamException {
		// TODO Auto-generated method stub

	}

	public NamespaceContext getNamespaceContext() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getPrefix(String uri) throws XMLStreamException {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getProperty(String name) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	public void setDefaultNamespace(String uri) throws XMLStreamException {
		// TODO Auto-generated method stub

	}

	public void setNamespaceContext(NamespaceContext context) throws XMLStreamException {
		// TODO Auto-generated method stub

	}

	public void setPrefix(String prefix, String uri) throws XMLStreamException {
		// TODO Auto-generated method stub

	}

	public void writeAttribute(String localName, String value) throws XMLStreamException {
		// TODO Auto-generated method stub

	}

	public void writeAttribute(String namespaceURI, String localName, String value)
			throws XMLStreamException {
		// TODO Auto-generated method stub

	}

	public void writeAttribute(String prefix, String namespaceURI, String localName, String value)
			throws XMLStreamException {
		// TODO Auto-generated method stub

	}

	public void writeCData(String data) throws XMLStreamException {
		// TODO Auto-generated method stub

	}

	public void writeCharacters(String text) throws XMLStreamException {
		// TODO Auto-generated method stub

	}

	public void writeCharacters(char[] text, int start, int len) throws XMLStreamException {
		// TODO Auto-generated method stub

	}

	public void writeComment(String data) throws XMLStreamException {
		// TODO Auto-generated method stub

	}

	public void writeDTD(String dtd) throws XMLStreamException {
		// TODO Auto-generated method stub

	}

	public void writeDefaultNamespace(String namespaceURI) throws XMLStreamException {
		// TODO Auto-generated method stub

	}

	public void writeEmptyElement(String localName) throws XMLStreamException {
		// TODO Auto-generated method stub

	}

	public void writeEmptyElement(String namespaceURI, String localName) throws XMLStreamException {
		// TODO Auto-generated method stub

	}

	public void writeEmptyElement(String prefix, String localName, String namespaceURI)
			throws XMLStreamException {
		// TODO Auto-generated method stub

	}

	public void writeEndDocument() throws XMLStreamException {
		// TODO Auto-generated method stub

	}

	public void writeEndElement() throws XMLStreamException {
		// TODO Auto-generated method stub

	}

	public void writeEntityRef(String name) throws XMLStreamException {
		// TODO Auto-generated method stub

	}

	public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException {
		// TODO Auto-generated method stub

	}

	public void writeProcessingInstruction(String target) throws XMLStreamException {
		// TODO Auto-generated method stub

	}

	public void writeProcessingInstruction(String target, String data) throws XMLStreamException {
		// TODO Auto-generated method stub

	}

	public void writeStartDocument() throws XMLStreamException {
		// TODO Auto-generated method stub

	}

	public void writeStartDocument(String version) throws XMLStreamException {
		// TODO Auto-generated method stub

	}

	public void writeStartDocument(String encoding, String version) throws XMLStreamException {
		// TODO Auto-generated method stub

	}

	public void writeStartElement(String localName) throws XMLStreamException {
		// TODO Auto-generated method stub

	}

	public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {
		// TODO Auto-generated method stub

	}

	public void writeStartElement(String prefix, String localName, String namespaceURI)
			throws XMLStreamException {
		// TODO Auto-generated method stub

	}

}



//
//
//Copyright (C) 2008, David A. Lee.
//
//The contents of this file are subject to the "Simplified BSD License" (the "License");
//you may not use this file except in compliance with the License. You may obtain a copy of the
//License at http://www.opensource.org/licenses/bsd-license.php 
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied.
//See the License for the specific language governing rights and limitations under the License.
//
//The Original Code is: all this file.
//
//The Initial Developer of the Original Code is David A. Lee
//
//Portions created by (your name) are Copyright (C) (your legal entity). All Rights Reserved.
//
//Contributor(s): none.
//
