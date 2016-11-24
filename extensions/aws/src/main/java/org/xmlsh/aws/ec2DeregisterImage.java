package org.xmlsh.aws;

import java.io.IOException;
import java.util.List;
import javax.xml.stream.XMLStreamException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.aws.util.AWSEC2Command;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.Options;
import org.xmlsh.core.SafeXMLStreamWriter;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;
import org.xmlsh.core.io.OutputPort;
import org.xmlsh.util.Util;
import com.amazonaws.services.ec2.model.BlockDeviceMapping;
import com.amazonaws.services.ec2.model.DeleteSnapshotRequest;
import com.amazonaws.services.ec2.model.DeregisterImageRequest;
import com.amazonaws.services.ec2.model.DescribeImagesRequest;
import com.amazonaws.services.ec2.model.DescribeImagesResult;
import com.amazonaws.services.ec2.model.Image;
import net.sf.saxon.s9api.SaxonApiException;

public class ec2DeregisterImage extends AWSEC2Command {

  static Logger mLogger = LogManager.getLogger();

  /**
   * @param args
   * @throws IOException
   * 
   * 
   */
  @Override
  public int run(List<XValue> args) throws Exception {

    Options opts = getOptions();
    parseOptions(opts, args);

    args = opts.getRemainingArgs();

    setSerializeOpts(this.getSerializeOpts(opts));

    if(args.size() != 1) {
      usage(null);
      return 1;
    }

    setSerializeOpts(this.getSerializeOpts(opts));
    try {
      getEC2Client(opts);
    } catch (UnexpectedException e) {
      usage(e.getLocalizedMessage());
      return 1;

    }

    traceCall("deregisterImage");

    int ret = deregisterImage(args.get(0).toString(),
        opts.getOptFlag("delete-snapshot", false));

    return ret;
  }

  private int deregisterImage(String image, boolean deleteSnap)
      throws IOException, XMLStreamException, SaxonApiException, CoreException {

    mLogger.entry(image, deleteSnap);

    String snapshotID = null;
    DescribeImagesRequest request = new DescribeImagesRequest()
        .withImageIds(image);
    if(request != null) {
      DescribeImagesResult result = getAWSClient().describeImages(request);
      if(result != null) {
        if(result.getImages().size() == 1) {
          Image img = result.getImages().get(0);
          String rootName = img.getRootDeviceName();
          String rootType = img.getRootDeviceType();
          mLogger.debug("Found image root device: %1 type %2", rootName,
              rootType);
          if("ebs".equals(rootType)) {
            for(BlockDeviceMapping bm : img.getBlockDeviceMappings()) {
              if(bm.getDeviceName().equals(rootName)) {
                snapshotID = bm.getEbs().getSnapshotId();
                mLogger.info("Found image assocated snapshot: %1", snapshotID);
                break;
              }
            }
          }
        }
      }
    }

    DeregisterImageRequest deregisterImageRequest = new DeregisterImageRequest(
        image);
    getAWSClient().deregisterImage(deregisterImageRequest);

    boolean deleted = false;
    if(deleteSnap && !Util.isBlank(snapshotID)) {
      mLogger.info("deleting snapshot: %1 ", snapshotID);
      try {
        getAWSClient().deleteSnapshot(
            new DeleteSnapshotRequest().withSnapshotId(snapshotID));

        deleted = true;
      } catch (Exception e) {
        mLogger.catching(e);
      }
    }

    writeResult(image, snapshotID, deleted);

    return 0;
  }

  private void writeResult(String image, String snapshotID, boolean deleted)
      throws IOException, XMLStreamException, SaxonApiException, CoreException {
    OutputPort stdout = this.getStdout();
    mWriter = new SafeXMLStreamWriter(
        stdout.asXMLStreamWriter(getSerializeOpts()));

    startDocument();
    startElement(this.getName());
    attribute("image-id", image);
    attribute("snapshot-id", snapshotID);
    attribute("snapshot-deleted", deleted);

    endElement();
    endDocument();
    closeWriter();

    stdout.writeSequenceTerminator(getSerializeOpts());

  }

}
