package org.xmlsh.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;



/**
 * @author DLEE
 * 
 * Validate an XML Document against a schema Document must come from a stream
 */
public class XSDValidator {
	

	private String mSchema = null;
	private	 List<String> mSchemaList = null ; 

	private class ValidatorHandler extends DefaultHandler {

		/**
		 * Returns a string describing parse exception details
		 */
		private String getParseExceptionInfo(SAXParseException spe) {
			String systemId = spe.getSystemId();
			if (systemId == null) {
				systemId = "null";
			}
			String info = "source: " + systemId + " Line=" + spe.getLineNumber() + ": "
					+ spe.getMessage();
			return info;
		}

		// The following methods are standard SAX ErrorHandler methods.
		// See SAX documentation for more info.

		public void warning(SAXParseException spe) throws SAXException {
			String message = "XML Parsing Warning: " + getParseExceptionInfo(spe);
			throw new SAXException(message);

		}

		public void error(SAXParseException spe) throws SAXException {
			// mLogger.error("XML Parsing Error", spe);
			String message = "XML Parsing Error: " + getParseExceptionInfo(spe);
			throw new SAXException(message);
		}

		public void fatalError(SAXParseException spe) throws SAXException {
			// mLogger.fatal("XML Parsing Fatal", spe);
			String message = "Fatal Error: " + getParseExceptionInfo(spe);
			throw new SAXException(message);
		}

		/* (non-Javadoc)
		 * @see org.xml.sax.helpers.DefaultHandler#resolveEntity(java.lang.String, java.lang.String)
		 */
		@Override
		public InputSource resolveEntity(String publicId, String systemId) throws IOException,
				SAXException {
			if( systemId.toLowerCase().endsWith(".dtd"))
				return new InputSource( new NullInputStream());
			else
				return super.resolveEntity(publicId, systemId);
		}

	}

	public XSDValidator(String schema) {
		mSchema = schema;

	}
	public XSDValidator(List<String> schemas) {
		mSchemaList = schemas;

	}
	/*
	 * Private method to create a SAXParser; if a schema is supplied, validation
	 * properties are set in the SAXParserFactory, and the parser's schema is
	 * set after creation
	 */
	public void validate(InputStream xml ) throws Exception {

		SAXParserFactory f = SAXParserFactory.newInstance();
		
		// f.setValidating(true);
		
		f.setFeature("http://xml.org/sax/features/validation", true);
		f.setFeature("http://apache.org/xml/features/validation/schema", true);
		f.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		// f.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammer", false);
		
		f.setNamespaceAware(true);
		f.setValidating(true);
		
		SAXParser parser = f.newSAXParser();
		
		parser.setProperty(
			    "http://java.sun.com/xml/jaxp/properties/schemaLanguage",
			    "http://www.w3.org/2001/XMLSchema");
		if( mSchema != null )
			parser.setProperty(
				"http://apache.org/xml/properties/schema/external-noNamespaceSchemaLocation",
				mSchema);
		
		if( mSchemaList != null ) {
			StringBuffer sb = new StringBuffer();
			for( String s : mSchemaList ){
				if( sb.length() > 0 )
					sb.append(" ");
				sb.append( s );
				
			}
			

			parser.setProperty(
					"http://apache.org/xml/properties/schema/external-schemaLocation",
					sb.toString() );
				
		}
		
	    
		
		parser.parse(xml, new ValidatorHandler());
	}


	
}
