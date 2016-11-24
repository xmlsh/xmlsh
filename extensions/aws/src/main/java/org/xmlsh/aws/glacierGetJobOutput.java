package org.xmlsh.aws;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import javax.xml.stream.XMLStreamException;
import org.xmlsh.aws.util.AWSGlacierCommand;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.Options;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;
import org.xmlsh.core.io.OutputPort;
import org.xmlsh.util.Util;
import com.amazonaws.services.glacier.model.DescribeJobRequest;
import com.amazonaws.services.glacier.model.DescribeJobResult;
import com.amazonaws.services.glacier.model.GetJobOutputRequest;
import com.amazonaws.services.glacier.model.GetJobOutputResult;
import net.sf.saxon.s9api.SaxonApiException;

public class glacierGetJobOutput extends AWSGlacierCommand {

  /**
   * @param args
   * @throws IOException
   */
  @Override
  public int run(List<XValue> args) throws Exception {

    Options opts = getOptions();
    parseOptions(opts, args);

    args = opts.getRemainingArgs();
    if(args.size() != 1)
      usage();

    setSerializeOpts(this.getSerializeOpts(opts));

    String vault = args.get(0).toString();
    String job = args.get(1).toString();

    try {
      getGlacierClient(opts);
    } catch (UnexpectedException e) {
      usage(e.getLocalizedMessage());
      return 1;

    }

    int ret = -1;
    ret = getOutput(vault, job);

    return ret;

  }

  private int getOutput(String vault, String job)
      throws IOException, XMLStreamException, SaxonApiException, CoreException,
      InterruptedException {

    OutputPort stdout = this.getStdout();

    DescribeJobRequest describeJobRequest = new DescribeJobRequest(vault, job);

    String status = null;
    DescribeJobResult describeResult = null;
    do {
      traceCall("describeJob");

      describeResult = getAWSClient().describeJob(describeJobRequest);

      status = describeResult.getStatusCode();

      mShell.printOut(status);
      if(!status.equals("InProgress"))
        break;

      Thread.sleep(10 * 1000);

    } while(true);

    if(status.equals("Succeeded")) {

      GetJobOutputRequest getJobOutputRequest = new GetJobOutputRequest(vault,
          job, null);

      GetJobOutputResult jobOutputResult = getAWSClient()
          .getJobOutput(getJobOutputRequest);
      InputStream jobOutput = jobOutputResult.getBody();
      Util.copyStream(jobOutput, stdout.asOutputStream(getSerializeOpts()));
      jobOutput.close();

    }

    return 0;
  }

  @Override
  public void usage() {
    super.usage();
  }

}
