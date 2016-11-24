package org.xmlsh.aws;

import java.io.IOException;
import java.util.List;
import javax.xml.stream.XMLStreamException;
import org.xmlsh.aws.util.AWSEC2Command;
import org.xmlsh.aws.util.AWSUtil;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.SafeXMLStreamWriter;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;
import org.xmlsh.core.io.OutputPort;
import org.xmlsh.util.Util;
import com.amazonaws.services.ec2.model.DisassociateAddressRequest;
import net.sf.saxon.s9api.SaxonApiException;

public class ec2DisassociateAddress extends AWSEC2Command {

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
      usage(null);
      return 1;
    }

    try {
      getEC2Client(opts);
    } catch (UnexpectedException e) {
      usage(e.getLocalizedMessage());
      return 1;

    }

    String ip = args.get(0).toString();

    int ret = disassociate(ip);

    return ret;

  }

  private int disassociate(String ip)
      throws IOException, XMLStreamException, SaxonApiException, CoreException {

    String raw_ip = AWSUtil.resolveDNS(ip);
    if(Util.isBlank(raw_ip))
      throw new InvalidArgumentException("Unknown IP address or format: " + ip);

    DisassociateAddressRequest request = new DisassociateAddressRequest(raw_ip);
    traceCall("disassociateAddress");
    getAWSClient().disassociateAddress(request);
    writeResult(raw_ip);

    return 0;
  }

  private void writeResult(String ip)
      throws IOException, XMLStreamException, SaxonApiException, CoreException {

    OutputPort stdout = this.getStdout();
    mWriter = new SafeXMLStreamWriter(
        stdout.asXMLStreamWriter(getSerializeOpts()));

    startDocument();
    startElement(this.getName());
    startElement("address");
    attribute("public-ip", ip);
    endElement();
    endElement();
    endDocument();
    closeWriter();

  }

}
