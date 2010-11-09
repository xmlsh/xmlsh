/**
 * $Id: xpwd.java 21 2008-07-04 08:33:47Z daldei $
 * $Date: 2008-07-04 04:33:47 -0400 (Fri, 04 Jul 2008) $
 *
 */

package org.xmlsh.commands.internal;

import java.io.PrintWriter;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.json.JSONObject;
import org.xmlsh.core.InputPort;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;

/*
 * 
 * Convert XML files to an CSV file
 * 
 * Arguments
 * 
 * -header		Add a header row
 * 
 * 
 */

public class xml2json extends XCommand
{

	





	public int run(  List<XValue> args  )	throws Exception
	{
		Options opts = new Options(SerializeOpts.getOptionDefs());
		opts.parse(args);

		args = opts.getRemainingArgs();

		OutputPort stdout = getStdout();
		
		InputPort inp = args.isEmpty() ? getStdin() : getInput( args.get(0) );

		SerializeOpts serializeOpts = getSerializeOpts(opts);
		XMLEventReader reader = inp.asXMLEventReader(serializeOpts);
		
		PrintWriter writer = stdout.asPrintWriter(serializeOpts);
		
		
		
		parse( reader , writer, false  );
		writer.flush();
		writer.close();

		
		// Consume input or we can get a Piped Close
		while( reader.hasNext() )
			reader.nextEvent();
		
		
		reader.close();
		inp.release();
		
		return 0;
		

		
		
	}

	private boolean parse(XMLEventReader reader, PrintWriter writer, boolean bComma ) throws XMLStreamException {
		while( reader.hasNext() ){
			XMLEvent e = reader.nextEvent();
			if( e.isStartElement() ){
				StartElement start = e.asStartElement();
				String name = start.getName().getLocalPart();
				
				if( bComma )
					writer.print(",");
				
				if( name.equals("OBJECT") ) 
					writeObject( start , reader , writer );
				else
				if( name.equals("ARRAY"))
					writeArray( start , reader ,  writer );
				else if(name.equals("MEMBER") )
					writeMember( start , reader , writer );
				else if( name.equals("NUMBER"))
					writeNumber( start , reader , writer );
				else if( name.equals("BOOLEAN"))
					writeBoolean( start , reader , writer );
				else if( name.equals("NULL") )
					writeNull( reader , writer );
				else if( name.equals("STRING"))
					writeString( start , reader , writer );
				else
					readToEnd(reader);
				return true ;
				
			}
			else
			if( e.isEndElement() )
				return false ;
		}
		return false ;
	}

	private void writeString(StartElement start, XMLEventReader reader, PrintWriter writer) throws XMLStreamException {
		String chars = readChars( reader );
		writer.print( JSONObject.quote(chars) );
		readToEnd(reader);
		
	}

	private void writeNull( XMLEventReader reader, PrintWriter writer) throws XMLStreamException {
		writer.print("null");
		readToEnd(reader);
	}

	private void writeBoolean(StartElement start, XMLEventReader reader, PrintWriter writer) throws XMLStreamException {
		writeNumber( start , reader , writer );
		
	}

	private void writeNumber(StartElement start, XMLEventReader reader, PrintWriter writer) throws XMLStreamException {
		
		String chars = readChars( reader );
		writer.print( chars );
		readToEnd(reader);
		
		
	}

	private void writeMember(StartElement start, XMLEventReader reader, PrintWriter writer) throws XMLStreamException {
		String name = start.getAttributeByName( new QName("name")).getValue();
		writer.print( JSONObject.quote(name) );
		writer.print(":");
		
		if( parse( reader , writer ,false))
			readToEnd(reader);
		
		
	}

	private void writeArray(StartElement start, XMLEventReader reader, PrintWriter writer) throws XMLStreamException {
		writer.print("[");
		boolean bFirst = true ;
		do {

			if( ! parse( reader , writer , ! bFirst  ) )
				break ;
			bFirst = false ;
		}
		while( true  ) ;
		writer.print("]");
			

		
	}

	private void writeObject(StartElement start, XMLEventReader reader, PrintWriter writer) throws XMLStreamException {

		writer.print("{");
		boolean bFirst = true ;
		do {

			if( ! parse( reader , writer , ! bFirst  ) )
				break ;
			bFirst = false ;
		}
		while( true  ) ;
		writer.print("}");
		
	}

	private void readToEnd(XMLEventReader reader) throws XMLStreamException {
		while( reader.hasNext() && ! reader.peek().isEndElement() )
			reader.nextEvent();
		
		if( reader.hasNext())
			reader.nextEvent();
		
	}

	private String readChars(XMLEventReader reader) throws XMLStreamException {

		StringBuffer sb = new StringBuffer();
		while( reader.hasNext() && reader.peek().isCharacters() ){
			Characters ch = reader.nextEvent().asCharacters();
			sb.append( ch.getData() );
			
		}
		return sb.toString();

	}
	
	
	
			
	
}

//
//
//Copyright (C) 2008,2009,2010 , David A. Lee.
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
