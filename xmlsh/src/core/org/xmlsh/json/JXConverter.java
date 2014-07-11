/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.json;

import java.io.IOException;

import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.util.Util;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

public abstract class JXConverter
{
	/*
	 * A class helper/config for JSON to XML conversions
	 */

	private JSONSerializeOpts mJsonSerializeOpts;
	private SerializeOpts mSerializeOpts;

	// Private class for cnverting FROM JSON 
	abstract class JSONConverter {
		XMLEventReader mReader;
		JsonGenerator  mGenerator;
		

		protected JSONConverter(XMLEventReader reader, JsonGenerator generator)
        {
	        super();
	        mReader = reader;
	        mGenerator = generator;
        }


		
		protected boolean parse( ) throws ConverterException {
			try {
				while( mReader.hasNext() ){
					XMLEvent e = mReader.nextEvent();
					if( e.isStartDocument())
						startDocument(e);
					else
					
					if( e.isStartElement() ){
						StartElement start = e.asStartElement();
						QName name = start.getName(); 
						if( ! startElement( start , name    ))
							return false ;
						
					}
					else
					if( e.isEndElement() ){
						if( ! endElement( e.asEndElement() ) ) 
							return false ;
					}
					else
					if( e.isEndDocument() ) {
						endDocument(e);
						return false ;
					}
					else
					if( e.isCharacters()) {
						if( ! characters(e)) 
							return false ;
					}
					else 
						throw new ConverterException("Unexpected XML element: " + e.getEventType() );
				}
			} catch( Exception e ) {
				 
				Util.wrapException( e , ConverterException.class );
				 
			}
			return false ;
		}
		





		void readToEnd() throws ConverterException {
			try {
	            while( mReader.hasNext() && ! mReader.peek().isEndElement() )
	            	mReader.nextEvent();
	            
	            if( mReader.hasNext())
	            	mReader.nextEvent();
            } catch (XMLStreamException e) {
	            throw new ConverterException("Exception reading to end of XML Element" ,e);
            }
		}
		
		String readChars() throws XMLStreamException {

			StringBuffer sb = new StringBuffer();
			while( mReader.hasNext() && mReader.peek().isCharacters() ){
				Characters ch = mReader.nextEvent().asCharacters();
				sb.append( ch.getData() );
				
			}
			return sb.toString();

		}

		public void readToEOF() throws ConverterException  {
			try {
		        // Consume input or we can get a Piped Close
		        while( mReader.hasNext() )
		        	mReader.nextEvent();
	        } catch (XMLStreamException e) {
	            throw new ConverterException("Exception reading to end of XML Document" ,e);

	        }
			
		}
		
		abstract boolean startElement( StartElement start , QName name) throws ConverterException;
		abstract boolean startDocument(XMLEvent e) throws ConverterException;;
		abstract boolean endElement(EndElement asEndElement) throws ConverterException;;
		abstract boolean endDocument(XMLEvent e) throws ConverterException;;
		abstract boolean characters(XMLEvent e);

	}
	
	abstract class XMLConverter {
		JsonParser mParser;
		XMLStreamWriter mWriter;
		

		protected XMLConverter(JsonParser jp, XMLStreamWriter sw )
        {
	        mParser = jp;
	        mWriter = sw;
        }


		public void parse() throws ConverterException
        {
			JsonToken tok = nextToken();
			writeValue(tok );
        }


		protected JsonToken nextToken() throws ConverterException
        {
	        try {
	            return mParser.nextToken();
            } catch (JsonParseException e) {
	           throw new ConverterException(e);
            } catch (IOException e) {
 	           throw new ConverterException(e);
            }
        }


		public void readToEOF()
        {
	        
        }
		protected void writeValue(JsonToken tok ) throws ConverterException
		{
			if(tok == null)
				return  ;

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
				writeNumber(getDoubleValue());
				break;
			case VALUE_NUMBER_INT:
				writeNumber(getLongValue());
				break;
			case VALUE_STRING:
				writeString(getStringValue());
				break;
			case VALUE_TRUE:
				writeBoolean(false);
				break;
			default:
				break;
			}


		}


		private String getStringValue() throws ConverterException 
        {
	    	try {
	            return mParser.getText();
            } catch (IOException e) {
	           throw new ConverterException(e);
            }
        }


		protected double getDoubleValue() throws ConverterException
        {
			try {
	            return mParser.getDoubleValue();
            } catch (IOException e) {
	           throw new ConverterException(e);
            }
        }

		protected long getLongValue() throws ConverterException
        {
			try {
	           return mParser.getLongValue();
            } catch (IOException e) {
	           throw new ConverterException(e);
            }
        }

		

		abstract void writeArray() throws ConverterException;
		abstract void writeObject() throws ConverterException;
		abstract void writeBoolean(boolean value)throws ConverterException;
		abstract void writeNull()throws ConverterException;
		abstract void writeNumber(double value)throws ConverterException;
		abstract void writeNumber(long value)throws ConverterException;
		abstract void writeString(String value)throws ConverterException;


		protected void writeStartElement(QName qn) throws XMLStreamException
        {
        	mWriter.writeStartElement(qn.getPrefix() , qn.getLocalPart() , qn.getNamespaceURI() );
            
        }


		protected void writeAttribute(QName qn, String value) throws XMLStreamException
        {
        	mWriter.writeAttribute( qn.getPrefix() ,  qn.getNamespaceURI(), qn.getLocalPart() , value);
            
        }


		protected void writeEndElement() throws XMLStreamException
        {
            mWriter.writeEndElement();
        }


		protected void writeCharacters(String s) throws XMLStreamException
        {
            mWriter.writeCharacters(s);
        }

		
		
	}
	
	
	abstract JSONConverter newJConverter(XMLEventReader reader, JsonGenerator generator);
	abstract XMLConverter newXMLConverter(JsonParser jp, XMLStreamWriter sw );


	/*
	 * Converter To and/or From JSON
	 */
	public JXConverter(JSONSerializeOpts jsonSerializeOpts, SerializeOpts serializeOpts )
    {
	    super();
	    mJsonSerializeOpts = jsonSerializeOpts;
	    mSerializeOpts = serializeOpts;
    }
	
	
	public void convertToJson(XMLEventReader reader, JsonGenerator jsonGenerator) throws ConverterException {
		
		
		JSONConverter converter = newJConverter(reader,jsonGenerator);
		converter.parse( );
		converter.readToEOF();

	
	}
	

	

	
	
	public void convertFromJson(JsonParser jp, XMLStreamWriter sw ) throws ConverterException {
		
		XMLConverter converter = newXMLConverter(jp,sw);
		converter.parse( );
		converter.readToEOF();

		
	}

	void close() {

	}

	

}



/*
 * Copyright (C) 2008-2012 David A. Lee.
 * 
 * The contents of this file are subject to the "Simplified BSD License" (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.opensource.org/licenses/bsd-license.php 

 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied.
 * See the License for the specific language governing rights and limitations under the License.
 *
 * The Original Code is: all this file.
 *
 * The Initial Developer of the Original Code is David A. Lee
 *
 * Portions created by (your name) are Copyright (C) (your legal entity). All Rights Reserved.
 *
 * Contributor(s): David A. Lee
 * 
 */