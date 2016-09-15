/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.json;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

import javanet.staxutils.OutputFactory;

import javax.xml.crypto.dsig.TransformException;
import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.WhitespaceStrippingPolicy;
import net.sf.saxon.s9api.XdmNode;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.builtin.commands.exit;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.sh.shell.ShellConstants;
import org.xmlsh.util.Util;
import org.xmlsh.util.XMLUtils;

import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.json.JsonWriteContext;

public class JXONConverter extends JXConverter
{
	static final String	 kJXML_URI	     = "http://www.xmlsh.org/jxml";
	static final QName	 kATTR_ENCODING	 = new QName("encoding");
	static final QName	 kATTR_NAME	     = new QName("name");
	static final QName	 kATTR_VALUE	 = new QName("value");
	static final QName	 kATTR_UNWRAP	 = new QName("unwrap");
	static final QName	 kATTR_HTML	     = new QName("html");	 // A String formated as XHTML
	static final QName	 kELEM_XJSON	 = new QName(kJXML_URI, "xjson");
	static final QName	 kELEM_FILE	     = new QName(kJXML_URI, "file");
	static final QName	 kELEM_OBJECT	 = new QName(kJXML_URI, "object");	 // A JSON Object
	static final QName	 kELEM_MEMBER	 = new QName(kJXML_URI, "member");	 // A JSON Object Member
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

			mLogger.entry(start, name);
			try {
			
				if(name.equals(kELEM_XJSON)) {
					while (parse())
						;
					
					return  mLogger.exit( false);
				} else if(name.equals(kELEM_FILE))
					throw new ConverterException("Depreciated element not supported: " + kELEM_FILE.toString());
				else

					if(name.equals(kELEM_OBJECT))
						writeObject(start);
					else if(name.equals(kELEM_ARRAY))
						writeArray(start);
					else if(name.equals(kELEM_MEMBER))
						writeMember(start);
					else if(name.equals(kELEM_NUMBER))
						writeNumber(start);
					else if(name.equals(kELEM_BOOLEAN))
						writeBoolean(start);
					else if(name.equals(kELEM_NULL))
						writeNull();
					else if(name.equals(kELEM_STRING))
						writeString(start);
					else
						throw new ConverterException("Depreciated element not supported: " + kELEM_FILE.toString());
			} catch (ConverterException e) {
				throw e;
			} catch (Exception e) {
				Util.wrapException(e, ConverterException.class);
			} 
			return mLogger.exit(true );
			
		}

		private void writeString(StartElement start) throws XMLStreamException, UnsupportedEncodingException,
		FileNotFoundException, IOException, TransformException,
		SaxonApiException, CoreException
		{
			mLogger.entry(start );
			
			String value = getAttr(start, kATTR_VALUE);
			@SuppressWarnings("unused")
			String encoding = getAttr(start, kATTR_ENCODING);
			String unwrap = getAttr(start, kATTR_UNWRAP);
			String html = getAttr(start, kATTR_HTML);
			String chars;
			if(value != null)
				chars = value;
			else {
				// readString eats the close tag
				chars = readString(Util.parseBoolean(html));

			}

			// If Unwrap then trim off <html> and leading and trailing blanks
			if(Util.parseBoolean(unwrap)) {
				chars = unwrap(chars);

			}

			mGenerator.writeString(chars);
			
			mLogger.trace("writeString({}) leaving. prevLevel: {} level: {} " , chars );

			
			mLogger.exit(   );
			

		}

		/*
		 * Parse an HTML element as XML and reserialize as HTML, store as a JSON
		 * string
		 */

		private String readString(boolean bHTML) throws TransformException, XMLStreamException, SaxonApiException,
		IOException
		{ 
			mLogger.entry(bHTML);

			byte[] bytes = bHTML ? serializeAsXML() : serializeAsString();

			// String xs = new String(xhtml,klENCODING_UTF_8);
			if(bHTML)
				return mLogger.exit( formatAsHtml(bytes));
			else
				return  mLogger.exit(new String(bytes, ShellConstants.kENCODING_UTF_8));

		}

		/*
		 * Unwrap a string by 1) Remove leading and trailing blanks 2) Remove
		 * any <html> (any case) from beginning and end 3) Remove leading and
		 * trailing blanks from the result
		 */
		private String unwrap(String value)
		{
			value = value.trim();
		    if(  value.length() < 6 || ! "<html>".equalsIgnoreCase(value.substring(0, 6)))
		        return value ;
			
			value = value.substring(6);
			if(value.length() < 7 ||!  "</html>".equalsIgnoreCase(value.substring(value.length() - 7)))
				return value ;
			
			value = value.substring(0, value.length() - 7);
			return value.trim();

		}

		private String getAttr(StartElement start, QName attr)
		{
			Attribute a = start.getAttributeByName(attr);
			if(a == null)
				return null;
			return a.getValue();
		}

		private void writeNull() throws IOException, ConverterException
		{
			mLogger.entry(  );
			mGenerator.writeNull();
			mLogger.exit( );

		}

		private void writeBoolean(StartElement start) throws XMLStreamException, IOException, ConverterException
		{

			mLogger.entry(start);
			
			String chars;
			Attribute v = start.getAttributeByName(kATTR_VALUE);

			if(v != null) 
				chars = v.getValue(); 
			else {
				chars = readString();
			}
			chars = chars.trim();
			mGenerator.writeBoolean(Util.parseBoolean(chars));
			
			mLogger.exit(  );

		}

		private void writeNumber(StartElement start) throws ConverterException, IOException, XMLStreamException
		{
			mLogger.entry(start);
			
			String chars;
			Attribute v = start.getAttributeByName(kATTR_VALUE);

			if(v != null)
				chars = v.getValue();
				else {
				chars = readString();
				}

			chars = chars.trim();

			//	Number num = NumberFormat.getInstance().parse(chars);

			mGenerator.writeNumber(chars);

			mLogger.exit();
		}

		private void writeMember(StartElement start) throws IOException, ConverterException
		{

			mLogger.entry(start);
			String name = start.getAttributeByName(kATTR_NAME).getValue();
			mGenerator.writeFieldName(name);
			parse();
			mLogger.exit();

		}

		private void writeArray(StartElement start) throws IOException, ConverterException
		{

			mLogger.entry( start  );			
			mGenerator.writeStartArray();
			do {

				if(!parse())
					break;
			} while (true);
			mGenerator.writeEndArray();
			mLogger.exit();

		}

		private void writeObject(StartElement start) throws IOException, ConverterException
		{
			
			mLogger.entry( start );
			mGenerator.writeStartObject();
			do {

				if(!parse())
					break;
			} while (true);
			mGenerator.writeEndObject();
			mLogger.exit();
			
		}

		/*
		 * Serialize the body as HTML and return as a string
		 */

		private String formatAsHtml(byte[] xhtml) throws SaxonApiException, IOException
		{

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			Serializer ser = Shell.getProcessor().newSerializer();
			ser.setOutputProperty(Serializer.Property.OMIT_XML_DECLARATION, "yes");
			ser.setOutputProperty(Serializer.Property.INDENT, "no");

			ser.setOutputProperty(Serializer.Property.METHOD, "html");
			ser.setOutputProperty(Serializer.Property.ENCODING, ShellConstants.kENCODING_UTF_8);
			ser.setOutputStream(bos);

			Processor processor = Shell.getProcessor();
			DocumentBuilder builder = processor.newDocumentBuilder();
			builder.setWhitespaceStrippingPolicy(WhitespaceStrippingPolicy.ALL);
			XdmNode node = builder.build(new StreamSource(new ByteArrayInputStream(xhtml)));
			processor.writeXdmValue(node, ser);
			return bos.toString(ShellConstants.kENCODING_UTF_8).trim();

		}

		/*
		 * Serialize as XML
		 */
		private byte[] serializeAsXML() throws XMLStreamException, UnsupportedEncodingException, FactoryConfigurationError, IOException 
		{

			mLogger.entry( );
			
	
			XMLEventWriter writer = null;
			XMLEventReader reader = null;
			
			try {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				XMLOutputFactory ofact = new OutputFactory();
				writer = ofact.createXMLEventWriter(bos, ShellConstants.kENCODING_UTF_8) ; 
			    reader = XMLUtils.createEventReader( readString() );
				writer.add(reader);
				writer.flush();
				return mLogger.exit( bos.toByteArray() );
			} finally {
				Util.safeClose(reader);
				Util.safeClose(writer);
			}


		}

		private byte[] serializeAsString() throws XMLStreamException, UnsupportedEncodingException, IOException
		{
			
			String s = readString();
			return s.getBytes(ShellConstants.kENCODING_UTF_8);

		}

		@Override
		protected
		boolean startDocument(XMLEvent e) throws ConverterException
		{
			mLogger.entry( e );
			return mLogger.exit(true );


		}

		@Override
		protected
		boolean endElement(EndElement e ) throws ConverterException
		{
			mLogger.entry( e );
			return mLogger.exit(false  );
		}

		@Override
		protected
		boolean endDocument(XMLEvent e) throws ConverterException
		{
			mLogger.entry( e);
			return mLogger.exit(false );		}

		@Override
		protected
		boolean characters(XMLEvent e)
		{
			// Ignore unexpected chars
			return mLogger.exit(true );
		}

	}

	class XConverter extends XMLConverter
	{

		protected XConverter(InputStream is, XMLStreamWriter sw) throws ConverterException
		{
			super(is, sw);
		}

		// Write a value and

		@Override
		void writeArray() throws ConverterException
		{

			mLogger.entry( );
			
			try {
				JsonToken tok;
				writeStartElement( kELEM_ARRAY );


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

			mLogger.entry( );
			
			JsonToken tok;

			try {
				writeStartElement( kELEM_OBJECT );

				while ((tok = nextToken()) == JsonToken.FIELD_NAME)
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
			mLogger.entry( );
			
			try {
				writeStartElement( kELEM_MEMBER );
				String name = mParser.getCurrentName();
				writeAttribute( kATTR_NAME , name );

				writeValue(nextToken());
				writeEndElement();
			} catch (XMLStreamException e) {
				throw new ConverterException(e);
			} catch (IOException e) {
				throw new ConverterException(e);
			}
		}

		@Override
		void writeBoolean(boolean value) throws ConverterException
		{
			mLogger.entry(value);
			

			try {

				writeStartElement( kELEM_BOOLEAN );
				writeCharacters(value ? "true" : "false");
				writeEndElement();

			} catch (XMLStreamException e) {
				throw new ConverterException(e);
			}
		}

		@Override
		void writeNull() throws ConverterException
		{
			mLogger.entry( );
			
			try {
				writeStartElement(kELEM_NULL);
				writeEndElement();
			} catch (XMLStreamException e) {
				throw new ConverterException(e);
			}

		}

		@Override
		void writeNumber() throws ConverterException
		{ 
			mLogger.entry( );
			
			try {
				writeStartElement(kELEM_NUMBER);
				writeCharacters( getStringValue() ); 
				writeEndElement();
			} catch (XMLStreamException e) {
				throw new ConverterException(e);
			}

		}


		@Override
		void writeString(String s) throws ConverterException
		{

			mLogger.entry(s);
			
			try {
				writeStartElement(kELEM_STRING);
				writeCharacters(s);

				writeEndElement();
			} catch (XMLStreamException e) {
				throw new ConverterException(e);
			}
		}

	}

	public JXONConverter( SerializeOpts serializeOpts, List<XValue> mArgs)
	{
		super(serializeOpts, mArgs);
	}

	@Override
	JSONConverter newJConverter(XMLStreamReader reader, OutputStream  os) throws ConverterException
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