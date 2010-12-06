/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.commands.stax;

import java.util.List;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import net.sf.saxon.s9api.QName;
import org.xmlsh.core.BuiltinFunctionCommand;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.StAXUtils;

public class hasNext extends BuiltinFunctionCommand {

	
	
	public hasNext()
	{
		super("hasNext");
	}
	
	@Override
	public XValue run(Shell shell, List<XValue> args) throws InvalidArgumentException, XMLStreamException  {
		
		
		if( args.size() == 0 )
			return null;
		
		XValue a0 = args.get(0);
		if(! (a0.asObject() instanceof XMLEventReader ) )
				throw new InvalidArgumentException("Expected XMLEventReader as args[0]");
		XMLEventReader reader = (XMLEventReader) a0.asObject();
		
		
		switch( args.size() ){
		case	1:
		
			return new XValue( reader.hasNext() );
		
		case	2: 	// type
		{
			int type = StAXUtils.getEventTypeByName( args.get(1).toString() );
			if( type < 0)
				return new XValue(false);
			
			while( reader.hasNext() && reader.peek().getEventType() != type )
				reader.nextEvent();
			
			return new XValue( reader.hasNext() );
		}	
		case	3: 	// type name
		{	
			int type = StAXUtils.getEventTypeByName( args.get(1).toString() );
			if( type < 0)
				return new XValue(false);
			QName name = args.get(2).asQName();
			
			while( reader.hasNext() ){
				XMLEvent event = reader.peek();
				if( event.getEventType() == type ){
						
					if( type == XMLEvent.START_ELEMENT && StAXUtils.matchesQName(reader.peek().asStartElement().getName() , name ) )
						return new XValue( true );
					else
					if( type == XMLEvent.END_ELEMENT && StAXUtils.matchesQName(reader.peek().asEndElement().getName() , name ) )
						return new XValue( true );
					
					
				}
				reader.nextEvent();
			}
				
			return new XValue( false  );
		}
		default:
			return null;
		}
	}

}



//
//
//Copyright (C) 2008,2009,2010 David A. Lee.
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
