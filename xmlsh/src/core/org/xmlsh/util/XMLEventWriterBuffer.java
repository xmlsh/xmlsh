/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.util;

import java.util.LinkedList;
import java.util.List;

import javanet.staxutils.BaseXMLEventReader;
import javanet.staxutils.BaseXMLEventWriter;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

public class XMLEventWriterBuffer extends BaseXMLEventWriter {
	private List<XMLEvent>  mList = new LinkedList<XMLEvent>();

	
	private class BufferReader extends BaseXMLEventReader {

		public boolean hasNext() {
			return ! mList.isEmpty() ;
		}

		public XMLEvent nextEvent() throws XMLStreamException {
			return mList.remove(0);
			
		}

		public XMLEvent peek() throws XMLStreamException {
			return mList.get(0);
		}
		
		
	}
	
	
	
	@Override
	protected void sendEvent(XMLEvent event) throws XMLStreamException {
		mList.add(event);
		
	}
	
	public List<XMLEvent> getBuffer() {
		return mList ;
	}
	
	public	XMLEventReader 	getReader()
	{
		return new BufferReader();
		
	}
	

}



//
//
//Copyright (C) 2008-2012  David A. Lee.
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
