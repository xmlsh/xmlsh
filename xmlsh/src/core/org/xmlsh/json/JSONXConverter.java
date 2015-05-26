/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.json;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.xml.crypto.dsig.TransformException;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import net.sf.saxon.s9api.SaxonApiException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.util.Util;

import com.fasterxml.jackson.core.JsonToken;

public class JSONXConverter extends JXConverter
{
	static final String	 kJXML_URI	     = "http://www.ibm.com/xmlns/prod/2009/jsonx";
	static final QName	 kATTR_NAME	     = new QName("name");
	static final QName	 kELEM_OBJECT	 = new QName(kJXML_URI, "object");	 // A JSON Object
	static final QName	 kELEM_STRING	 = new QName(kJXML_URI, "string");	 // A JSON STRING
	static final QName	 kELEM_NUMBER	 = new QName(kJXML_URI, "number");	 // A  JSON NUMBER
	static final QName	 kELEM_ARRAY	 = new QName(kJXML_URI, "array");	 // A  JSON ARRAY
	static final QName	 kELEM_BOOLEAN	 = new QName(kJXML_URI, "boolean");	 // A JSON Literal (true,false)
	static final QName	 kELEM_NULL	     = new QName(kJXML_URI, "null");	 // A JSON Literal null
	public static Logger	mLogger	     = LogManager.getLogger();

	class JConverter extends JSONConverter
	{


		protected JConverter(XMLStreamReader reader, OutputStream os) throws ConverterException 
		{
			super(reader, os);
		}

		@Override
		public boolean startElement(StartElement start, QName name) throws ConverterException
		{
			try {

				if(name.equals(kELEM_OBJECT))
					writeObject(start);
				else if(name.equals(kELEM_ARRAY))
					writeArray(start);
				else if(name.equals(kELEM_NUMBER))
					writeNumber(start);
				else if(name.equals(kELEM_BOOLEAN))
					writeBoolean(start);
				else if(name.equals(kELEM_NULL))
					writeNull(start);
				else if(name.equals(kELEM_STRING))
					writeString(start);
				else
					throw new ConverterException("Depreciated element not supported: " + start.toString());
			} catch (ConverterException e) {
				throw e;
			} catch (Exception e) {
				Util.wrapException(e, ConverterException.class);
			}
			return true;
		}

		private void writeString(StartElement start) throws XMLStreamException, UnsupportedEncodingException,
		FileNotFoundException, IOException, TransformException,
		SaxonApiException, CoreException
		{
			writeNameField( start );
			String chars = readString();
			mGenerator.writeString(chars);

		}


		private void writeNameField(StartElement start) throws IOException
		{
			String name = getAttr( start , kATTR_NAME );
			if( ! Util.isBlank(name))
				mGenerator.writeFieldName(name);

		}

		private String getAttr(StartElement start, QName attr)
		{
			Attribute a = start.getAttributeByName(attr);
			if(a == null)
				return null;
			return a.getValue();
		}

		private void writeNull(StartElement start) throws IOException, ConverterException, XMLStreamException
		{
			writeNameField( start );
			mGenerator.writeNull();
		}

		private void writeBoolean(StartElement start) throws XMLStreamException, IOException, ConverterException
		{
			writeNameField( start );
			String chars = readString();
			chars = chars.trim();
			mGenerator.writeBoolean(Util.parseBoolean(chars));
		}

		private void writeNumber(StartElement start) throws ConverterException, IOException, XMLStreamException
		{

			writeNameField( start );

			String chars = readString();
			chars = chars.trim();
			mGenerator.writeNumber(chars);
		}


		private void writeArray(StartElement start) throws IOException, ConverterException
		{
			writeNameField(start);
			mGenerator.writeStartArray();
			do {

				if(!parse())
					break;
			} while (true);
			mGenerator.writeEndArray();

		}

		private void writeObject(StartElement start) throws IOException, ConverterException
		{
			writeNameField(start);
			mGenerator.writeStartObject();
			do {

				if(!parse())
					break;
			} while (true);
			mGenerator.writeEndObject();

		}



		@Override
		protected
		boolean startDocument(XMLEvent e) throws ConverterException
		{
			return true;

		}

		@Override
		protected
		boolean endElement(EndElement asEndElement) throws ConverterException
		{

			return false;
		}

		@Override
		protected
		boolean endDocument(XMLEvent e) throws ConverterException
		{
			return false;
		}

		@Override
		protected
		boolean characters(XMLEvent e)
		{
			// Ignore unexpected chars
			return true;
		}


	}

	class XConverter extends XMLConverter
	{

		private String mFieldName;


		protected XConverter(InputStream is, XMLStreamWriter sw) throws ConverterException
		{
			super(is, sw);
		}

		// Write a value and

		@Override
		void writeArray() throws ConverterException
		{

			try {
				JsonToken tok;
				writeStartElement( kELEM_ARRAY );
				writeFieldAttr();
				while ((tok = nextToken()) != null && tok != JsonToken.END_ARRAY) {
					writeValue(tok);
				}
				writeEndElement();
			} catch (XMLStreamException e) {
				throw new ConverterException(e);
			}
		}

		@Override
		void writeObject() throws ConverterException
		{

			JsonToken tok;
			try {

				writeStartElement( kELEM_OBJECT );
				writeFieldAttr();
				while ((tok = nextToken()) == JsonToken.FIELD_NAME )
					writeMember();
				if(tok == null)
					throw new ConverterException("Unexpected EOF");

				if(tok != JsonToken.END_OBJECT)
					throw new ConverterException("Unexpected token: " + tok);
				writeEndElement();
			} catch (XMLStreamException e) {
				throw new ConverterException(e);
			}
		}

		private void writeMember() throws ConverterException
		{
			try {
				mFieldName = mParser.getCurrentName();
				writeValue(nextToken());
			} catch (IOException e) {
				throw new ConverterException(e);
			}
		}

		@Override
		void writeBoolean(boolean value) throws ConverterException
		{

			try {

				writeStartElement( kELEM_BOOLEAN );
				writeFieldAttr();
				writeCharacters(value ? "true" : "false");
				writeEndElement();

			} catch (XMLStreamException e) {
				throw new ConverterException(e);
			}
		}

		private void writeFieldAttr() throws XMLStreamException
		{
			if( mFieldName != null ) {
				writeAttribute(kATTR_NAME, mFieldName);
				mFieldName = null ;
			}

		}

		@Override
		void writeNull() throws ConverterException
		{
			try {
				writeStartElement(kELEM_NULL);
				writeFieldAttr();
				writeEndElement();
			} catch (XMLStreamException e) {
				throw new ConverterException(e);
			}

		}

		@Override
		void writeNumber() throws ConverterException
		{
			try {
				writeStartElement(kELEM_NUMBER);
				writeFieldAttr();
				writeCharacters( getStringValue() ); 
				writeEndElement();
			} catch (XMLStreamException e) {
				throw new ConverterException(e);
			}

		}


		@Override
		void writeString(String s) throws ConverterException
		{

			try {
				writeStartElement(kELEM_STRING);
				writeFieldAttr();
				writeCharacters(s);

				writeEndElement();
			} catch (XMLStreamException e) {
				throw new ConverterException(e);
			}
		}

	}

	public JSONXConverter(SerializeOpts serializeOpts, List<XValue> mArgs)
	{
		super( serializeOpts, mArgs);
	}

	@Override
	JSONConverter newJConverter(XMLStreamReader reader, OutputStream os) throws ConverterException
	{
		return new JConverter(reader, os);
	}

	@Override
	XMLConverter newXMLConverter(InputStream is, XMLStreamWriter sw) throws ConverterException
	{
		return new XConverter(is, sw);

	}

}

/*
 * Copyright (C) 2008-2012 David A. Lee.
 * 
 * The contents of this file are subject to the "Simplified BSD License" (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.opensource.org/licenses/bsd-license.php
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * 
 * The Original Code is: all this file.
 * 
 * The Initial Developer of the Original Code is David A. Lee
 * 
 * Portions created by (your name) are Copyright (C) (your legal entity). All
 * Rights Reserved.
 * 
 * Contributor(s): David A. Lee
 */