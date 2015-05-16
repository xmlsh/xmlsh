package org.xmlsh.aws;

import java.io.IOException;
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
import org.xmlsh.util.Util;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.UpdateItemRequest;
import com.amazonaws.services.dynamodbv2.model.UpdateItemResult;

public class ddbUpdateItem extends AWSDDBCommand {

    private boolean bQuiet = false;

    /**
     * @param args
     * @throws IOException
     */
    @Override
    public int run(List<XValue> args) throws Exception {
        Options opts = getOptions("update:,table=table-name:,key-name:+,key-value:+,key:,c=condition:,q=quiet,return=return-values:,attr-name-expr:+,attr-value-expr:+");

        // Options opts = getOptions("expected:+,q=quiet");
        opts.parse(args);
        setSerializeOpts(this.getSerializeOpts(opts));

        bQuiet = opts.hasOpt("quiet");

        try {
            getDDBClient(opts);
        } catch (UnexpectedException e) {
            usage(e.getLocalizedMessage());
            return 1;

        }

        int ret = -1;

        ret = update(opts);

        return ret;

    }

    private int update( Options opts) throws IOException,
            XMLStreamException, SaxonApiException, CoreException {
        String condition = opts.getOptString("condition", null);
        String returnValues = opts.getOptString("return-values", null);
        String tableName = opts.getOptStringRequired("table");
        Map<String, AttributeValue> key = parseKeyOptions(opts);
        
        String updateExpr = opts.getOptString("update", null);
        List<XValue> args = opts.getRemainingArgs();

        if (!args.isEmpty()) {
            if (updateExpr == null)
                updateExpr = args.remove(0).toString();
            else
                usage("Unexpected arguments");
        }

        if (!args.isEmpty() || Util.isBlank(updateExpr)) {
            usage("Update expression required");
            return 1;
        }

        UpdateItemRequest updateItemRequest = new UpdateItemRequest()
            .withTableName(tableName).withKey(key)
            .withUpdateExpression(updateExpr). 
            withExpressionAttributeNames(DDBTypes.parseAttrNameExprs(opts)).
            withExpressionAttributeValues(DDBTypes.parseAttrValueExprs(opts));
        
        if (condition != null)
            updateItemRequest.setConditionExpression(condition);
        if (returnValues != null)
            updateItemRequest.setReturnValues(returnValues);

        traceCall("updateItem");
               
        UpdateItemResult result ; 

        try {
            result = mAmazon.updateItem(updateItemRequest);
        } catch (AmazonClientException e) {
            return handleException(e);
        }

        if (!bQuiet) {

            startResult();
            if (result.getAttributes() != null) {
                writeItem(result.getAttributes());
            }
            writeMetric(  new RequestMetrics( result.getConsumedCapacity() , result.getItemCollectionMetrics() ));
 
            endResult();
        }

        return 0;

    }

}
