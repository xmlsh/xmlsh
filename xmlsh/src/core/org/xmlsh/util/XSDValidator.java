package org.xmlsh.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

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

	/*
	 * Features stolen from SourceValidator.java from xerces
	 */

	/** Schema full checking feature id (http://apache.org/xml/features/validation/schema-full-checking). */
	protected static final String SCHEMA_FULL_CHECKING_FEATURE_ID = "http://apache.org/xml/features/validation/schema-full-checking";

	/** Honour all schema locations feature id (http://apache.org/xml/features/honour-all-schemaLocations). */
	protected static final String HONOUR_ALL_SCHEMA_LOCATIONS_ID = "http://apache.org/xml/features/honour-all-schemaLocations";

	/** Validate schema annotations feature id (http://apache.org/xml/features/validate-annotations) */
	protected static final String VALIDATE_ANNOTATIONS_ID = "http://apache.org/xml/features/validate-annotations";

	/** Generate synthetic schema annotations feature id (http://apache.org/xml/features/generate-synthetic-annotations). */
	protected static final String GENERATE_SYNTHETIC_ANNOTATIONS_ID = "http://apache.org/xml/features/generate-synthetic-annotations";

	// property ids

	/** StAX support for reporting line and column numbers property id (javax.xml.stream.isSupportingLocationCoordinates). */
	protected static final String IS_SUPPORTING_LOCATION_COORDINATES = "javax.xml.stream.isSupportingLocationCoordinates";

	// default settings

	/** Default schema language (http://www.w3.org/2001/XMLSchema). */
	protected static final String DEFAULT_SCHEMA_LANGUAGE = XMLConstants.W3C_XML_SCHEMA_NS_URI;


	/** Default validation source. */
	protected static final String DEFAULT_VALIDATION_SOURCE = "sax";

	/** Default schema full checking support (false). */
	protected static final boolean DEFAULT_SCHEMA_FULL_CHECKING = false;

	/** Default honour all schema locations (false). */
	protected static final boolean DEFAULT_HONOUR_ALL_SCHEMA_LOCATIONS = false;

	/** Default validate schema annotations (false). */
	protected static final boolean DEFAULT_VALIDATE_ANNOTATIONS = false;

	/** Default generate synthetic schema annotations (false). */
	protected static final boolean DEFAULT_GENERATE_SYNTHETIC_ANNOTATIONS = false;

	/** Default memory usage report (false). */
	protected static final boolean DEFAULT_MEMORY_USAGE = false;



	private class ValidatorHandler extends DefaultHandler {

		private Object 	mParser;




		public ValidatorHandler(Object parser) {
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

		@Override
		public void warning(SAXParseException spe) throws SAXException {
			String message = "XML Parsing Warning: " + getParseExceptionInfo(spe);
			throw new SAXException(message);

		}

		@Override
		public void error(SAXParseException spe) throws SAXException {
			// mLogger.error("XML Parsing Error", spe);
			String message = "XML Parsing Error: " + getParseExceptionInfo(spe);
			throw new SAXException(message);
		}

		@Override
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


		/*
		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes)
				throws SAXException {

			super.startElement(uri, localName, qName, attributes);


			 // THIS WORKS !!!!!!!!!!!!!

			PSVIProvider psvi = (PSVIProvider) mParser;
			ElementPSVI elem = psvi.getElementPSVI();

			System.out.println("PSVI for: " + elem.getElementDeclaration().getName() );




		}
		 */

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
	public void validate_breaks_with_dtd(Source source  ) throws Exception {




		String schemaLanguage = DEFAULT_SCHEMA_LANGUAGE;
		boolean schemaFullChecking = DEFAULT_SCHEMA_FULL_CHECKING;
		boolean honourAllSchemaLocations = DEFAULT_HONOUR_ALL_SCHEMA_LOCATIONS;
		boolean validateAnnotations = DEFAULT_VALIDATE_ANNOTATIONS;
		boolean generateSyntheticAnnotations = DEFAULT_GENERATE_SYNTHETIC_ANNOTATIONS;





		SchemaFactory f = SchemaFactory.newInstance(schemaLanguage);
		f.setErrorHandler(new ValidatorHandler(null));

		f.setFeature(SCHEMA_FULL_CHECKING_FEATURE_ID, schemaFullChecking);
		f.setFeature(HONOUR_ALL_SCHEMA_LOCATIONS_ID, honourAllSchemaLocations);
		f.setFeature(VALIDATE_ANNOTATIONS_ID, validateAnnotations);
		f.setFeature(GENERATE_SYNTHETIC_ANNOTATIONS_ID, generateSyntheticAnnotations);




		List<Source>	sources = new ArrayList<Source>();



		if( mSchema != null )
			sources.add( new StreamSource(mSchema));

		Schema s = f.newSchema(sources.toArray(new Source[sources.size()]));
		Validator v = s.newValidator();
		ValidatorHandler validatorHandler = new ValidatorHandler(v);
		v.setErrorHandler(validatorHandler);
		v.setFeature(SCHEMA_FULL_CHECKING_FEATURE_ID, schemaFullChecking);
		v.setFeature(HONOUR_ALL_SCHEMA_LOCATIONS_ID, honourAllSchemaLocations);
		v.setFeature(VALIDATE_ANNOTATIONS_ID, validateAnnotations);
		v.setFeature(GENERATE_SYNTHETIC_ANNOTATIONS_ID, generateSyntheticAnnotations);


		v.validate( source );


	}
	/*
	 * Private method to create a SAXParser; if a schema is supplied, validation
	 * properties are set in the SAXParserFactory, and the parser's schema is
	 * set after creation
	 */
	public void validate(InputSource source ) throws Exception {

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



		parser.parse( source, new ValidatorHandler(parser));

	}



}
