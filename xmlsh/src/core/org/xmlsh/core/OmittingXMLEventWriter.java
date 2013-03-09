/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.core;

import javanet.staxutils.helpers.EventWriterDelegate;

import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

public class OmittingXMLEventWriter extends EventWriterDelegate {

	public OmittingXMLEventWriter(XMLEventWriter out) {
		super(out);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see javanet.staxutils.helpers.EventWriterDelegate#add(javax.xml.stream.events.XMLEvent)
	 */
	@Override
	public void add(XMLEvent event) throws XMLStreamException {
		
		if( event.getEventType() == XMLEvent.START_DOCUMENT )
			return ;
		super.add( event );
	}
	

}



//
//
//Copyright (C) 2008-2013    David A. Lee.
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
