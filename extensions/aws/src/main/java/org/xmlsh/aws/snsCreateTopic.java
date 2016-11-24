package org.xmlsh.aws;

import java.io.IOException;
import java.util.List;
import javax.xml.stream.XMLStreamException;
import org.xmlsh.aws.util.AWSSNSCommand;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.Options;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;
import org.xmlsh.core.io.OutputPort;
import com.amazonaws.services.sns.model.CreateTopicRequest;
import com.amazonaws.services.sns.model.CreateTopicResult;
import net.sf.saxon.s9api.SaxonApiException;

public class snsCreateTopic extends AWSSNSCommand {

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

    if(args.size() != 1) {
      usage();
      return 1;
    }

    String name = args.get(0).toString();

    try {
      getSNSClient(opts);
    } catch (UnexpectedException e) {
      usage(e.getLocalizedMessage());
      return 1;

    }

    int ret;

    ret = create(name);

    return ret;

  }

  private int create(String name)
      throws IOException, XMLStreamException, SaxonApiException, CoreException {

    CreateTopicRequest request = new CreateTopicRequest();
    request.setName(name);
    traceCall("createTopic");

    CreateTopicResult result = getAWSClient().createTopic(request);

    OutputPort stdout = this.getStdout();
    mWriter = stdout.asXMLStreamWriter(getSerializeOpts());

    startDocument();
    startElement(getName());

    startElement("topic");
    attribute("arn", result.getTopicArn());

    endElement();

    endElement();
    endDocument();
    closeWriter();
    stdout.writeSequenceTerminator(getSerializeOpts());

    return 0;

  }

}
