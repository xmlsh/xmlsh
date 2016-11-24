package org.xmlsh.aws;

import java.io.IOException;
import java.util.List;
import javax.xml.stream.XMLStreamException;
import org.xmlsh.aws.util.AWSSQSCommand;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;
import com.amazonaws.services.sqs.model.ChangeMessageVisibilityRequest;
import net.sf.saxon.s9api.SaxonApiException;

public class sqsChangeMessageVisibility extends AWSSQSCommand {

  /**
   * @param args
   * @throws IOException
   */
  @Override
  public int run(List<XValue> args) throws Exception {

    Options opts = getOptions();
    parseOptions(opts, args);
    setSerializeOpts(this.getSerializeOpts(opts));

    args = opts.getRemainingArgs();

    if(args.size() != 3) {
      usage();
      return 1;
    }

    try {
      getSQSClient(opts);
    } catch (UnexpectedException e) {
      usage(e.getLocalizedMessage());
      return 1;

    }

    int ret;
    ret = changeVisibility(args.get(0).toString(), args.get(1).toString(),
        args.get(2).toInt());

    return ret;

  }

  private int changeVisibility(String url, String message, int vis)
      throws IOException, XMLStreamException, InvalidArgumentException,
      SaxonApiException {

    ChangeMessageVisibilityRequest request = new ChangeMessageVisibilityRequest();
    request.setQueueUrl(url);
    request.setReceiptHandle(message);
    request.setVisibilityTimeout(vis);

    traceCall("changeMessageVisibility");

    getAWSClient().changeMessageVisibility(request);

    return 0;

  }

}
