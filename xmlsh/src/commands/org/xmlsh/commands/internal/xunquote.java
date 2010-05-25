/**
 * $Id: xecho.java 401 2010-03-18 20:23:55Z daldei $
 * $Date: 2010-03-18 16:23:55 -0400 (Thu, 18 Mar 2010) $
 *
 */

package org.xmlsh.commands.internal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.List;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;
import net.sf.saxon.s9api.XdmValue;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.util.Util;

public class xunquote extends XCommand {
	
	public int run( List<XValue> args ) throws Exception {
		
		Options opts = new Options( "n,p=port:" ,SerializeOpts.getOptionDefs() );
		opts.parse(args);
		
		args = opts.getRemainingArgs();

		SerializeOpts serializeOpts = getSerializeOpts(opts);
		
		boolean nolf = opts.hasOpt("n");
		
		String port = opts.getOptString("p", null);
		
		OutputPort stdout = 
			port != null ? mShell.getEnv().getOutputPort(port) : 
			mShell.getEnv().getStdout();

		if( stdout == null )
			throw new InvalidArgumentException("Output port not found: " + port );
			
	
		
		XMLEventWriter out = stdout.asXMLEventWriter(serializeOpts);

		
		args = Util.expandSequences(args);
		
		
		/*
		 * IF there are any arguments, then treat them as strings and re-parse them to XML
		 * Otherwise use stdin, parse it and output as XML
		 */
		
		boolean bFirst = true;
		if( args.size() > 0 )
			for ( XValue arg : args ){
				if( ! bFirst )
					stdout.writeSequenceSeperator(serializeOpts);
				
				
				ByteArrayOutputStream	tempBuf = new ByteArrayOutputStream(  );
				arg.serialize( tempBuf , serializeOpts );
				ByteArrayInputStream  	in = new ByteArrayInputStream( tempBuf.toByteArray() );
				XMLInputFactory factory = XMLInputFactory.newInstance();
				XMLEventReader reader = factory.createXMLEventReader( in , serializeOpts.getEncoding() );
				
				copyXML( reader , out );
				reader.close();
				bFirst = false;
				
				
		}  else { 
			// Parse Stdin as XML and output
			XMLEventReader reader = getStdin().asXMLEventReader(serializeOpts);
			copyXML( reader , out );
			reader.close();
			
			
			
		}
			
		out.close();
		if( ! nolf )
			stdout.writeSequenceTerminator(serializeOpts);

		return 0;
	}

	private void copyXML(XMLEventReader in, XMLEventWriter out) throws XMLStreamException {
		while( in.hasNext() ){
			XMLEvent e = (XMLEvent) in.next();
			out.add(e);
		}
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
