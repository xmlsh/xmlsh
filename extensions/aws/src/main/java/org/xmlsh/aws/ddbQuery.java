package org.xmlsh.aws;

import java.io.IOException;
import java.util.ArrayList;
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
import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import com.amazonaws.services.dynamodbv2.model.QueryResult;
import com.amazonaws.services.dynamodbv2.model.Select;

public class ddbQuery extends AWSDDBCommand {
    private int kLIMIT = 100;
    /**
     * @param args
     * @throws IOException 
     */
    @Override
    public int run(List<XValue> args) throws Exception {
        Options opts = getOptions(sTABLE_OPTIONS, sKEY_OPTIONS , sRETURN_OPTIONS , sCONSISTANT_OPTS ,
                sDOCUMENT_OPTS , sATTR_EXPR_OPTIONS ,
                "limit:,query:,filter=filter-expression:,select:,index-name:,key-condition-expression:,projection-expression:");
        parseOptions(opts, args);

        args = opts.getRemainingArgs();

        setSerializeOpts(this.getSerializeOpts(opts));

        try {
            getDDBClient(opts);
        } catch (UnexpectedException e) {
            usage(e.getLocalizedMessage());
            return 1;

        }


        int ret = -1;
        ret = query(opts);

        return ret;
    }

    private int query(Options opts) throws IOException, XMLStreamException, SaxonApiException, CoreException 
    {

        boolean bConsistantRead = opts.hasOpt("consistant");
        String filterExpression = opts.getOptString("filter", null);

        Map<String, AttributeValue> exclusiveStartKey =null;


        QueryRequest queryRequest = new QueryRequest().
                withTableName(opts.getOptStringRequired("table")).
                withConsistentRead(bConsistantRead).
                withExpressionAttributeNames(parseAttrNameExprs(opts)).
                withExpressionAttributeValues(parseAttrValueExprs(opts));


        int userLimit = opts.getOptInt("limit", 0);
        if (userLimit > 0)
            queryRequest.setLimit(Math.min(userLimit, kLIMIT));

        if( opts.hasOpt("index-name"))
            queryRequest.setIndexName( opts.getOptStringRequired("index-name"));
        if( opts.hasOpt("key-condition-expression"))
            queryRequest.setKeyConditionExpression(opts.getOptStringRequired("key-condition-expression"));
        if( opts.hasOpt("projection-expression"))
            queryRequest.setProjectionExpression( 
                    opts.getOptStringRequired("projection-expression"));             
        if (filterExpression != null)
            queryRequest.setFilterExpression(filterExpression);
        if( opts.hasOpt("select"))
            queryRequest.setSelect(parseSelect(opts.getOptStringRequired("select")));
        ArrayList<RequestMetrics> metrics = new ArrayList<RequestMetrics>(); 

        // start result eary due to looping 
 
        boolean bStarted = false ;
        do {
            traceCall("query");
            if (exclusiveStartKey != null)
                queryRequest.setExclusiveStartKey(exclusiveStartKey);
            QueryResult result;
            try {
                result =  getAWSClient().query(queryRequest);
            } catch (AmazonClientException e) {
                   return handleException(e);
            }
              finally {
                  if( bStarted )
                     endResult();
            }
            if( ! bStarted )
                bStarted = startResult();
            for( Map<String, AttributeValue> item :  result.getItems() )
                writeItem(item);
            metrics.add( new RequestMetrics(result.getCount(), result.getScannedCount(), result.getConsumedCapacity()));
            exclusiveStartKey = result.getLastEvaluatedKey();

        } while( exclusiveStartKey != null );

        writeMetrics( metrics );
        endResult();
        return 0;
    }

    private Select parseSelect(String select) {
        return Select.fromValue(select.toUpperCase());
    }

}

