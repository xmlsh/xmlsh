package org.xmlsh.aws;

import java.io.IOException;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import net.sf.saxon.s9api.SaxonApiException;

import org.xmlsh.aws.util.AWSDDBCommand;
import org.xmlsh.aws.util.DDBTypes.IKeyAttrValueMap;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.Options;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;
import org.xmlsh.util.Util;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.dynamodbv2.model.UpdateItemRequest;
import com.amazonaws.services.dynamodbv2.model.UpdateItemResult;

public class ddbUpdateItem extends AWSDDBCommand {

    /**
     * @param args
     * @throws IOException
     */
    @Override
    public int run(List<XValue> args) throws Exception {
        Options opts = getOptions(sTABLE_OPTIONS, sATTR_EXPR_OPTIONS , sKEY_OPTIONS , sCONDITION_OPTIONS,sRETURN_OPTIONS , "update:");

        // Options opts = getOptions("expected:+,q=quiet");
        parseOptions(opts, args);
        setSerializeOpts(this.getSerializeOpts(opts));

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
        IKeyAttrValueMap key = parseKeyOptions(opts);
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
        withExpressionAttributeNames(parseAttrNameExprs(opts)).
        withExpressionAttributeValues(parseAttrValueExprs(opts));

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
