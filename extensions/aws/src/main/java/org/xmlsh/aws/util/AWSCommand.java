/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.aws.util;


import java.util.Date;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.core.Options;
import org.xmlsh.core.XCommand;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.util.Util;

import com.amazonaws.AmazonWebServiceClient;
import com.amazonaws.services.cloudformation.model.Tag;

public abstract class AWSCommand<T extends AmazonWebServiceClient>  extends XCommand {

	private	AWSClient<T>	mAmazon ;

	Logger mLogger = LogManager.getLogger( );

	protected XMLStreamWriter mWriter;
	public static final String sCOMMON_OPTS = "region:,endpoint:,client:,config:,accessKey:,secretKey:,rate-retry:,retry-delay:,result-format:" ;
	protected int rateRetry = 0;
	protected int retryDelay = 10000; // 10 seconds default



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


	protected Options getOptions( String sopts )	
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


	public void attribute(String localName, boolean flag) throws XMLStreamException {
		attribute( localName , flag ? "true" : "false" );

	}




	protected void writeZones(List<String> zones) throws XMLStreamException {

		writeStringList("zones" , "zone" , "name" , zones );

	}

	protected void attribute(String name, Integer value) throws XMLStreamException {
		attribute( name , value == null ? "" : String.valueOf(value));
	}

	protected void attribute(String name, int value) throws XMLStreamException {
		attribute( name , String.valueOf(value));
	}


	protected void attribute(String name, Date date) throws XMLStreamException {
		attribute( name ,  date == null ? "" : Util.formatXSDateTime(date));

	}


	protected void writeStringList(String listName, String elementName, String attrName, List<String> strings)
			throws XMLStreamException {
		if( listName != null )
			startElement(listName);
		for( String string : strings ){

			writeElementAttribute(elementName, attrName, string);
		}
		if( listName != null )
			endElement();

	}


	public void writeElementAttribute(String elementName, String attrName, String string)
			throws XMLStreamException {
		startElement( elementName );
		attribute( attrName  , string );
		endElement();
	}


	protected void attribute(String name, long n) throws XMLStreamException {
		attribute( name , String.valueOf(n));

	}


	


	protected void emptyDocument() throws XMLStreamException {
		startDocument();
		startElement(getName());
		endElement();
		endDocument();
	}


	protected void textElement(String name, String s) throws XMLStreamException {
		startElement(name);
		characters(s);
		endElement();
	}

	protected void traceCall( String method )
	{
		T obj = getAWSClient();

		mLogger.info( "AWS Method Call: " + obj.getClass().toString() + "." + method );
	}

	protected void parseCommonOptions(Options opts) {

		rateRetry = opts.getOptInt("rate-retry", 0);
		retryDelay = opts.getOptInt("retry-delay", 10000);



	}

	protected T getAWSClient() {
		return mAmazon.getClient();
	}


	protected void setAmazon(	AWSClient<T> a)
	{
		mAmazon = a;
	}


	protected AWSClient<T> getClient() {
		return mAmazon;
	}




 




}

//
//
// Copyright (C) 2008-2014    David A. Lee.
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
