package org.xmlsh.aws;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import net.sf.saxon.s9api.SaxonApiException;

import org.xmlsh.aws.util.AWSDDBCommand;
import org.xmlsh.aws.util.AWSDDBCommand.RequestMetrics;
import org.xmlsh.aws.util.DDBTypes;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;

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
        Options opts = getOptions("limit:,table=table-name:,key-name:+,key-value:+,key:+,query:" + 
            "attr-name-expr:,attr-value-expr:,c=consistant,filter=filter-expression:,select:,index-name:,key-condition-expression:,projection-expression:");
        opts.parse(args);

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

        OutputPort stdout = this.getStdout();
        mWriter = stdout.asXMLStreamWriter(getSerializeOpts());
        boolean bConsistantRead = opts.hasOpt("consistant");
        String filterExpression = opts.getOptString("filter", null);
        startDocument();
        startElement(getName());
        
        Map<String, AttributeValue> exclusiveStartKey =null;
        // placehoder for attribute values "v1" -> expr 
        Map<String, AttributeValue> attrValuesExpr=null;
        // placehoder for attribute names "v1" -> expr 
        Map<String, String> attrNamesExpr=null;
        
        if( opts.hasOpt("attr-name-expr"))
            attrNamesExpr = DDBTypes.parseAttrNameExprs( opts.getOptValues("attr-name-expr"));

        if( opts.hasOpt("attr-value-expr"))
            attrValuesExpr = parseAttrValueExprs( opts.getOptValues("attr-value-expr"));
        
        QueryRequest queryRequest = new QueryRequest().
                withTableName(opts.getOptStringRequired("table")).
                withConsistentRead(bConsistantRead);
        
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
        if (attrValuesExpr != null)
            queryRequest.setExpressionAttributeValues(attrValuesExpr);
        if (attrNamesExpr != null)
            queryRequest.setExpressionAttributeNames(attrNamesExpr);
        if( opts.hasOpt("select"))
            queryRequest.setSelect(parseSelect(opts.getOptStringRequired("select")));
        ArrayList<RequestMetrics> metrics = new ArrayList<RequestMetrics>();
        do {

            traceCall("query");
            if (exclusiveStartKey != null)
                queryRequest.setExclusiveStartKey(exclusiveStartKey);
            QueryResult result = mAmazon.query(queryRequest);
            for( Map<String, AttributeValue> item :  result.getItems() )
               writeItem(item);
            metrics.add( new RequestMetrics(result.getCount(), result.getScannedCount(), result.getConsumedCapacity()));

            exclusiveStartKey = result.getLastEvaluatedKey();
            
        } while( exclusiveStartKey != null );

        writeMetrics( metrics );
        
        endElement();
        endDocument();
        closeWriter();
        stdout.writeSequenceTerminator(getSerializeOpts());
        stdout.release();
        return 0;
    }

    private Select parseSelect(String select) {
        return Select.fromValue(select.toUpperCase());
    }

}

