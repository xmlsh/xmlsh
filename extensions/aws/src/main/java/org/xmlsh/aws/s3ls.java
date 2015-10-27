package org.xmlsh.aws;

import java.io.IOException;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import net.sf.saxon.s9api.SaxonApiException;

import org.xmlsh.aws.util.AWSS3Command;
import org.xmlsh.aws.util.S3Path;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.Options;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;
import org.xmlsh.util.Util;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ListMultipartUploadsRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.MultipartUpload;
import com.amazonaws.services.s3.model.MultipartUploadListing;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public class s3ls extends AWSS3Command {

  private boolean bLongListing;
  private String mDelim = S3Path.kDEF_DELIM;
  private boolean bMetadata;
  private boolean bRecurse = false;

  /**
   * @param args
   * @throws IOException
   */
  @Override
  public int run(List<XValue> args) throws Exception {

    Options opts = getOptions("delim:,r=recurse,l=long,lm=long-metadata,m=multipart,meta=metadata");
    parseOptions(opts, args);

    args = opts.getRemainingArgs();

    setSerializeOpts(this.getSerializeOpts(opts));

    bLongListing = opts.hasOpt("l") || opts.hasOpt("lm");
    bMetadata = opts.hasOpt("metadata") || opts.hasOpt("lm");
    

    try {
      getS3Client(opts);
    } catch (UnexpectedException e) {
      usage(e.getLocalizedMessage());
      return 1;

    }

    mDelim = opts.getOptString("delim", S3Path.kDEF_DELIM);
    bRecurse = opts.hasOpt("r");

    int ret;
    
    
    S3Path s3path = null ;
    switch (args.size()) {
    case 0:
      s3path = getS3Path();
      break;
    case 1:
      s3path = getS3Path( args.get(0));
      break;
    default:
      usage();
      return 1;
    }
    
    if( ! s3path.hasBucket() )
     ret = listBuckets();
    else
    if (opts.hasOpt("m"))
      ret = listMultipart(s3path);
    else
      ret = list(s3path);
    return ret;

  }

  private int listBuckets()
      throws IOException, XMLStreamException, SaxonApiException, CoreException {

    startResult();

    traceCall("listBuckets");

    List<Bucket> buckets = getAWSClient().listBuckets();
    for (Bucket bucket : buckets) {
      startElement("bucket");
      attribute("name", bucket.getName());
      if (bLongListing) {
        attribute("create-date",
            Util.formatXSDateTime(bucket.getCreationDate()));
        attribute("owner", bucket.getOwner().getDisplayName());

      }
      if( bMetadata )
        ;

      endElement();

    }

    endResult();
    return 0;

  }

  private int listMultipart(S3Path path)
      throws XMLStreamException, IOException, SaxonApiException, CoreException {

    if (!path.hasBucket()) {
      usage();
      return 1;

    }

    startResult();
    
    writePath(path);

    ListMultipartUploadsRequest request = getListMultipartRequest(path, this.bRecurse ? null : mDelim);
    MultipartUploadListing list = getAWSClient().listMultipartUploads(request);

    do {

      List<MultipartUpload> uploads = list.getMultipartUploads();
      for (MultipartUpload obj : uploads) {
        startElement("file");
        attribute("key", obj.getKey());
        if (bLongListing) {

          attribute("initiated",
              Util.formatXSDateTime(obj.getInitiated()));
          attribute("initiator", obj.getInitiator().getId());
          attribute("uploadId", obj.getUploadId());
          attribute("owner", obj.getOwner().getDisplayName());
          attribute("storage-class", obj.getStorageClass());
        }
        endElement();

      }
      if (list.isTruncated()) {
        request.setUploadIdMarker(list.getUploadIdMarker());
        request.setKeyMarker(list.getKeyMarker());
        list = getAWSClient().listMultipartUploads(request);
      } else
        break;
    } while (true);

    endResult();

    return 0;

  }

  private int list(S3Path path) throws IOException, XMLStreamException,
  SaxonApiException, AmazonClientException, CoreException {

    if (!path.hasBucket()) {
      usage();
      return 1;

    }

    startResult();
    writePath(path);

    ListObjectsRequest request = getListRequest(path, bRecurse?null: mDelim);
    traceCall("listObjects");

    ObjectListing list = getAWSClient().listObjects(request);

    do {

      List<String> prefixes = list.getCommonPrefixes();
      if (prefixes != null && prefixes.size() > 0) {
        for (String p : prefixes) {
          startElement("directory");
          attribute("name", p);
          endElement();
        }
      }

      List<S3ObjectSummary> objs = list.getObjectSummaries();
      for (S3ObjectSummary obj : objs) {
        startElement("file");
        attribute("key", obj.getKey());
        attribute("type", S3Path.isDirectory(obj.getKey(), mDelim ) ? "directory" : "file" ); 
        if (bLongListing) {

          attribute("size", Long.toString(obj.getSize()));
          attribute("md5", obj.getETag());
          attribute("mod-date",
              Util.formatXSDateTime(obj.getLastModified()));
          attribute("owner", obj.getOwner().getDisplayName());
          attribute("storage-class", obj.getStorageClass());
        }
        if( bMetadata ){
          ObjectMetadata data = getAWSClient().getObjectMetadata( path.getBucket()  , obj.getKey()  );
          writeMeta( data );
        }
        
        endElement();

      }
      if (list.isTruncated()) {
        // String marker = list.getNextMarker();
        list = getAWSClient().listNextBatchOfObjects(list);
      } else
        break;
    } while (true);

    endResult();
    return 0;
  }

  private void writePath(S3Path path)
      throws XMLStreamException {
    attribute("bucket", path.getBucket());
    attribute("prefix", Util.notNull(path.getPrefix()));
    attribute("delim", Util.notNull(mDelim));
  }

  @Override
  public void usage() {
    super.usage("Usage: s3ls [options] [bucket/prefix]");
  }

}
