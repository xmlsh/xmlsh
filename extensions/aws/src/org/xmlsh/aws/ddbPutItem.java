package org.xmlsh.aws;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import net.sf.saxon.s9api.SaxonApiException;

import org.xmlsh.aws.util.AWSDDBCommand;
import org.xmlsh.aws.util.DDBTypes;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;


public class ddbPutItem	 extends  AWSDDBCommand {
    
    private boolean bQuiet = false ;
	

	/**
	 * @param args
	 * @throws IOException 
	 */
	@Override
	public int run(List<XValue> args) throws Exception {
        Options opts = getOptions("table=table-name:,key-name:+,key-value:+,key:+,c=condition:,q=quiet,return=return-values:");

	//	Options opts = getOptions("expected:+,q=quiet");
		opts.parse(args);

        setSerializeOpts(this.getSerializeOpts(opts));
    
		args = opts.getRemainingArgs();
		  opts.parse(args);
	        String tableName = opts.getOptStringRequired("table");
	        Map<String, AttributeValue> attrs = parseKeyOptions(opts);
	        
	     if( !args.isEmpty() ) 
	       attrs.putAll( DDBTypes.parseAttributeValues(args) );

		bQuiet = opts.hasOpt("quiet");
		try {
			 getDDBClient(opts);
		} catch (UnexpectedException e) {
			usage( e.getLocalizedMessage() );
			return 1;
			
		}
		

		int ret = -1;
		ret = put(tableName, attrs , opts.getOptString("condition", null) , opts.getOptString("return-values", null ) );

		
		
		return ret;
		
		
	}


	private int put(String tableName, Map<String, AttributeValue> attrs, String condition ,String returnValues ) throws IOException, XMLStreamException, SaxonApiException, CoreException 
	{
		
	     
		PutItemRequest putItemRequest = new PutItemRequest().
				withTableName(tableName).withItem(attrs);
		if( condition != null)
		    putItemRequest.setConditionExpression(condition);
		if( returnValues != null )
		    putItemRequest.setReturnValues(returnValues);
		
		
		traceCall("putItem");

		PutItemResult result = mAmazon.putItem(putItemRequest);

			OutputPort stdout = this.getStdout();
			mWriter = stdout.asXMLStreamWriter(getSerializeOpts());
			 startDocument();
		     startElement(getName());

			if( result.getAttributes() != null ){
			    writeItem( result.getAttributes() );
			}
			
	        writeMetric(  new RequestMetrics( result.getConsumedCapacity() , result.getItemCollectionMetrics() ));
			endElement();
			endDocument();
			closeWriter();
			stdout.writeSequenceTerminator(getSerializeOpts());
			stdout.release();
			
		
		
		return 0;
		
	}




	

}
