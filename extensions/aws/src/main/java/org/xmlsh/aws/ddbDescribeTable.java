package org.xmlsh.aws;

import java.io.IOException;
import java.util.List;
import javax.xml.stream.XMLStreamException;
import org.xmlsh.aws.util.AWSDDBCommand;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.Options;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;
import com.amazonaws.AmazonClientException;
import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableResult;
import net.sf.saxon.s9api.SaxonApiException;

public class ddbDescribeTable extends AWSDDBCommand {

  /**
   * @param args
   * @throws IOException
   */
  @Override
  public int run(List<XValue> args) throws Exception {
    Options opts = getOptions(sTABLE_OPTIONS);
    parseOptions(opts, args);

    setSerializeOpts(this.getSerializeOpts(opts));
    args = opts.getRemainingArgs();

    if(args.size() != 0) {
      usage();
      return 1;
    }

    try {
      getDDBClient(opts);
    } catch (UnexpectedException e) {
      usage(e.getLocalizedMessage());
      return 1;

    }

    int ret = -1;
    ret = describe(opts);

    return ret;

  }

  private int describe(Options opts) throws IOException,
      XMLStreamException, SaxonApiException, CoreException {

    String tableName = opts.getOptStringRequired("table");

    DescribeTableRequest describeTableRequest = new DescribeTableRequest()
        .withTableName(tableName);

    traceCall("describeTable");
    DescribeTableResult result = null;
    try {
      result = getAWSClient().describeTable(describeTableRequest);
    } catch (AmazonClientException e) {
      return handleException(e);
    }
    startResult();
    writeTableDescription(result.getTable());
    endResult();

    return 0;

  }

}
