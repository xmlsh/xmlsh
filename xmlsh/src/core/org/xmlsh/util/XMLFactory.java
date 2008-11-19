package org.xmlsh.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xmlsh.sh.shell.Shell;

/**
 * @author DLEE
 * 
 * Helper code for creating a standard JDK DOM tree
 * 
 */
public class XMLFactory
{

	private static Logger mLogger = LogManager.getLogger(XMLFactory.class);

	/*
	 * UNUSED 
	    private static final String outputEncoding = "UTF-8";
	    private static final String JAXP_SCHEMA_LANGUAGE =
	        "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
	    private static final String W3C_XML_SCHEMA =
	        "http://www.w3.org/2001/XMLSchema";
	    private static final String JAXP_SCHEMA_SOURCE =
	        "http://java.sun.com/xml/jaxp/properties/schemaSource";
	*/

	// Document Builder Factory
	private DocumentBuilder mdb = null;

	// Error handler to report errors and warnings
	private static class MyErrorHandler implements ErrorHandler
	{

		/**
		 * Returns a string describing parse exception details
		 */
		private String getParseExceptionInfo(SAXParseException spe)
		{
			String systemId = spe.getSystemId();
			if (systemId == null)
			{
				systemId = "null";
			}
			String info =
				"URI=" + systemId + " Line=" + spe.getLineNumber() + ": " + spe.getMessage();
			return info;
		}

		// The following methods are standard SAX ErrorHandler methods.
		// See SAX documentation for more info.

		public void warning(SAXParseException spe) throws SAXException
		{
			mLogger.warn("XML Parsing warning", spe);
		}

		public void error(SAXParseException spe) throws SAXException
		{
			mLogger.error("XML Parsing Error", spe);
			String message = "Error: " + getParseExceptionInfo(spe);
			throw new SAXException(message);
		}

		public void fatalError(SAXParseException spe) throws SAXException
		{
			mLogger.fatal("XML Parsing Fatal", spe);
			String message = "Fatal Error: " + getParseExceptionInfo(spe);
			throw new SAXException(message);
		}
	}

	public XMLFactory() throws XMLException
	{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		// Set namespaceAware to true to get a DOM Level 2 tree with nodes
		// containing namesapce information.  This is necessary because the
		// default value from JAXP 1.0 was defined to be false.
		dbf.setNamespaceAware(false);

		// Set the validation mode to either: no validation, DTD
		// validation, or XSD validation
		dbf.setValidating(false);

		// Optional: set various configuration options
		dbf.setIgnoringComments(true);
		dbf.setIgnoringElementContentWhitespace(true);
		dbf.setCoalescing(false);

		// The opposite of creating entity ref nodes is expanding them inline
		dbf.setExpandEntityReferences(true);

		// Step 2: create a DocumentBuilder that satisfies the constraints
		// specified by the DocumentBuilderFactory
		try
		{
			mdb = dbf.newDocumentBuilder();
		}
		catch (ParserConfigurationException e)
		{
			throw new XMLException("Parser configuration exception", e);
		}

		mdb.setErrorHandler(new MyErrorHandler());

	}

	public Document parse(File file) throws XMLException
	{
		try
		{
			return mdb.parse(file);
		}
		catch (Exception e)
		{
			throw new XMLException("Exception parsing XML",e);
		}
	}

	public Document parse(InputStream stream) throws XMLException
	{
		try
		{
			return mdb.parse(stream);
		}
		catch (Exception e)
		{
			throw new XMLException("Exception parsing XML",e);
		}
	}

	public Document parse(String string) throws XMLException, UnsupportedEncodingException
	{

        return parse( new ByteArrayInputStream( string.getBytes(Shell.getEncoding())));
	}

	public Document newDocument()
	{
		return mdb.newDocument();
	}

}

//
//
//Copyright (C) 2008, David A. Lee.
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

