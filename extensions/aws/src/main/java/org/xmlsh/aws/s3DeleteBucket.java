package org.xmlsh.aws;

import java.io.IOException;
import java.util.List;
import javax.xml.stream.XMLStreamException;
import org.xmlsh.aws.util.AWSS3Command;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;
import com.amazonaws.services.s3.model.DeleteBucketRequest;
import net.sf.saxon.s9api.SaxonApiException;

public class s3DeleteBucket extends AWSS3Command {

  /**
   * @param args
   * @throws IOException
   */
  @Override
  public int run(List<XValue> args) throws Exception {

    Options opts = getOptions("r=recurse");
    parseOptions(opts, args);

    args = opts.getRemainingArgs();

    setSerializeOpts(this.getSerializeOpts(opts));

    try {
      getS3Client(opts);
    } catch (UnexpectedException e) {
      usage(e.getLocalizedMessage());
      return 1;

    }

    int ret = 0;
    if(args.size() < 1) {
      if(getBucket() == null)
        usage();
      else
        args = XValue.newXValue(getBucket()).asXList();
    }

    for(XValue arg : args)
      ret += deleteBucket(arg.toString());

    return ret;

  }

  private int deleteBucket(String bucket) throws IOException,
      XMLStreamException, InvalidArgumentException, SaxonApiException {

    DeleteBucketRequest request = new DeleteBucketRequest(bucket);

    traceCall("deleteBucket");

    getAWSClient().deleteBucket(request);

    return 0;

  }

}
