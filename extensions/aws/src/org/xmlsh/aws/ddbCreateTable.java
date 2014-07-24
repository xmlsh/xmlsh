package org.xmlsh.aws;

import net.sf.saxon.s9api.SaxonApiException;
import org.xmlsh.aws.util.AWSDDBCommand;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;
import org.xmlsh.util.StringPair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.CreateTableResult;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;


public class ddbCreateTable	 extends  AWSDDBCommand {

	

	/**
	 * @param args
	 * @throws IOException 
	 */
	@Override
	public int run(List<XValue> args) throws Exception {

		Options opts = getOptions("readCapacity:,writeCapacity:,attribute:+,key:+");
		opts.parse(args);

		args = opts.getRemainingArgs();
		
		if( args.size() != 1 )
			usage();
		
		setSerializeOpts(opts);
		
		try {
			 getDDBClient(opts);
		} catch (UnexpectedException e) {
			usage( e.getLocalizedMessage() );
			return 1;
			
		}
		

		int ret = -1;
		ret = create(args.get(0).toString(),opts);

		
		
		return ret;
		
		
	}


	private int create(String tableName, Options opts) throws IOException, XMLStreamException, SaxonApiException, CoreException 
	{

		OutputPort stdout = this.getStdout();
		mWriter = stdout.asXMLStreamWriter(getSerializeOpts());
		
		
		
	    ProvisionedThroughput provisionedThroughput = new ProvisionedThroughput()
	        .withReadCapacityUnits(opts.getOptLong("readCapacity", 5L))
	        .withWriteCapacityUnits(opts.getOptLong("writeCapacity", 5L));
		    
		
		
		startDocument();
		startElement(getName());
         
		
		
		Collection<AttributeDefinition> attributeDefinitions  = parseAttributes( opts );
		Collection<KeySchemaElement> keySchema = parseKeySchema(opts);

	
	
		CreateTableRequest createTableRequest = new CreateTableRequest()
		   .withTableName(tableName)
		   .withProvisionedThroughput(provisionedThroughput)
		   .withAttributeDefinitions(attributeDefinitions)
		   .withKeySchema(keySchema);
		   
	 

		traceCall("createTable");

		CreateTableResult result = mAmazon.createTable(createTableRequest);
	    writeTableDescription(result.getTableDescription());

		
		endElement();
		endDocument();
		
		closeWriter();
		stdout.writeSequenceTerminator(getSerializeOpts());
		
		return 0;
	}


	private Collection<KeySchemaElement> parseKeySchema(Options opts) throws InvalidArgumentException {
		
		
		ArrayList<KeySchemaElement> tableKeySchema = new ArrayList<KeySchemaElement>();

		for( XValue xv : opts.getOptValues("key") ){
			
			StringPair sp = new StringPair( xv.toString(),':' );
			
			tableKeySchema.add(new KeySchemaElement().withAttributeName(sp.getRight())
					.withKeyType(KeyType.valueOf(sp.getLeft().toUpperCase())));

		}
		return tableKeySchema;
	}


	private Collection<AttributeDefinition> parseAttributes(Options opts) throws InvalidArgumentException {

		ArrayList<AttributeDefinition> attributeDefinitions = new ArrayList<AttributeDefinition>();
        for( XValue xv : opts.getOptValues("attribute") ){
			
			StringPair sp = new StringPair( xv.toString(),':' );
			
			attributeDefinitions.add(new AttributeDefinition()
			.withAttributeName(sp.getRight()).withAttributeType(sp.getLeft()));
		}

        return attributeDefinitions;
	
		
	     
		
	}



	public void usage() {
		super.usage();
	}



	

}
