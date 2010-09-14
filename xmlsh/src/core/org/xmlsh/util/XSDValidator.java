package org.xmlsh.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.xerces.xs.ElementPSVI;
import org.apache.xerces.xs.PSVIProvider;
import org.xml.sax.Attributes;
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

		private SAXParser 	mParser;
		
		
		
		
		public ValidatorHandler(SAXParser parser) {
			super();
			mParser = parser;
		}

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

		/* (non-Javadoc)
		 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
		 */
		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes)
				throws SAXException {

			super.startElement(uri, localName, qName, attributes);
			
			/*
			 * THIS WORKS !!!!!!!!!!!!!
			PSVIProvider psvi = (PSVIProvider) mParser;
			ElementPSVI elem = psvi.getElementPSVI();
			
			System.out.println("PSVI for: " + elem.getElementDeclaration().getName() );
			*/
			
			
			
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
	private void validate_test(InputStream xml ) throws Exception {

		SchemaFactory f = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		
		// f.setFeature("http://xml.org/sax/features/validation", true);
		// f.setFeature("http://apache.org/xml/features/validation/schema", true);
		//f.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		// f.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammer", false);
		
		// f.setNamespaceAware(true);
		// f.setValidating(true);
		

		/*
		f.setProperty(
			    "http://java.sun.com/xml/jaxp/properties/schemaLanguage",
			    "http://www.w3.org/2001/XMLSchema");
		*/
		if( mSchema != null )
			f.setProperty(
				"http://apache.org/xml/properties/schema/external-noNamespaceSchemaLocation",
				mSchema);
		
		if( mSchemaList != null ) {
			StringBuffer sb = new StringBuffer();
			for( String s : mSchemaList ){
				if( sb.length() > 0 )
					sb.append(" ");
				sb.append( s );
				
			}
			

			f.setProperty(
					"http://apache.org/xml/properties/schema/external-schemaLocation",
					sb.toString() );
				
		}
		
	    
		Schema s = f.newSchema();
		Validator v = s.newValidator();
		v.setErrorHandler(new ValidatorHandler(null));
		v.validate( new StreamSource(xml));
		

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
		
	    
		
		parser.parse(xml, new ValidatorHandler(parser));
	
	}


	
}
