package org.xmlsh.aws;

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.stream.XMLStreamException;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.trans.XPathException;

import org.xmlsh.aws.util.AWSDDBCommand;
import org.xmlsh.aws.util.DDBTypes;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.UnimplementedException;
import org.xmlsh.core.XValue;
import org.xmlsh.util.Util;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;


public class ddbPutItem	 extends  AWSDDBCommand {
    
    private boolean bQuiet = false ;
	/**
	 * @param args
	 * @throws IOException 
	 */
	@Override
	public int run(List<XValue> args) throws Exception {
        Options opts = getOptions("j=json,d=document,table=table-name:,key-name:+,key-value:+,key:+,c=condition:,q=quiet,return=return-values:,attr-name-expr:+,attr-value-expr:+");
	//	Options opts = getOptions("expected:+,q=quiet");
		opts.parse(args);
        setSerializeOpts(this.getSerializeOpts(opts));
		
		bQuiet = opts.hasOpt("quiet");
		try {
			 getDDBClient(opts);
		} catch (UnexpectedException e) {
			usage( e.getLocalizedMessage() );
			return 1;
			
		}

		int ret = -1;
	if( opts.hasOpt("json" ) || opts.hasOpt("document") )
	    ret = putDocument(opts);
	else
		ret = put(opts);
		return ret;
		
		
	}

    private int putDocument( Options opts ) throws XPathException, IOException, XMLStreamException, SaxonApiException, CoreException {
        String tableName = opts.getOptStringRequired("table");
        DynamoDB dynamoDB = super.getDynamotDB(opts);
        Table table = dynamoDB.getTable(tableName);
        PutItemSpec itemSpec = parsePutItemSpec( opts );
        PrintStream ostream = null;
      try {
             PutItemOutcome item =    table.putItem(itemSpec);
             Item r = item.getItem() ;
             if( r != null ){
                 assert( r != null );
                 ostream = mShell.getEnv().getStdout().asPrintStream(getSerializeOpts());
                 ostream.print( r.toJSON());
                 ostream.close(); 
                 ostream=null;
             }
        } catch (AmazonClientException e) {
            return handleException(e);
        } finally {
           if( ostream != null ) ostream.close();
        }
       
        return 0;


    }

    private int put(Options opts)
            throws IOException, XMLStreamException, SaxonApiException, CoreException
    {
        String condition = opts.getOptString("condition", null);
        String returnValues = opts.getOptString("return-values", null ) ;
        
        List<XValue> args = opts.getRemainingArgs();
          String tableName = opts.getOptStringRequired("table");
          Map<String, AttributeValue> item = parseKeyOptions(opts);
          
       if( !args.isEmpty() ) 
           // allow args to be sequences 
           item.putAll( DDBTypes.parseAttributeValues(  Util.expandSequences(args) ));
        
        PutItemRequest putItemRequest = new PutItemRequest().
                withTableName(tableName).
                withItem(item).
                withExpressionAttributeNames(DDBTypes.parseAttrNameExprs(opts)).
                withExpressionAttributeValues(DDBTypes.parseAttrValueExprs(opts));
        
        if (condition != null)
            putItemRequest.setConditionExpression(condition);
        if (returnValues != null)
            putItemRequest.setReturnValues(returnValues);
        
       

        traceCall("putItem");
        PutItemResult result = null;
        try {
            result = mAmazon.putItem(putItemRequest);
        } catch (ConditionalCheckFailedException ce) {
            return handleConditionException(ce);
        } catch (AmazonClientException e) {
            return handleException(e);
        }
        startResult();
        if (result.getAttributes() != null) {
            writeItem(result.getAttributes());
        }
        writeMetric(new RequestMetrics(result.getConsumedCapacity(), result.getItemCollectionMetrics()));
        endResult();

        return 0;

    }




    protected PutItemSpec parsePutItemSpec(Options opts) throws InvalidArgumentException, UnexpectedException, UnimplementedException, IOException, XPathException {
        PutItemSpec spec = new PutItemSpec().withItem(   Item.fromMap(DDBTypes.parseItemValueObject(opts)) ).
                withNameMap(DDBTypes.parseAttrNameExprs(opts)).
                withValueMap(  DDBTypes.parseAttrValueObjectExprs(opts) );
        
        String condition = opts.getOptString("condition", null);
        String returnValues = opts.getOptString("return-values", null ) ;
        if (condition != null)
            spec = spec.withConditionExpression(condition);
        if( returnValues != null)
            spec = spec.withReturnValues( ReturnValue.fromValue(returnValues ));

                  
        if( opts.hasRemainingArgs() )
            return spec;
        return spec;
    }
    
	

}
