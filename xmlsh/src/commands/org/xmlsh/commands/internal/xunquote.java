/**
 * $Id: xecho.java 401 2010-03-18 20:23:55Z daldei $
 * $Date: 2010-03-18 16:23:55 -0400 (Thu, 18 Mar 2010) $
 *
 */

package org.xmlsh.commands.internal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.util.Util;

public class xunquote extends XCommand {
	
	static class UnquotingInputStream extends InputStream
	{
		private		byte[]  	mBytes;
		int			pos = 0;
		int 		end = 0;
		
		
		public UnquotingInputStream(byte[] bytes) {
			super();
			mBytes = bytes;
			end = mBytes.length;
		}

		
		private int next()
		{
			if( pos >= end )
				return -1;
			return mBytes[pos++];
		}

		private String readToSemi()
		{
			StringBuffer sb = new StringBuffer();
			int c ;
			while( ( c = next()) >= 0  && c != ';')
				sb.append((char)c);
			return sb.toString();
			
		}
		

		@Override
		public int read() throws IOException {
			int c = next();
			if( c == -1 )
				return c;
			if( c == '&' ){
				String s = readToSemi();
				if( s.length() == 0 )
					return -1;
				if( s.equals("lt"))
					return '<' ;
				if( s.equals("gt"))
					return '>';
				if( s.equals("quot"))
					return '"' ;
				if( s.equals("apos"))
					return '\'';
				if( s.equals("amp"))
					return '&';
				
				if( s.startsWith("#")){
					if( s.charAt(1) == 'x' )
						return Integer.parseInt(s.substring(2), 16);
					else
						return Integer.parseInt(s.substring(1), 10);
				}
				else 
					throw new IOException("Unexepcted entity: &" + s + ";");
					
			} else
				return c;
			
		}
		
	};
	
	
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
				UnquotingInputStream  	in = new UnquotingInputStream( tempBuf.toByteArray() );
				
				XMLInputFactory factory = XMLInputFactory.newInstance();
				XMLEventReader reader = factory.createXMLEventReader( in , serializeOpts.getInputXmlEncoding() );
				
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
//Copyright (C) 2008,2009,2010,2011 , David A. Lee.
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
