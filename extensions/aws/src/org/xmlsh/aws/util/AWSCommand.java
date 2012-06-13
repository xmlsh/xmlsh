/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.aws.util;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.xmlsh.core.Options;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XCommand;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.util.Util;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;

public abstract class AWSCommand extends XCommand {
	
	protected XMLStreamWriter mWriter;
	protected SerializeOpts mSerializeOpts;
	
	protected static final String sCOMMON_OPTS = "client:,config:accessKey:,secretKey:" ;
	
	
	static {
		
		LogManager.getLogger("httpclient").setLevel(Level.WARN);
		LogManager.getLogger("http.wire").setLevel(Level.WARN);

		LogManager.getLogger("org.apache.http").setLevel(Level.WARN);
		LogManager.getLogger("com.amazonaws").setLevel(Level.WARN);
		
	}
	
	
	
	public AWSCommand() {
		super();
	}
	
	
	protected	Options getOptions()	
	{

		return new Options( getCommonOpts()  , SerializeOpts.getOptionDefs());
		
	
			
	}


	protected String getCommonOpts() {
		return sCOMMON_OPTS;
	}
	
	
	protected	Options getOptions( String sopts )	
	{

		return new Options( getCommonOpts() + "," + sopts , SerializeOpts.getOptionDefs());
		
	
			
	}
	
	
	
	protected void closeWriter() throws XMLStreamException {
		mWriter.flush();
		mWriter.close();
	}


	protected void endDocument() throws XMLStreamException {
		mWriter.writeEndDocument();
	}


	protected void endElement() throws XMLStreamException {
		mWriter.writeEndElement();
	}


	protected void startElement(String localname) throws XMLStreamException {
		mWriter.writeStartElement(localname);
	}


	protected void startDocument() throws XMLStreamException {
		mWriter.writeStartDocument();
	}
	
	protected	  void attribute( String localName , String value ) throws XMLStreamException
	{
		mWriter.writeAttribute(localName, Util.notNull(value));
	}
	
	
	
	protected	  void characters( String value ) throws XMLStreamException
	{
		if( value != null )
			mWriter.writeCharacters(value);
	}

}

//
//
// Copyright (C) 2008-2012  David A. Lee.
//
// The contents of this file are subject to the "Simplified BSD License" (the
// "License");
// you may not use this file except in compliance with the License. You may
// obtain a copy of the
// License at http://www.opensource.org/licenses/bsd-license.php
//
// Software distributed under the License is distributed on an "AS IS" basis,
// WITHOUT WARRANTY OF ANY KIND, either express or implied.
// See the License for the specific language governing rights and limitations
// under the License.
//
// The Original Code is: all this file.
//
// The Initial Developer of the Original Code is David A. Lee
//
// Portions created by (your name) are Copyright (C) (your legal entity). All
// Rights Reserved.
//
// Contributor(s): none.
//
