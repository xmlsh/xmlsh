package org.xmlsh.util;

import java.io.InputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

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

	}

	public XSDValidator(String schema) {
		mSchema = schema;

	}

	/*
	 * Private method to create a SAXParser; if a schema is supplied, validation
	 * properties are set in the SAXParserFactory, and the parser's schema is
	 * set after creation
	 */
	public void validate(InputStream xml ) throws Exception {

		SAXParserFactory f = SAXParserFactory.newInstance();
		f.setValidating(true);
		
		f.setFeature("http://xml.org/sax/features/validation", true);
		f.setFeature("http://apache.org/xml/features/validation/schema", true);
		f.setNamespaceAware(true);
		
		SAXParser parser = f.newSAXParser();

		parser.setProperty(
				"http://apache.org/xml/properties/schema/external-noNamespaceSchemaLocation",
				mSchema);
		parser.parse(xml, new ValidatorHandler());
	}


	
}
