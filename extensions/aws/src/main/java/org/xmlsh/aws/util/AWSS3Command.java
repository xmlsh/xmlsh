/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.aws.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map.Entry;

import javax.xml.stream.XMLStreamException;

import net.sf.saxon.s9api.SaxonApiException;

import org.xmlsh.aws.clients.S3Client;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.XValue;
import org.xmlsh.util.Util;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ListMultipartUploadsRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ListVersionsRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.transfer.TransferManager;

public abstract class AWSS3Command extends AWSCommand<AmazonS3Client> {


    private String mBucket = null;
    private String mKey = null ;

	protected String getBucket() {
        return mBucket;
    }

	 public S3Path getS3Path() {
	    return getS3Client().getS3Path(mBucket , mKey );
	 }
    public S3Path getS3Path(String keyOrArg) {
      return  getS3Client().getS3Path( mBucket ,
          Util.isBlank(keyOrArg) ? mKey : keyOrArg );
    }

    public S3Path getS3Path(XValue keyOrArg) {
      return  getS3Path( mBucket , keyOrArg == null ? mKey :  keyOrArg.toString()  );
  }
	public S3Path getS3Path(String bucket, String key) {
		return getS3Client().getS3Path(
		        Util.isBlank(bucket)? mBucket : bucket ,
		        Util.isBlank(key) ? mKey : key);
	}

	private S3Client getS3Client() {
		return (S3Client) getClient();
	}

	public CannedAccessControlList getAcl(String acl) {
		return getS3Client().getAcl(acl);
	}

	public int setAcl(S3Path src, String acl) throws CoreException,
			IOException, XMLStreamException, SaxonApiException {
		return getS3Client().setAcl(src, acl);
	}

	public TransferManager getTransferManager() {
		return getS3Client().getTransferManager();
	}

	public void shutdownTransferManager() {
		getS3Client().shutdownTransferManager();
	}

	public String sMetaDataElem = "metadata";
	public String sUserMetaDataElem = "user";

	public AWSS3Command() {
		super();
	}

	@Override
	protected String getCommonOpts() {
		return super.getCommonOpts() + ",crypt,keypair:,threads:,bucket:,key:" ;
	}



	protected void getS3Client(Options opts) throws UnsupportedEncodingException, IOException, CoreException {

	   setAmazon(AWSClientFactory.newS3Client(  mShell , opts ));
	   mBucket = opts.getOptString("bucket", mBucket );

	}



  protected ListVersionsRequest getListVersionsRequest(S3Path path, String delim) {
    ListVersionsRequest req = new ListVersionsRequest();
    req.setBucketName(path.getBucket());
    if( ! Util.isBlank(delim))
      req.setDelimiter(delim);
    if( ! Util.isBlank(path.getPrefix()))
      req.setPrefix(path.getPrefix());

    return req;


  }

	protected ListObjectsRequest getListRequest(S3Path path, String delim) {
		ListObjectsRequest req = new ListObjectsRequest();
		req.setBucketName(path.getBucket());
		if( ! Util.isBlank(delim))
			req.setDelimiter(delim);
		if( ! Util.isBlank(path.getPrefix()))
			req.setPrefix(path.getPrefix());
		return req;


	}
	protected ListMultipartUploadsRequest getListMultipartRequest(S3Path path, String delim) {
		ListMultipartUploadsRequest req = new ListMultipartUploadsRequest(path.getBucket());
		if( ! Util.isBlank(delim))
			req.setDelimiter(delim);
		if( ! Util.isBlank(path.getPrefix()))
			req.setPrefix(path.getPrefix());
		return req;

	}

	protected void writeMeta(ObjectMetadata m) throws InvalidArgumentException, XMLStreamException,
	SaxonApiException {

	  mLogger.entry(m);
		startElement(sMetaDataElem);


		attribute("cache-control" , m.getCacheControl() );
		attribute("content-disposition" ,m.getContentDisposition() );
		attribute("content-encoding" , m.getContentEncoding() );
		attribute("md5" , m.getContentMD5() );
		attribute("etag" , m.getETag() );
		attribute("version-id" , m.getVersionId() );
		attribute("content-length" , String.valueOf(m.getContentLength()) );
		attribute("last-modified" , Util.formatXSDateTime(m.getLastModified()) );
		attribute("expiration-time" , Util.formatXSDateTime(m.getExpirationTime()));
		attribute("expiration-time-rule-id" , m.getExpirationTimeRuleId() );
		attribute("httpExpiresDate" , Util.formatXSDateTime(m.getHttpExpiresDate()));
		attribute("ongoingRestore" , m.getOngoingRestore());
    attribute("restore-expiration-time" , Util.formatXSDateTime(m.getRestoreExpirationTime()));
    attribute("instance-length" , m.getInstanceLength() );
    attribute("sse-algorithm" , m.getSSEAlgorithm());
    attribute("sse-aws-kms-key-id" , m.getSSEAwsKmsKeyId());
    attribute("sse-customer-algorithm" , m.getSSECustomerAlgorithm());
    attribute("sse-customer-key-md5" , m.getSSECustomerKeyMd5());
    attribute("storage-class",m.getStorageClass());

		startElement("user-metadata");
		for( Entry<String, String> user : m.getUserMetadata().entrySet()  ){
			startElement(sUserMetaDataElem);
			attribute("name", user.getKey() );
			attribute("value", user.getValue() );
			endElement();

		}
		endElement();
		endElement();

		mLogger.exit();
	}




}

//
//
// Copyright (C) 2008-2014    David A. Lee.
//
// The contents of this file are subject to the "Simplified BSD License" (the
// "License");
// you may not use this file except in compliance with the License. You may
// obtain a copy of the
// License at http://www.opensource.org/licenses/bsd-license.php
//
// Software distributed under the License is distributed on an "AS IS" basis,
// WITHOUT WARRANTY OF ANY KIND, either express or implied.
// See the License for the specific language governing rights and limitations
// under the License.
//
// The Original Code is: all this file.
//
// The Initial Developer of the Original Code is David A. Lee
//
// Portions created by (your name) are Copyright (C) (your legal entity). All
// Rights Reserved.
//
// Contributor(s): none.
//
