/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.commands.test;

import java.io.IOException;
import java.util.List;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.xmlsh.core.CoreException;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;

public class xevent extends XCommand {

	public xevent() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public int run(List<XValue> args) throws Exception {
		
		
		
		Options opts = new Options( "r,s,n:" );
		opts.parse(args);
		
		if( opts.hasOpt("r"))
			receive();
		else
		if( opts.hasOpt("s")){
			int n = Integer.parseInt(opts.getOptValue("n").toString());
			send(n);
		} else
			usage();
		
		return 0;
			
		}



	private void send(int n) throws InvalidArgumentException, XMLStreamException, IOException {
		
		XMLStreamWriter	writer = getStdout().asXMLStreamWriter(getSerializeOpts());
		writer.writeStartDocument();
		writer.writeStartElement("root");
		while( n-- >0 ){
			writer.writeStartElement("data");
			writer.writeAttribute("attr", "value");
			writer.writeCharacters("charactors");
			writer.writeEndElement();
			
		}
		writer.writeEndElement();
		writer.writeStartDocument();
		writer.close();
		
		
		
		
		
	}

	private void receive() throws CoreException, IOException, XMLStreamException {
		XMLEventReader reader = getStdin().asXMLEventReader(this.getSerializeOpts());
		while( reader.hasNext())
			reader.nextEvent();
		reader.close();
		
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
