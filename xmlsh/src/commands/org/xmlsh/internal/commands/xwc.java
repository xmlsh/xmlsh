/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.internal.commands;

import java.util.List;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.xmlsh.core.InputPort;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.core.io.OutputPort;
import org.xmlsh.sh.shell.SerializeOpts;

public class xwc extends XCommand {

	private int  counts[] = new int[ XMLStreamConstants.ENTITY_DECLARATION ] ;


	@Override
	public int run(List<XValue> args) throws Exception {

		Options opts = new Options( SerializeOpts.getOptionDefs() );
		opts.parse(args);
		args = opts.getRemainingArgs();


		InputPort stdin = null;
		if( args.size() > 0 )
			stdin = getInput( args.get(0));
		else
			stdin = getStdin();
		if( stdin == null )
			throw new InvalidArgumentException("Cannot open input");
		OutputPort stdout = getStdout();
		if( stdout == null ){
			throw new InvalidArgumentException("Cannot open input");
		}
		try {

			setSerializeOpts(opts);

			XMLStreamReader reader = stdin.asXMLStreamReader(getSerializeOpts());
			count(reader);
			reader.close();

			XMLStreamWriter writer =stdout.asXMLStreamWriter(getSerializeOpts());
			output(writer);
			writer.flush();
			writer.close();

		} 
		finally {

		}
		return 0;


	}

	private void output(XMLStreamWriter writer) throws XMLStreamException {
		writer.writeStartDocument();
		writer.writeStartElement("xwc");
		write( writer , "start-element" , counts[ XMLStreamConstants.START_ELEMENT-1]);
		write( writer , "end-element" , counts[ XMLStreamConstants.END_ELEMENT-1]);
		write( writer , "processing-instruction" , counts[ XMLStreamConstants.PROCESSING_INSTRUCTION-1] );
		write( writer , "characters" , counts[ XMLStreamConstants.CHARACTERS-1] );
		write( writer , "comment" , counts[ XMLStreamConstants.COMMENT-1] );
		write( writer , "space" , counts[ XMLStreamConstants.SPACE-1] );
		write( writer , "start-document" , counts[ XMLStreamConstants.START_DOCUMENT-1] );
		write( writer , "end-document" , counts[ XMLStreamConstants.END_DOCUMENT-1] );
		write( writer , "entity-reference" , counts[ XMLStreamConstants.ENTITY_REFERENCE-1] );
		write( writer , "attribute" , counts[ XMLStreamConstants.ATTRIBUTE-1] );
		write( writer , "dtd" , counts[ XMLStreamConstants.DTD-1] );
		write( writer , "cdata" , counts[ XMLStreamConstants.CDATA-1] );
		write( writer , "namespace" , counts[ XMLStreamConstants.NAMESPACE-1] );
		write( writer , "notation-decl" , counts[ XMLStreamConstants.NOTATION_DECLARATION-1] );
		write( writer , "entity-decl" , counts[ XMLStreamConstants.ENTITY_DECLARATION-1] );

		writer.writeEndElement();
		writer.writeEndDocument();

	}

	private void write(XMLStreamWriter writer, String localName, int n) throws XMLStreamException {
		writer.writeStartElement(localName);

		writer.writeCharacters( String.valueOf(n));
		writer.writeEndElement();
	}

	private void count(XMLStreamReader reader) throws XMLStreamException {
		while( reader.hasNext()){
			int event = reader.next();
			counts[ event-1]++;
			if( event == XMLStreamConstants.START_ELEMENT ){
				counts[ XMLStreamConstants.ATTRIBUTE -1 ] += reader.getAttributeCount();

			}
		}
	}

}



//
//
//Copyright (C) 2008-2014    David A. Lee.
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
