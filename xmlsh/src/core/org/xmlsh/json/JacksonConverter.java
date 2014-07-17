/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.json;

import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.WhitespaceStrippingPolicy;
import net.sf.saxon.s9api.XdmNode;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import org.xmlsh.core.CoreException;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.XMLStreamWriterDelegate;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.JSONUtils;
import org.xmlsh.util.Util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.NumberFormat;
import java.util.List;

import javanet.staxutils.OutputFactory;

import javax.xml.crypto.dsig.TransformException;
import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.stream.StreamSource;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonParser.NumberType;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.deser.FromXmlParser;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;

public class JacksonConverter extends JXConverter
{
	public static Logger	mLogger	     = LogManager.getLogger(JacksonConverter.class);


	public JacksonConverter(JSONSerializeOpts jsonSerializeOpts, SerializeOpts serializeOpts, List<XValue> mArgs)
	{
		super(jsonSerializeOpts, serializeOpts, mArgs);
	}

	class JSONConverter  implements IJSONConverter {
		    
		private JsonGenerator mGenerator;
		private FromXmlParser mParser;
		


		protected JSONConverter(XMLStreamReader reader, OutputStream os) throws ConverterException
        {
	        super();
	        try {
				mParser = JSONUtils.getXmlMapper().getFactory().createParser(reader);
	            mGenerator = JSONUtils.getJsonObjectMapper().getFactory().createGenerator(os);
            } catch (IOException e) {

            	throw new ConverterException(e);
            }
        }


		@Override
        public boolean parse() throws ConverterException
        {
			try {
				/*
				mParser.nextToken();
	            mGenerator.copyCurrentStructure(mParser);
	            */
				TreeNode tree = mParser.readValueAsTree();
				mGenerator.writeTree(tree);
	            return true ;
            } catch (IOException e) {
            	throw new ConverterException(e);
            }
			
        }
		
	}
	

	class XMLConverter implements IXMLConverter {
		    
		JsonParser mParser;
		InputStream mInput;
		XMLStreamWriter mWriter;
		ToXmlGenerator mGenerator;
		

		protected XMLConverter(InputStream is, XMLStreamWriter sw ) throws ConverterException
        {
			mInput = is ;
	        mWriter = new XMLStreamWriterDelegate(sw);
			
	        try {
	            mParser = JSONUtils.getJsonObjectMapper().getFactory().createParser(is);
	            mGenerator = JSONUtils.getXmlMapper().getFactory().createGenerator(mWriter);
            } catch (JsonParseException e) {
	            throw new ConverterException(e);
            } catch (IOException e) {
	            throw new ConverterException(e);
            }

        }
		
		
		@Override
        public boolean parse() throws ConverterException
        {
			try { 
				TreeNode tree = mParser.readValueAsTree();
				mGenerator.writeTree(tree);
				
				/*
				mParser.nextToken();
	            mGenerator.copyCurrentStructure(mParser);
	            */
	            return true ;
            } catch (IOException e) {
            	throw new ConverterException(e);
            }
        }
		
	}


	@Override
    IJSONConverter newJConverter(XMLStreamReader reader, OutputStream os ) throws ConverterException
    {
		return new JSONConverter(reader, os);
    }



	@Override
    IXMLConverter newXMLConverter(InputStream is, XMLStreamWriter sw) throws ConverterException
    {
		return new XMLConverter(is, sw);
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