/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.twitter.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONString;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.XCommand;
import org.xmlsh.util.Util;

public abstract class TwitterCommand extends XCommand {
	
	protected static final String kTWITTER_NS = "http://www.xmlsh.org/schemas/twitter";

	
	protected XMLStreamWriter mWriter;

	protected final static String sCOMMON_OPTS = "c=connect:,u=user:,p=password:";
	public TwitterCommand() {
		super();
	}

	
	
	
	protected void closeWriter() throws XMLStreamException {
		mWriter.flush();
		mWriter.close();
	}


	protected void endDocument() throws XMLStreamException {
		mWriter.writeEndDocument();
	}


	protected void endElement() throws XMLStreamException {
		mWriter.writeEndElement();
	}


	protected void startElement(String localname) throws XMLStreamException {
		mWriter.writeStartElement(localname);
	}


	protected void startDocument() throws XMLStreamException {
		mWriter.writeStartDocument();
	}
	
	protected	  void attribute( String localName , String value ) throws XMLStreamException
	{
		mWriter.writeAttribute(localName, value);
	}
	
	protected	  void characters( String value ) throws XMLStreamException
	{
		mWriter.writeCharacters(value);
	}




	String formatXSDateTime(Date date) {
		if( date == null )
			date = new Date();
		
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SS'Z'");
		df.setTimeZone( TimeZone.getTimeZone("UTC") );
			
			
		
		return df.format(date);
	}
	
	protected String httpGetString( String sURL ) throws IOException
	{
		URL url = new URL( sURL  );
		InputStream is = url.openStream();
		String s = Util.readString(is, "UTF8");
		is.close();
		return s;
		
		
	}
	
	
	


	protected void writeJSON(JSONObject obj) throws XMLStreamException,
	InvalidArgumentException, JSONException {
		
		
		Iterator<?> keys = obj.keys();

		while (keys.hasNext()) {

			Object k = keys.next();
			String name = k.toString();
			startElement( toNCName( name ));

			Object v = obj.get(name);
			writeJSON(v);
			endElement();

		}


	}

	private String toNCName(String name) {
		return name  ;
	}




	private void writeJSONBoolean(Boolean b) throws XMLStreamException {
		
		// writer.writeAttribute("value", b.booleanValue() ? "true" : "false");
		characters(b.booleanValue() ? "true" : "false");


	}


	private void writeJSONNull( ) throws XMLStreamException {

	}
	private void writeJSON(Number v) throws XMLStreamException, JSONException {
		characters( JSONObject.numberToString(v));
		

	}

	private void writeJSON(String s) throws XMLStreamException {

		characters(s);

	}

	private void writeJSON(JSONArray array) throws XMLStreamException,
	JSONException, InvalidArgumentException {

		int len = array.length();



		for (int i = 0; i < len; i += 1) {

			Object v = array.get(i);
			startElement("item");
			writeJSON(v);
			endElement();
		}

	}

	private void writeJSON(Object object) throws XMLStreamException,
	InvalidArgumentException, JSONException {
		if (object instanceof JSONObject) {
			writeJSON((JSONObject) object);

		} else if (object instanceof JSONArray) {
			writeJSON((JSONArray) object);
		} else if (object instanceof JSONString) {

			writeJSON(((JSONString) object).toJSONString());
		} else if (object instanceof Number) {
			writeJSON((Number) object);
		} else if (object instanceof Boolean) {
			writeJSONBoolean(((Boolean) object));
		} else if( object == JSONObject.NULL )
			writeJSONNull(   );
		else if( object instanceof String )
			writeJSON( (String) object  );
		else
			throw new InvalidArgumentException("Unknown type for JSON value: "
					+ object.getClass().toString());

	}

	
}

//
//
// Copyright (C) 2008-2012  David A. Lee.
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
