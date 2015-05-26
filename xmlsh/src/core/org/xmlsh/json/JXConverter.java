/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.builtin.commands.exit;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.util.Util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

public abstract class JXConverter {
	/*
	 * A class helper/config for JSON to XML conversions
	 */

	static Logger mLogger = LogManager.getLogger();

	private SerializeOpts mSerializeOpts;
	private List<XValue> mArgs;

	SerializeOpts getSerializeOpts() {
		return mSerializeOpts;
	}

	interface IJSONConverter {

		public abstract boolean parse() throws ConverterException;

		public void close() throws ConverterException;

	}

	interface IXMLConverter {

		public abstract boolean parse() throws ConverterException;

		public void close() throws ConverterException;

	}

	// Private class for converting FROM JSON
	abstract class JSONConverter implements IJSONConverter {
		XMLEventReader mReader;
		XMLStreamReader mStreamReader;

		JsonGenerator mGenerator;

		protected JSONConverter(XMLStreamReader reader, OutputStream os)
				throws ConverterException {
			super();
			mStreamReader = reader;

			try {
				mGenerator = JSONUtils.createGenerator(os, getSerializeOpts());
				mReader = XMLInputFactory.newInstance().createXMLEventReader(
						mStreamReader);
			} catch (XMLStreamException e) {
				throw new ConverterException(e);

			} catch (FactoryConfigurationError e) {
				throw new ConverterException(e);
			} catch (IOException e) {
				throw new ConverterException(e);

			}
		}

		/*
		 * Returns TRUE if the end of this element/doc is reached 
		 * 
		 * @see org.xmlsh.json.IJSONConverter#parse()
		 */
		@Override
		public boolean parse() throws ConverterException {
			mLogger.entry();

			try {
				boolean nested = true ;
				while (nested && mReader.hasNext()) {
					XMLEvent e = mReader.nextEvent();
					nested = dispatch(e);
				}
			} catch (Exception e) {

				Util.wrapException(e, ConverterException.class);

			}
			return mLogger.exit(false);

		}

		private boolean dispatch(XMLEvent e) throws ConverterException {
			boolean nested = false ;
			if (e.isStartDocument())
				nested = startDocument(e);
			else
			if (e.isStartElement()) {
				StartElement start = e.asStartElement();
				QName name = start.getName();
				nested = startElement(start, name);

			} else if (e.isEndElement()) {
				nested = endElement(e.asEndElement());
			} else if (e.isEndDocument()) {
				nested = endDocument(e);
			} else if (e.isCharacters()) {
				nested = characters(e);
			} else
				throw mLogger.throwing(new ConverterException(
						"Unexpected XML element: " + e.getEventType()));
			
			return nested ; 
		}


		abstract protected boolean startElement(StartElement start, QName name)
				throws ConverterException;

		abstract protected boolean startDocument(XMLEvent e)
				throws ConverterException;;

		abstract protected boolean endElement(EndElement asEndElement)
				throws ConverterException;;

		abstract protected boolean endDocument(XMLEvent e)
				throws ConverterException;;

		abstract protected boolean characters(XMLEvent e);

		protected String readString() throws XMLStreamException,
				UnsupportedEncodingException, IOException {
			mLogger.entry();
			String s =  mReader.getElementText();
			return mLogger.exit(s);

		}

		@Override
		public void close() {

			mLogger.entry();

			JSONUtils.safeClose(mGenerator);
			mGenerator = null;
			Util.safeClose(mReader);
			mReader = null;

		}

	}

	abstract class XMLConverter implements IXMLConverter {
		JsonParser mParser;
		InputStream mInput;
		XMLStreamWriter mWriter;

		protected XMLConverter(InputStream is, XMLStreamWriter sw)
				throws ConverterException {
			mInput = is;
			try {
				mParser = JSONUtils.getJsonFactory().createParser(is);
			} catch (JsonParseException e) {
				throw new ConverterException(e);
			} catch (IOException e) {
				throw new ConverterException(e);
			}
			mWriter = sw;
		}

		@Override
		public boolean parse() throws ConverterException {
			JsonToken tok = nextToken();
			writeValue(tok);
			return true;
		}

		protected JsonToken nextToken() throws ConverterException {
			try {
				return mParser.nextToken();
			} catch (JsonParseException e) {
				throw new ConverterException(e);
			} catch (IOException e) {
				throw new ConverterException(e);
			}
		}

		protected void writeValue(JsonToken tok) throws ConverterException {
			if (tok == null)
				return;

			switch (tok) {
			case START_ARRAY:
				writeArray();
				break;
			case START_OBJECT:
				writeObject();
				break;
			case VALUE_EMBEDDED_OBJECT:
				break;
			case VALUE_FALSE:
				writeBoolean(false);
				break;
			case VALUE_NULL:
				writeNull();
				break;
			case VALUE_NUMBER_FLOAT:
			case VALUE_NUMBER_INT:
				writeNumber();
				break;
			case VALUE_STRING:
				writeString(getStringValue());
				break;
			case VALUE_TRUE:
				writeBoolean(true);
				break;
			default:
				break;
			}

		}

		protected String getStringValue() throws ConverterException {
			try {
				return mParser.getText();
			} catch (IOException e) {
				throw new ConverterException(e);
			}
		}

		protected Number getNumberValue() throws ConverterException {
			try {
				return mParser.getNumberValue();
			} catch (IOException e) {
				throw new ConverterException(e);
			}
		}

		abstract void writeArray() throws ConverterException;

		abstract void writeObject() throws ConverterException;

		abstract void writeBoolean(boolean value) throws ConverterException;

		abstract void writeNull() throws ConverterException;

		abstract void writeNumber() throws ConverterException;

		abstract void writeString(String value) throws ConverterException;

		protected void writeStartElement(QName qn) throws XMLStreamException {
			mWriter.writeStartElement(qn.getPrefix(), qn.getLocalPart(),
					qn.getNamespaceURI());

		}

		protected void writeAttribute(QName qn, String value)
				throws XMLStreamException {
			mWriter.writeAttribute(qn.getPrefix(), qn.getNamespaceURI(),
					qn.getLocalPart(), value);

		}

		protected void writeEndElement() throws XMLStreamException {
			mWriter.writeEndElement();
		}

		protected void writeCharacters(String s) throws XMLStreamException {
			mWriter.writeCharacters(s);
		}

		@Override
		public void close() throws ConverterException {

			try {
				mWriter.flush();
			} catch (XMLStreamException e) {
				throw new ConverterException(e);
			}
			JSONUtils.safeClose(mParser);
			mParser = null;

		}

	}

	abstract IJSONConverter newJConverter(XMLStreamReader reader,
			OutputStream os) throws ConverterException;

	abstract IXMLConverter newXMLConverter(InputStream is, XMLStreamWriter sw)
			throws ConverterException;

	/*
	 * Converter To and/or From JSON
	 */
	public JXConverter(SerializeOpts serializeOpts, List<XValue> args) {
		super();
		mSerializeOpts = serializeOpts;
		mArgs = args;
	}

	public void convertToJson(XMLStreamReader reader, OutputStream os)
			throws ConverterException {

		IJSONConverter converter = newJConverter(new XMLRewritingStreamReader(
				reader), os);
		try {
			converter.parse();

		} finally {
			converter.close();
		}
	}

	public void convertFromJson(InputStream is, XMLStreamWriter sw)
			throws ConverterException {

		IXMLConverter converter = newXMLConverter(is, sw);
		try {
			converter.parse();

		} finally {
			converter.close();
		}

	}

	public static JXConverter getConverter(String format,
			SerializeOpts serializeOpts, List<XValue> args)
			throws InvalidArgumentException {
		if (Util.isEqual(format, "jxon"))
			return new JXONConverter(serializeOpts, args);
		else if (Util.isEqual(format, "jsonx"))
			return new JSONXConverter(serializeOpts, args);
		else if (Util.isEqual(format, "jackson"))
			return new JacksonConverter(serializeOpts, args);
		else
			throw new InvalidArgumentException("Unknown convert format: "
					+ format);

	}

	protected List<XValue> getArgs() {
		return mArgs;
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