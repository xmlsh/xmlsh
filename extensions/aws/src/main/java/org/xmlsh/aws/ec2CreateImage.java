package org.xmlsh.aws;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import javax.xml.stream.XMLStreamException;
import org.xmlsh.aws.util.AWSEC2Command;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.Options;
import org.xmlsh.core.SafeXMLStreamWriter;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;
import org.xmlsh.core.io.OutputPort;
import com.amazonaws.services.ec2.model.BlockDeviceMapping;
import com.amazonaws.services.ec2.model.CreateImageRequest;
import com.amazonaws.services.ec2.model.CreateImageResult;
import net.sf.saxon.s9api.SaxonApiException;

public class ec2CreateImage extends AWSEC2Command {

  /**
   * @param args
   * @throws IOException
   * 
   * 
   */
  @Override
  public int run(List<XValue> args) throws Exception {

    Options opts = getOptions(
        "name:,description:,no-reboot,block-device-mapping:+");
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

    int ret = createImage(args.get(0).toString(),
        opts.getOptStringRequired("name"), opts);

    return ret;
  }

  private int createImage(String instance_id, String name, Options opts)
      throws IOException, XMLStreamException, SaxonApiException, CoreException {

    CreateImageRequest request = new CreateImageRequest(instance_id, name);
    if(opts.hasOpt("description"))
      request.setDescription(opts.getOptStringRequired("description"));

    if(opts.hasOpt("no-reboot"))
      request.setNoReboot(true);

    Collection<BlockDeviceMapping> blockDeviceMappings = getBlockDeviceMappings(
        opts);
    if(blockDeviceMappings != null)
      request.setBlockDeviceMappings(blockDeviceMappings);

    traceCall("createImage");

    CreateImageResult result = getAWSClient().createImage(request);

    writeResult(result);

    return 0;
  }

  private void writeResult(CreateImageResult result)
      throws IOException, XMLStreamException, SaxonApiException, CoreException {
    OutputPort stdout = this.getStdout();
    mWriter = new SafeXMLStreamWriter(
        stdout.asXMLStreamWriter(getSerializeOpts()));

    startDocument();
    startElement(this.getName());

    startElement("image");
    attribute("image-id", result.getImageId());
    endElement();

    endElement();
    endDocument();
    closeWriter();

    stdout.writeSequenceTerminator(getSerializeOpts());

  }

}
