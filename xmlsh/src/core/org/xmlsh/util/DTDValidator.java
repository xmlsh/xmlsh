package org.xmlsh.util;

import java.io.InputStream;
import java.net.URL;

import javax.xml.stream.XMLInputFactory;

import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLStreamReader2;
import org.codehaus.stax2.validation.XMLValidationSchema;
import org.codehaus.stax2.validation.XMLValidationSchemaFactory;



/**
 * @author DLEE
 * 
 * Validate an XML Document against a schema Document must come from a stream
 */
public class DTDValidator {
	

	private URL mDTD = null;



	public DTDValidator(URL dtd) {
		mDTD = dtd;

	}

	/*
	 * Private method to create a SAXParser; if a schema is supplied, validation
	 * properties are set in the SAXParserFactory, and the parser's schema is
	 * set after creation
	 */
	public void validate(String systemid , InputStream xml ) throws Exception {

		// step 1: get schema factory
		// XMLValidationSchemaFactory sf = XMLValidationSchemaFactory.newInstance(XMLValidationSchema.SCHEMA_ID_RELAXNG);
		XMLValidationSchemaFactory sf = XMLValidationSchemaFactory.newInstance(XMLValidationSchema.SCHEMA_ID_DTD );
		
		// step 2: construct validation schema instance
		XMLValidationSchema rng = sf.createSchema(mDTD);
		// step 3: construct stream reader
		XMLInputFactory2 ifact = (XMLInputFactory2)XMLInputFactory.newInstance();
		ifact.setProperty(XMLInputFactory2.P_DTD_OVERRIDE, null );
		ifact.setProperty(XMLInputFactory.SUPPORT_DTD , Boolean.valueOf(false) );
			

		XMLStreamReader2 sr = (XMLStreamReader2) ifact.createXMLStreamReader(systemid , xml);
		// step 4: enable validation
		
		sr.validateAgainst(rng);
		// step 5: stream through the document:
		while (sr.hasNext()) {
		  sr.next();
		}
		// done!
		
		
	}


	
}
