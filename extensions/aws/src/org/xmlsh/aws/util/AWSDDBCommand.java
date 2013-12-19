/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.aws.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.util.Base64;
import org.xmlsh.util.Util;

import com.amazonaws.regions.RegionUtils;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.LocalSecondaryIndexDescription;
import com.amazonaws.services.dynamodbv2.model.Projection;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughputDescription;
import com.amazonaws.services.dynamodbv2.model.TableDescription;

public abstract class AWSDDBCommand extends AWSCommand {
	

protected		AmazonDynamoDB mAmazon ;
	
	public AWSDDBCommand() {
		super();
	}
	protected Object getClient() {
		return mAmazon; 
	}

	protected void getDDBClient(Options opts) throws UnexpectedException, InvalidArgumentException {
		
			
		mAmazon =  new AmazonDynamoDBClient(
				new AWSCommandCredentialsProviderChain( mShell, opts  ) 
		
		);
		
		setEndpoint(opts);
		setRegion(opts);
	}
	
	/* (non-Javadoc)
	 * @see org.xmlsh.aws.util.AWSCommand#setRegion(java.lang.String)
	 */
	@Override
	public void setRegion(String region) {
	    mAmazon.setRegion( RegionUtils.getRegion(region));
		
	}
	
	@Override
    public void setEndpoint( String endpoint )
    {
    	mAmazon.setEndpoint( endpoint );
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
