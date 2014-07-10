/**

 * $Id: xpwd.java 21 2008-07-04 08:33:47Z daldei $
 * $Date: 2008-07-04 04:33:47 -0400 (Fri, 04 Jul 2008) $
 *
 */

package org.xmlsh.commands.json;

import org.xmlsh.core.InputPort;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

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

public class json2xml extends XCommand
{
	private static final String	kJXML_URI	  = "http://www.xmlsh.org/jxml";

	private boolean	            bFirstElement	= true;
	private int	                depth	      = 0;

	public int run(List<XValue> args) throws Exception
	{
		Options opts = new Options(SerializeOpts.getOptionDefs());
		opts.parse(args);

		args = opts.getRemainingArgs();

		OutputPort stdout = getStdout();

		mSerializeOpts = getSerializeOpts(opts);
		SerializeOpts inputOpts = mSerializeOpts.clone();
		// JSON is always UTF8
		inputOpts.setInputTextEncoding("UTF-8");

		InputPort in = args.isEmpty() ? this.getStdin() : this.getInput(args.get(0));
		XMLStreamWriter sw = null ;
		Reader inr = null;
		try {
				
			inr = new InputStreamReader(in.asInputStream(inputOpts), inputOpts.getInputTextEncoding());
	
			JsonFactory jsonFactory = new JsonFactory(); // or, for data binding,
														 // org.codehaus.jackson.mapper.MappingJsonFactory
			JsonParser jp = jsonFactory.createParser(inr); // or URL, Stream,
														   // Reader, String, byte[]
	
			/*
			 * Assume JSON file is wrapped by an Object
			 */
	
			sw = stdout.asXMLStreamWriter(mSerializeOpts);
		
			sw.writeStartDocument();

			write(jp, sw);
			sw.writeEndDocument();
		} finally {
		
			if( sw != null ) {
				sw.flush();
				sw.close();
			}
			if( in != null )
			  inr.close();
		}
		return 0;

	}

	private void write(JsonParser parser, XMLStreamWriter writer) throws XMLStreamException, InvalidArgumentException,
	                                                             IOException
	{
		JsonToken tok = parser.nextToken();
		writeValue(tok , parser, writer);
		if( (tok = parser.getCurrentToken()) != null && (tok=parser.nextToken() ) != null )
			throw new InvalidArgumentException("parse complete before EOF: token=" + tok);
	}

	// Write a value and return 
	private void writeValue(JsonToken tok , JsonParser parser, XMLStreamWriter writer) throws XMLStreamException,
	                                                                       InvalidArgumentException, IOException
	{

		if(tok == null)
			return  ;

		switch (tok) {
		case START_ARRAY:

			writeArray(parser, writer);
			break;
		case START_OBJECT:
			writeObject(parser, writer);
			break;

		case VALUE_EMBEDDED_OBJECT:
			break;
		case VALUE_FALSE:
			writeBoolean(false, writer);
			break;
		case VALUE_NULL:
			writeNull(writer);
			break;
		case VALUE_NUMBER_FLOAT:
			write(parser.getFloatValue(), writer);
			break;
		case VALUE_NUMBER_INT:
			write(parser.getIntValue(), writer);
			break;
		case VALUE_STRING:
			write(parser.getText(), writer);
			break;
		case VALUE_TRUE:
			writeBoolean(false, writer);
			break;
		default:
			break;
		}


	}

	private void writeArray(JsonParser parser, XMLStreamWriter writer) throws XMLStreamException, IOException,
	                                                                       InvalidArgumentException
	{

		JsonToken tok;
		writer.writeStartElement("", "array", kJXML_URI);

		 while( (tok = parser.nextToken()) != null && tok != JsonToken.END_ARRAY) {
			writeValue( tok , parser, writer );
		}
		writer.writeEndElement();
	}

	private void writeObject(JsonParser parser, XMLStreamWriter writer) throws XMLStreamException, IOException,
	                                                                        InvalidArgumentException
	{

		JsonToken tok;

		writer.writeStartElement("", "object", kJXML_URI);

		while ((tok = parser.nextToken()) == JsonToken.FIELD_NAME)
			writeMember(parser, writer);
		
		if( tok == null )
			throw new IOException("Unexpected EOF");

		if(tok != JsonToken.END_OBJECT)
			throw new InvalidArgumentException("Unexpected token: " + tok);
		writer.writeEndElement();
	}

	private void writeMember(JsonParser parser, XMLStreamWriter writer) throws XMLStreamException, IOException,
	                                                                        InvalidArgumentException
	{
		writer.writeStartElement("", "member", kJXML_URI);
		String name = parser.getCurrentName();
		writer.writeAttribute("name", name);
		
		writeValue(parser.nextToken() , parser, writer);
		writer.writeEndElement();
	}

	private void writeBoolean(Boolean b, XMLStreamWriter writer) throws XMLStreamException
	{
		writer.writeStartElement("", "boolean", kJXML_URI);
		// writer.writeAttribute("value", b.booleanValue() ? "true" : "false");
		writer.writeCharacters(b.booleanValue() ? "true" : "false");
		writer.writeEndElement();
	}

	private void writeNull(XMLStreamWriter writer) throws XMLStreamException
	{
		writer.writeStartElement("", "null", kJXML_URI);
		writer.writeEndElement();

	}

	private void write(int v, XMLStreamWriter writer) throws XMLStreamException
	{
		writer.writeStartElement("", "number", kJXML_URI);
		writer.writeCharacters(Integer.toString(v));
		writer.writeEndElement();

	}

	private void write(float v, XMLStreamWriter writer) throws XMLStreamException
	{
		writer.writeStartElement("", "number", kJXML_URI);
		writer.writeCharacters(Float.toString(v));
		writer.writeEndElement();

	}

	private void write(String s, XMLStreamWriter writer) throws XMLStreamException
	{

		writer.writeStartElement("", "string", kJXML_URI);
		writer.writeCharacters(s);
		writer.writeEndElement();

	}

}

//
//
// Copyright (C) 2008-2014 David A. Lee.
//
// The contents of this file are subject to the "Simplified BSD License" (the
// "License");
// you may not use this file except in compliance with the License. You may
// obtain a copy of the
// License at http://www.opensource.org/licenses/bsd-license.php
//
// Software distributed under the License is distributed on an "AS IS" basis,
// WITHOUT WARRANTY OF ANY KIND, either express or implied.
// See the License for the specific language governing rights and limitations
// under the License.
//
// The Original Code is: all this file.
//
// The Initial Developer of the Original Code is David A. Lee
//
// Portions created by (your name) are Copyright (C) (your legal entity). All
// Rights Reserved.
//
// Contributor(s): none.
//
