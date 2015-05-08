package org.xmlsh.aws;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import net.sf.saxon.s9api.SaxonApiException;

import org.xmlsh.aws.util.AWSDDBCommand;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;
import org.xmlsh.util.Util;

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
        Options opts = getOptions("table=table-name:,key-name:+,key-value:+,key:,c=condition:,q=quiet,return=return-values:,attr-name-expr:+,attr-value-expr:+");

        // Options opts = getOptions("expected:+,q=quiet");
        opts.parse(args);

        args = opts.getRemainingArgs();
        opts.parse(args);
        String tableName = opts.getOptStringRequired("table");
        Map<String, AttributeValue> attrs = parseKeyOptions(opts);
        
        // Exprs are constants used in expressions like
        //  Update SET #a1 = :v1  
        // placehoder for attribute values "v1" -> expr 
        Map<String, AttributeValue> attrValuesExpr=null;
        // placehoder for attribute names "v1" -> expr 
        Map<String, String> attrNamesExpr=null;
        
        
        if( opts.hasOpt("attr-name-expr"))
            attrNamesExpr = parseAttrNameExprs( opts.getOptValues("attr-name-expr"));

        if( opts.hasOpt("attr-value-expr"))
            attrValuesExpr = parseAttrValueExprs( opts.getOptValues("attr-name-expr"));
        


        String updateExpr = opts.getOptString("update", null);
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

        mSerializeOpts = this.getSerializeOpts(opts);

        bQuiet = opts.hasOpt("quiet");

        try {
            getDDBClient(opts);
        } catch (UnexpectedException e) {
            usage(e.getLocalizedMessage());
            return 1;

        }

        int ret = -1;

        ret = update(tableName, attrs, updateExpr,
                opts.getOptString("condition", null),
                opts.getOptString("return-values", null), attrValuesExpr,
                attrNamesExpr);

        return ret;

    }

    private int update(String tableName, Map<String, AttributeValue> key,
            String updateExpr, String condition, String returnValues,
            Map<String, AttributeValue> attrValuesExpr,
            Map<String, String> attrNamesExpr) throws IOException,
            XMLStreamException, SaxonApiException, CoreException {

        UpdateItemRequest updateItemRequest = new UpdateItemRequest()
        .withTableName(tableName).withKey(key)
        .withUpdateExpression(updateExpr);
        if (condition != null)
            updateItemRequest.setConditionExpression(condition);
        if (returnValues != null)
            updateItemRequest.setReturnValues(returnValues);
        if (attrValuesExpr != null)
            updateItemRequest.setExpressionAttributeValues(attrValuesExpr);
        if (attrNamesExpr != null)
            updateItemRequest.setExpressionAttributeNames(attrNamesExpr);

        traceCall("updateItem");

        UpdateItemResult result = mAmazon.updateItem(updateItemRequest);

        if (!bQuiet) {
            OutputPort stdout = this.getStdout();
            mWriter = stdout.asXMLStreamWriter(mSerializeOpts);
            startDocument();
            startElement(getName());

            if (result.getAttributes() != null) {
                writeItem(result.getAttributes());
            }

            if (result.getConsumedCapacity() != null) {
                writeConsumedCapacity(result.getConsumedCapacity());
            }
            if (result.getItemCollectionMetrics() != null) {
                writeItemCollectionMetrics(result.getItemCollectionMetrics());
            }
            endElement();
            endDocument();
            closeWriter();
            stdout.writeSequenceTerminator(mSerializeOpts);
            stdout.release();
        }

        return 0;

    }

}
