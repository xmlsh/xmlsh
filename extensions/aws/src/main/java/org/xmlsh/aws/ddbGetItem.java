package org.xmlsh.aws;

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import javax.xml.stream.XMLStreamException;
import org.xmlsh.aws.util.AWSDDBCommand;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.Options;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;
import org.xmlsh.util.Util;
import com.amazonaws.AmazonClientException;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.trans.XPathException;

public class ddbGetItem extends AWSDDBCommand {

  /**
   * @param args
   * @throws IOException
   */
  @Override
  public int run(List<XValue> args) throws Exception {
    Options opts = getOptions(sTABLE_OPTIONS, sKEY_OPTIONS, sRETURN_OPTIONS,
        sDOCUMENT_OPTS, sATTR_NAME_EXPR_OPTIONS, sCONSISTANT_OPTS,
        "projection-expression:");
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
    if(opts.hasOpt("json") || opts.hasOpt("document"))
      ret = getDocument(opts);
    else
      ret = getItem(opts);
    return ret;
  }

  private int getDocument(Options opts) throws XPathException, IOException,
      XMLStreamException, SaxonApiException, CoreException {
    String tableName = opts.getOptStringRequired("table");
    DynamoDB dynamoDB = super.getDynamotDB(opts);
    Table table = dynamoDB.getTable(tableName);
    GetItemSpec itemSpec = parseGetItemSpec(opts);

    PrintStream ostream = mShell.getEnv().getStdout()
        .asPrintStream(getSerializeOpts());
    try {
      Item item = table.getItem(itemSpec);
      ostream.print(item.toJSONPretty());
    } catch (AmazonClientException e) {
      return handleException(e);
    } finally {
      ostream.close();
    }
    return 0;

  }

  private int getItem(Options opts)
      throws XMLStreamException, SaxonApiException, IOException, CoreException {

    String tableName = opts.getOptStringRequired("table");
    Map<String, AttributeValue> keys = parseKeyOptions(opts);
    boolean bConsistantRead = opts.hasOpt("consistant");

    GetItemRequest getItemRequest = new GetItemRequest()
        .withTableName(tableName).withKey(keys)
        .withConsistentRead(bConsistantRead)
        .withExpressionAttributeNames(parseAttrNameExprs(opts));

    if(opts.hasOpt("projection-expression"))
      getItemRequest.setProjectionExpression(
          opts.getOptStringRequired("projection-expression"));

    else {
      if(opts.hasRemainingArgs())
        getItemRequest.setProjectionExpression(
            Util.stringJoin(Util.toStringList(opts.getRemainingArgs()), ","));
    }

    traceCall("getItem");

    GetItemResult result;
    try {
      result = getAWSClient().getItem(getItemRequest);
    } catch (AmazonClientException e) {
      return handleException(e);
    }

    startResult();
    if(result.getItem() != null)
      writeItem(result.getItem());
    writeMetric(new RequestMetrics(result.getConsumedCapacity(), null));
    endResult();

    return 0;

  }
}
