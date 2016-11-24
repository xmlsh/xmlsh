package org.xmlsh.aws;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Date;
import java.util.List;
import javax.xml.stream.XMLStreamException;
import org.xmlsh.aws.util.AWSS3Command;
import org.xmlsh.aws.util.S3Path;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;
import org.xmlsh.core.io.OutputPort;
import org.xmlsh.util.StringPair;
import org.xmlsh.util.Util;
import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ResponseHeaderOverrides;
import com.amazonaws.services.s3.model.SSEAlgorithm;
import com.amazonaws.services.s3.model.SSECustomerKey;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.trans.XPathException;

public class s3GeneratePresignedUrl extends AWSS3Command {

  private OutputStream mOutput;

  /**
   * @param args
   * @throws IOException
   */
  @Override
  public int run(List<XValue> args) throws Exception {

    Options opts = getOptions(
        "expiration:,key:,sse-algorithm:,sse-customer-key:,sse-customer-key-algorithm:,conent-type:,content-md5:,kms=kms-cmk-id:,http-method:,response-header:+,zero-byte");
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
    if(args.size() != 1) {
      usage();
      return 1;
    }
    OutputPort outp = this.getStdout();
    mOutput = outp.asOutputStream(getSerializeOpts());

    S3Path path = new S3Path(args.get(0).toString());
    ret = generate(path, opts);

    return ret;

  }

  private int generate(S3Path path, Options opts)
      throws IOException, XMLStreamException, InvalidArgumentException,
      SaxonApiException, XPathException {

    GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(
        path.getBucket(), path.getKey());

    if(opts.hasOpt("expiration"))

      request.setExpiration(parseDate(opts.getOptValue("expiration")));

    if(opts.hasOpt("http-method"))
      request.setMethod(parseMethod(opts.getOptStringRequired("http-method")));

    if(opts.hasOpt("key"))
      request.setKey(opts.getOptStringRequired("key"));

    if(opts.hasOpt("sse-algorithm"))
      request.setSSEAlgorithm(
          SSEAlgorithm.fromString(opts.getOptStringRequired("sse-algorithm")));

    if(opts.hasOpt("sse-customer-key"))
      request.setSSECustomerKey(
          new SSECustomerKey(opts.getOptStringRequired("sse-customer-key")));

    if(opts.hasOpt("sse-customer-key-algorithm"))
      request.setSSECustomerKeyAlgorithm(SSEAlgorithm
          .fromString(opts.getOptStringRequired("sse-customer-key-algorithm")));

    if(opts.hasOpt("content-type"))
      request.setContentType(opts.getOptStringRequired("content-type"));

    if(opts.hasOpt("content-md5"))
      request.setContentType(opts.getOptStringRequired("content-md5"));

    if(opts.hasOpt("kms"))
      request.setKmsCmkId(opts.getOptStringRequired("kms"));

    if(opts.hasOpt("zero-byte"))
      request.setZeroByteContent(true);

    request.setResponseHeaders(
        parseResponseHeaders(opts.getOptValues("response-header")));

    traceCall("generatePresignedUrl");

    URL url = getAWSClient().generatePresignedUrl(request);

    mOutput.write(
        url.toString().getBytes(getSerializeOpts().getOutputTextEncoding()));
    mOutput.write(Util.getNewlineBytes(getSerializeOpts()));

    return 0;

  }

  private ResponseHeaderOverrides parseResponseHeaders(List<XValue> optValues) {
    if(optValues == null || optValues.isEmpty())
      return null;
    ResponseHeaderOverrides headers = new ResponseHeaderOverrides();
    for(XValue xv : optValues) {
      StringPair p = new StringPair(xv, '=');
      assert (p.hasLeft());
      if(p.hasLeft()) {
        switch(p.getLeft()){
        case ResponseHeaderOverrides.RESPONSE_HEADER_CONTENT_TYPE:
          headers.setContentType(p.getRight());
          break;
        case ResponseHeaderOverrides.RESPONSE_HEADER_CONTENT_LANGUAGE:
          headers.setContentLanguage(p.getRight());
          break;
        case ResponseHeaderOverrides.RESPONSE_HEADER_EXPIRES:
          headers.setExpires(p.getRight());
          break;
        case ResponseHeaderOverrides.RESPONSE_HEADER_CACHE_CONTROL:
          headers.setCacheControl(p.getRight());
          break;
        case ResponseHeaderOverrides.RESPONSE_HEADER_CONTENT_DISPOSITION:
          headers.setContentDisposition(p.getRight());
          break;
        case ResponseHeaderOverrides.RESPONSE_HEADER_CONTENT_ENCODING:
          headers.setContentEncoding(p.getRight());

        }

      }

    }
    return headers;

  }

  private HttpMethod parseMethod(String method) {

    return HttpMethod.valueOf(method);

  }

  private Date parseDate(XValue value) throws InvalidArgumentException {

    return (Date) value.convert(Date.class);

  }

}
