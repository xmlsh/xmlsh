package org.xmlsh.aws;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import javax.xml.stream.XMLStreamException;
import org.xmlsh.aws.util.AWSSQSCommand;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.InputPort;
import org.xmlsh.core.Options;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;
import org.xmlsh.core.io.OutputPort;
import org.xmlsh.util.Util;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import net.sf.saxon.s9api.SaxonApiException;

public class sqsSendMessage extends AWSSQSCommand {

  /**
   * @param args
   * @throws IOException
   */
  @Override
  public int run(List<XValue> args) throws Exception {

    Options opts = getOptions("f=file:");
    parseOptions(opts, args);
    setSerializeOpts(this.getSerializeOpts(opts));

    args = opts.getRemainingArgs();

    if(args.size() < 1) {
      usage();
      return 1;
    }

    String url = args.get(0).toString();
    String body = null;

    // Get message from file
    if(opts.hasOpt("f")) {
      body = readMessage(mShell.getEnv().getInput(opts.getOptValue("f")));

    }
    else
      switch(args.size()){
      case 1:
        // Read from stdin
        body = readMessage(getStdin());
        break;
      case 2: {
        body = args.get(1).toString();
        break;
      }
      default: {
        usage();
        return 1;
      }
      }

    try {
      getSQSClient(opts);
    } catch (UnexpectedException e) {
      usage(e.getLocalizedMessage());
      return 1;

    }

    int ret;

    ret = send(url, body);

    return ret;

  }

  private String readMessage(InputPort input)
      throws CoreException, IOException {

    try (InputStream is = input.asInputStream(getSerializeOpts());) {
      return Util.readString(is, getSerializeOpts().getInputTextEncoding());
    }

  }

  private int send(String url, String body)
      throws IOException, XMLStreamException, SaxonApiException, CoreException {

    SendMessageRequest request = new SendMessageRequest(url, body);

    traceCall("sendMessage");

    SendMessageResult result = getAWSClient().sendMessage(request);

    OutputPort stdout = this.getStdout();
    mWriter = stdout.asXMLStreamWriter(getSerializeOpts());

    startDocument();
    startElement(getName());
    startElement("message");
    attribute("md5", result.getMD5OfMessageBody());
    attribute("id", result.getMessageId());
    endElement();
    endElement();
    endDocument();
    closeWriter();
    stdout.writeSequenceTerminator(getSerializeOpts());

    return 0;

  }

}
