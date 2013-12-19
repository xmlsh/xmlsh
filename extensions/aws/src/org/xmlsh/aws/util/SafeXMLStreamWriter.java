/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.aws.util;

import javanet.staxutils.helpers.StreamWriterDelegate;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.xmlsh.util.Util;


public class SafeXMLStreamWriter extends StreamWriterDelegate {

	public SafeXMLStreamWriter(XMLStreamWriter out) {
		super(out);
		
	}

	/* (non-Javadoc)
	 * @see javanet.staxutils.helpers.StreamWriterDelegate#writeAttribute(java.lang.String, java.lang.String)
	 */
	@Override
	public void writeAttribute(String localName, String value) throws XMLStreamException {
		// TODO Auto-generated method stub
		super.writeAttribute(localName, Util.notNull(value));
	}

	/* (non-Javadoc)
	 * @see javanet.staxutils.helpers.StreamWriterDelegate#writeAttribute(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void writeAttribute(String namespaceURI, String localName, String value)
			throws XMLStreamException {
		// TODO Auto-generated method stub
		super.writeAttribute(namespaceURI, localName, Util.notNull(value));
	}

	/* (non-Javadoc)
	 * @see javanet.staxutils.helpers.StreamWriterDelegate#writeAttribute(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void writeAttribute(String prefix, String namespaceURI, String localName, String value)
			throws XMLStreamException {
		// TODO Auto-generated method stub
		super.writeAttribute(prefix, namespaceURI, localName, Util.notNull(value));
	}
	

}



//
//
//Copyright (C) 2008-2014 David A. Lee.
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
