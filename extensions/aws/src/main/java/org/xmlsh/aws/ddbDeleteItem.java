package org.xmlsh.aws;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import net.sf.saxon.s9api.SaxonApiException;

import org.xmlsh.aws.util.AWSDDBCommand;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.Options;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.DeleteItemRequest;
import com.amazonaws.services.dynamodbv2.model.DeleteItemResult;


public class ddbDeleteItem	 extends  AWSDDBCommand {

    /**
     * @param args
     * @throws IOException 
     */
    @Override
    public int run(List<XValue> args) throws Exception {
        Options opts = getOptions(sTABLE_OPTIONS, sKEY_OPTIONS , sCONDITION_OPTIONS,sRETURN_OPTIONS );

        //	Options opts = getOptions("expected:+,q=quiet");
        parseOptions(opts, args);

        setSerializeOpts(this.getSerializeOpts(opts));

        args = opts.getRemainingArgs();
        parseOptions(opts, args);
        String tableName = opts.getOptStringRequired("table");
        Map<String, AttributeValue> attrs = parseKeyOptions(opts);

        if( !args.isEmpty() ){
            usage("Unexpected arguments");
            return 1;
        }

        try {
            getDDBClient(opts);
        } catch (UnexpectedException e) {
            usage( e.getLocalizedMessage() );
            return 1;

        }

        int ret = -1;
        ret = delete(tableName, attrs , opts.getOptString("condition", null) , opts.getOptString("return-values", null ) );

        return ret;


    }


    private int delete(String tableName, Map<String, AttributeValue> key, String condition ,String returnValues ) throws IOException, XMLStreamException, SaxonApiException, CoreException 
    {


        DeleteItemRequest deleteItemRequest = new DeleteItemRequest().
                withTableName(tableName).withKey(key);
        if( condition != null)
            deleteItemRequest.setConditionExpression(condition);
        if( returnValues != null )
            deleteItemRequest.setReturnValues(returnValues);
        traceCall("deleteItem");
        DeleteItemResult result = null ;
        try {
            result = getAWSClient().deleteItem(deleteItemRequest);
        } catch( AmazonClientException e ) {
            return handleException(e);
        }


        if( ! bQuiet ){
            startResult();
            if( result.getAttributes() != null ){
                writeItem( result.getAttributes() );
            }
            writeMetric(  new RequestMetrics( result.getConsumedCapacity() , result.getItemCollectionMetrics() ));
            endResult();
        }	


        return 0;

    }




}
