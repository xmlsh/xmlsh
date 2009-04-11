/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.util;

import javanet.staxutils.BaseXMLStreamReader;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

public class XMLEventStreamReader extends BaseXMLStreamReader {

	private XMLEventReader 		mReader;
	private	 XMLEvent			mEvent; 	// current event
	
	public XMLEventStreamReader(XMLEventReader reader) {
		mReader = reader;
	}

	public XMLEventStreamReader(XMLEventReader reader,String systemId, String encoding) {
		super(systemId, encoding);
		mReader = reader;
	}

	public void close() throws XMLStreamException {
		mReader.close();
	}

	public int getAttributeCount() 
	{
		//if( mEvent != null )
			return 0;
		
		
	}

	public QName getAttributeName(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getAttributeType(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getAttributeValue(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getAttributeValue(String namespaceURI, String localName) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getCharacterEncodingScheme() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getEventType() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getLocalName() {
		// TODO Auto-generated method stub
		return null;
	}

	public Location getLocation() {
		// TODO Auto-generated method stub
		return null;
	}

	public QName getName() {
		// TODO Auto-generated method stub
		return null;
	}

	public NamespaceContext getNamespaceContext() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getNamespaceCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getNamespacePrefix(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getNamespaceURI(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getPIData() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getPITarget() {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getProperty(String name) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getText() {
		// TODO Auto-generated method stub
		return null;
	}

	public char[] getTextCharacters() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getTextCharacters(int sourceStart, char[] target, int targetStart, int length)
			throws XMLStreamException {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getTextLength() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getTextStart() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getVersion() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean hasNext() throws XMLStreamException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isAttributeSpecified(int index) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isStandalone() {
		// TODO Auto-generated method stub
		return false;
	}

	public int next() throws XMLStreamException {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean standaloneSet() {
		// TODO Auto-generated method stub
		return false;
	}

}



//
//
//Copyright (C) 2008,2009 , David A. Lee.
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
