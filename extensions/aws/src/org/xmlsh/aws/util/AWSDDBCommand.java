/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.aws.util;

import org.xmlsh.commands.util.CSVParser;
import org.xmlsh.commands.util.CSVRecord;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;
import org.xmlsh.util.Base64;
import org.xmlsh.util.StringPair;
import org.xmlsh.util.Util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import com.amazonaws.regions.RegionUtils;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.LocalSecondaryIndexDescription;
import com.amazonaws.services.dynamodbv2.model.Projection;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughputDescription;
import com.amazonaws.services.dynamodbv2.model.TableDescription;

public abstract class AWSDDBCommand extends AWSCommand {


	private static final QName Q_TYPE = new QName("type");
	private static final QName Q_NAME = new QName("name");
	private static final QName Q_ITEM = new QName("item");
	private static final QName Q_VALUE = new QName("value");
	private static final QName Q_ATTRIBUTE = new QName("attribute");


	protected		AmazonDynamoDBClient mAmazon ;

	private String mRegion = null;
	private String mEndpoint = null ;


	public AWSDDBCommand() {
		super();
	}
	@Override
	protected Object getClient() {
		return mAmazon; 
	}

	protected void getDDBClient(Options opts) throws UnexpectedException, InvalidArgumentException {


		mAmazon =  new AmazonDynamoDBClient(
				new AWSCommandCredentialsProviderChain( mShell, opts  ) 

				);


		setRegion(opts);
		setEndpoint(opts);

		if( Util.isEqual(mRegion,"local") && mEndpoint != null )
			mAmazon.setEndpoint( mEndpoint , "dynamodb" , mRegion );
		else {
			if( mRegion != null )
				mAmazon.setRegion(RegionUtils.getRegion(mRegion));
			if( mEndpoint != null )
				mAmazon.setEndpoint( mEndpoint );

		}







	}

	/* (non-Javadoc)
	 * @see org.xmlsh.aws.util.AWSCommand#setRegion(java.lang.String)
	 */
	@Override
	public void setRegion(String region) {
		mRegion = region;
	}

	@Override
	public void setEndpoint( String endpoint )
	{
		mEndpoint = endpoint ;
	}

	protected void writeAttribute(String key, AttributeValue avalue) throws XMLStreamException, IOException {

		startElement("attribute");
		attribute("name" , key);


		if (avalue.getS() != null) { 
			attribute("type","S");
			characters( avalue.getS());
		}
		if (avalue.getN() != null) {
			attribute("type" , "N");
			characters( avalue.getN());
		}

		if (avalue.getB() != null) {
			attribute("type","B");
			binary( avalue.getB().array() );	
		}
		if (avalue.getSS() != null) {
			attribute("type","SS");
			for( String s : avalue.getSS() ){
				startElement("value");
				characters( s );
				endElement();
			}

		}
		if (avalue.getNS() != null) {
			attribute("type","NS");
			for( String s : avalue.getNS() ){
				startElement("value");
				characters( s );
				endElement();
			}
		}
		if (avalue.getBS() != null) {
			attribute("type","BS");
			for( ByteBuffer s : avalue.getBS() ){
				startElement("value");
				binary( s.array() );
				endElement();
			}
		}

		endElement();
	}


	protected CSVRecord parseCols(XValue cols) {

		if( cols.isAtomic() )
			return new CSVRecord( Arrays.asList( cols.toString().split(",")));
		else
			return new CSVRecord(cols.asStringList());


	}
	void binary( byte[] array ) throws IOException
	{

		ByteArrayOutputStream os = new ByteArrayOutputStream();
		Base64.OutputStream b64 = new Base64.OutputStream( os , Base64.ENCODE  ); 
		b64.write(array);
		b64.close();



	}

	protected void writeTableDescription(TableDescription tableDescription) throws XMLStreamException {
		startElement("table");
		attribute("name",tableDescription.getTableName());
		attribute("status",		tableDescription.getTableStatus() );
		attribute("create-date", Util.formatXSDateTime(tableDescription.getCreationDateTime()));
		attribute("item-count",tableDescription.getItemCount());
		attribute("size",tableDescription.getTableSizeBytes() );


		writeAttributeDefinitions(tableDescription.getAttributeDefinitions());
		writeKeySchema(tableDescription.getKeySchema());
		writeLocalSecondaryIndexes(tableDescription.getLocalSecondaryIndexes());
		writeProvisionedThroughput(tableDescription.getProvisionedThroughput());


	}

	private void writeLocalSecondaryIndexes(List<LocalSecondaryIndexDescription> localSecondaryIndexes) throws XMLStreamException {
		startElement("local-secondary-indexes");
		if( localSecondaryIndexes != null )
			for( LocalSecondaryIndexDescription index : localSecondaryIndexes)
				writeLocalSecondaryIndex( index );
		endElement();

	}

	private void writeLocalSecondaryIndex(LocalSecondaryIndexDescription index) throws XMLStreamException {

		startElement("local-secondary-index");
		attribute("index-name",index.getIndexName());
		attribute("index-size",index.getIndexSizeBytes());
		attribute("item-count",index.getItemCount());

		writeKeySchema(index.getKeySchema());
		writeProjection(index.getProjection());
		endElement();

	}

	private void writeProjection(Projection projection) throws XMLStreamException {
		startElement("projection");
		attribute("projection-type",projection.getProjectionType());
		for( String s : projection.getNonKeyAttributes() )
			textElement("non-key-attribute", s );


		endElement();

	}

	private void writeProvisionedThroughput(ProvisionedThroughputDescription provisionedThroughput) {
		// TODO Auto-generated method stub

	}

	private void writeKeySchema(List<KeySchemaElement> keySchema) {
		// TODO Auto-generated method stub

	}

	private void writeAttributeDefinitions(List<AttributeDefinition> attributeDefinitions) throws XMLStreamException {
		startElement("attribute-definitions");
		for( AttributeDefinition def :  attributeDefinitions )
			writeAttributeDefinition( def );
		endElement();

	}

	private void writeAttributeDefinition(AttributeDefinition def) throws XMLStreamException {
		startElement("attribute-definition");
		attribute("name" , def.getAttributeName());
		attribute("type" , def.getAttributeType());
		endElement();
	}

	public Collection<ByteBuffer> parseBS(XValue xv) throws IOException {

		ArrayList<ByteBuffer> ret = new ArrayList<ByteBuffer>();
		for( String s : xv.asStringList() )
			ret.add( parseBinary( s ));

		return ret; 


	}
	public Collection<ByteBuffer> parseBinary(String[] strings) throws IOException {

		ByteBuffer bb[] = new ByteBuffer[strings.length];
		for( int i = 0 ; i < strings.length ; i++ )
			bb[i] = parseBinary(strings[i]);
		return Arrays.asList(bb);

	}

	private ByteBuffer parseBinary(XValue xv) throws IOException {
		return parseBinary( xv.toString() );

	}


	protected ByteBuffer parseBinary(String s) throws IOException {


		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		Base64.InputStream b64 = new Base64.InputStream(new ByteArrayInputStream(s.getBytes("UTF8")), Base64.DECODE );
		Util.copyStream(b64, bos);
		b64.close();
		return ByteBuffer.wrap( bos.toByteArray());

	}


	public Collection<String> parseSS(XValue xv) {
		return xv.asStringList();
	}

	protected Map<String,AttributeValue> readAttributesXML(XMLEventReader xreader ) throws IOException, UnexpectedException, XMLStreamException {

		Map<String,AttributeValue> attributes = new HashMap<String,AttributeValue>();
		while( xreader.hasNext() ){
			XMLEvent tag = xreader.nextTag();
			if( isStartElement(tag,Q_ATTRIBUTE) ){
				StartElement e = tag.asStartElement();
				attributes.put( attributeValue(e,Q_NAME) ,
						parseAttributeValue( attributeValue(e,Q_TYPE ) , xreader ) );
				readEndElement(xreader);

			}
			else
				throw new UnexpectedException("Unexpected tag: " + tag.toString() );
		}
		return attributes;
	}



	protected Map<String,AttributeValue> readItemCSV(Reader reader , CSVParser parser,  CSVRecord header , String listSep, CSVRecord types ) 
			throws IOException, UnexpectedException, XMLStreamException 
			{

		String line = Util.readLine(reader);
		if( line == null )
			return null ;

		CSVRecord r = parser.parseLine(line);
		Map<String,AttributeValue> attributes = new HashMap<String,AttributeValue>();
		for( int i = 0 ; i < r.getNumFields() ; i++ ){
			if( i >= header.getNumFields() )
				break ;
			String value= r.getField(i);
			String name = header.getField(i);
			attributes.put(name ,  parseAttributeValue( types.getField(i) , value , listSep ) );
		}
		return attributes;
			}

	private AttributeValue parseAttributeValue(String type, String value, String listSep ) throws XMLStreamException, UnexpectedException, IOException 
	{
		AttributeValue av  = new AttributeValue();

		if( type.equals("N" ))
			av.setN( value );
		else
			if( type.equals("NS"))
				av.setNS( Arrays.asList(value.split( listSep )	) );
			else
				if( type.equals("S" ))
					av.setS(value);
				else
					if( type.equals("SS" ))
						av.setSS( Arrays.asList(value.split( listSep )	));
					else
						if( type.equals("B" ))
							av.setB( parseBinary(value) );
						else
							if( type.equals("BS" )) 
								av.setBS( parseBinary( value.split(listSep)));
							else
								throw new UnexpectedException("Unknown type: " + type );
		return av;
	}


	protected Map<String,AttributeValue> readItemXML(XMLEventReader xreader ) throws IOException, UnexpectedException, XMLStreamException {

		if( xreader.hasNext() ){
			XMLEvent tag = xreader.nextTag();
			if( isStartElement(tag, Q_ITEM) ){
				Map<String,AttributeValue> attributes =  readAttributesXML(xreader);
				readEndElement(xreader);
				return attributes ;
			}
			else
				throw new UnexpectedException("Unexpected tag: " + tag.toString() );
		} 
		return null ;

	}

	private String attributeValue(StartElement e, QName name) {
		return e.getAttributeByName( name ).getValue();
	}
	private boolean isStartElement(XMLEvent tag,QName name ) {
		return tag.isStartElement() && tag.asStartElement().getName().equals(name);
	}
	private AttributeValue parseAttributeValue(String type, XMLEventReader xreader) throws XMLStreamException, UnexpectedException, IOException {
		AttributeValue av  = new AttributeValue();


		if( type.equals("N" ))
			av.setN( readString(xreader));
		else
			if( type.equals("NS"))
				av.setNS( readStrings(xreader) );
			else
				if( type.equals("S" ))
					av.setS( readString(xreader));
				else
					if( type.equals("SS" ))
						av.setSS( readStrings(xreader));
					else
						if( type.equals("B" ))
							av.setB( readBinary(xreader) );
						else
							if( type.equals("BS" )) 
								av.setBS( readBinarys( xreader ));


							else
								throw new UnexpectedException("Unknown type: " + type );

		return av;
	}
	private Collection<ByteBuffer> readBinarys(XMLEventReader xreader) throws XMLStreamException, IOException {
		Collection<ByteBuffer> result = new ArrayList<ByteBuffer>();
		while( xreader.hasNext() ){
			XMLEvent tag = xreader.nextTag();
			if( tag.isEndElement() )
				break;
			if( isStartElement( tag , Q_VALUE ))
				result.add( readBinary(xreader));

		}
		return result ;

	}
	private ByteBuffer readBinary(XMLEventReader xreader) throws IOException, XMLStreamException {
		return parseBinary( readString(xreader) );
	}
	private Collection<String> readStrings(XMLEventReader xreader) throws XMLStreamException {
		Collection<String> result = new ArrayList<String>();

		while( xreader.hasNext() ){
			XMLEvent tag = xreader.nextTag();
			if( tag.isEndElement() )
				break;
			if( isStartElement( tag , Q_VALUE ))
				result.add( readString(xreader));

		}
		return result;

	}

	private String readString(XMLEventReader xreader) throws XMLStreamException {
		String s = xreader.getElementText();
		readEndElement( xreader );
		return s;
	}

	private void readEndElement( XMLEventReader xreader ) throws XMLStreamException{
		while(xreader.hasNext() )
			if( xreader.nextTag().isEndElement() )
				return ;

	}
	protected Map<String,AttributeValue> getAttributes(List<XValue> args) throws IOException, UnexpectedException {
		Map<String,AttributeValue> attrs = new  HashMap<String,AttributeValue>();
		while( !args.isEmpty()){

			StringPair sp = new StringPair(args.remove(0).toString(),':');
			String type = sp.getLeft();
			AttributeValue av  = new AttributeValue();

			XValue xv = args.remove(0);

			if( type.equals("N" ))
				av.setN( xv.toString() );
			else
				if( type.equals("NS"))
					av.setNS( parseSS( xv ) );
				else
					if( type.equals("S" ))
						av.setS( xv.toString());
					else
						if( type.equals("SS" ))
							av.setSS( parseSS(xv));
						else
							if( type.equals("B" ))
								av.setB( parseBinary(xv) );
							else
								if( type.equals("BS" )) 
									av.setBS( parseBS( xv ));


								else
									throw new UnexpectedException("Unknown type: " + type );


			String value = args.isEmpty() ? "" : args.remove(0).toString();

			attrs.put(sp.getRight(),av);


		}
		return attrs ;
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
