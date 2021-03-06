package org.xmlsh.aws;

import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;
import javax.xml.stream.XMLStreamException;
import org.xmlsh.aws.util.AWSSQSCommand;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.Options;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;
import org.xmlsh.core.io.OutputPort;
import org.xmlsh.util.Util;
import com.amazonaws.services.sqs.model.GetQueueAttributesRequest;
import com.amazonaws.services.sqs.model.GetQueueAttributesResult;
import net.sf.saxon.s9api.SaxonApiException;

public class sqsGetQueueAttributes extends AWSSQSCommand {

  /**
   * @param args
   * @throws IOException
   */
  @Override
  public int run(List<XValue> args) throws Exception {

    Options opts = getOptions();
    parseOptions(opts, args);

    args = opts.getRemainingArgs();

    setSerializeOpts(this.getSerializeOpts(opts));

    if(args.size() < 2) {
      usage();
      return 1;
    }

    String name = args.remove(0).toString();

    try {
      getSQSClient(opts);
    } catch (UnexpectedException e) {
      usage(e.getLocalizedMessage());
      return 1;

    }

    int ret;

    ret = list(name, Util.toStringList(args));

    return ret;

  }

  private int list(String name, List<String> attrNames)
      throws IOException, XMLStreamException, SaxonApiException, CoreException {

    OutputPort stdout = getStdout();
    mWriter = stdout.asXMLStreamWriter(getSerializeOpts());

    startDocument();
    startElement(getName());

    GetQueueAttributesRequest request = new GetQueueAttributesRequest(name);
    request.setAttributeNames(attrNames);
    traceCall("getQueueAttributes");

    GetQueueAttributesResult attrsResult = getAWSClient()
        .getQueueAttributes(request);
    for(Entry<String, String> attr : attrsResult.getAttributes().entrySet()) {
      startElement("attribute");
      attribute("name", attr.getKey());
      characters(attr.getValue());
      endElement();
    }

    endElement();
    endDocument();
    closeWriter();
    stdout.writeSequenceTerminator(getSerializeOpts());

    return 0;

  }

}
