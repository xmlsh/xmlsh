/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.aws.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.KeyPair;
import java.security.Security;
import java.util.Map.Entry;

import javax.xml.stream.XMLStreamException;

import net.sf.saxon.s9api.SaxonApiException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMReader;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.XValue;
import org.xmlsh.util.Util;

import com.amazonaws.regions.RegionUtils;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3EncryptionClient;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.EncryptionMaterials;
import com.amazonaws.services.s3.model.ListMultipartUploadsRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.StaticEncryptionMaterialsProvider;

public abstract class AWSS3Command extends AWSCommand {
	

	protected		AmazonS3 mAmazon ;
	public String sMetaDataElem = "metadata";
	public String sUserMetaDataElem = "user";
	
	public AWSS3Command() {
		super();
	}

	protected String getCommonOpts() {
		return super.getCommonOpts() + ",crypt,keypair:" ;
	}
	
	protected Object getClient() {
		return mAmazon; 
	}

	
	
	protected void getS3Client(Options opts) throws UnsupportedEncodingException, IOException, CoreException {
		
		if( opts.hasOpt("crypt")){
			
			synchronized( AWSS3Command.class  ){
				if( Security.getProperty(BouncyCastleProvider.PROVIDER_NAME) == null ) 
					Security.addProvider(new BouncyCastleProvider());
			}
			
			XValue sKeypair = opts.getOptValueRequired("keypair");

			
			KeyPair keyPair = (KeyPair) readPEM(sKeypair);
			
			mAmazon =  new AmazonS3EncryptionClient(
					new AWSCommandCredentialsProviderChain( mShell, opts  ) ,
					new StaticEncryptionMaterialsProvider(
							new EncryptionMaterials( keyPair ))
			
			);
			
			
		} else
			mAmazon =  new AmazonS3Client(
					new AWSCommandCredentialsProviderChain( mShell, opts  ) 
			
			);
		
		setEndpoint(opts);
		setRegion(opts);

	}
	
	/* (non-Javadoc)
	 * @see org.xmlsh.aws.util.AWSCommand#setRegion(java.lang.String)
	 */
	@Override
	public void setRegion(String region) {
	    mAmazon.setRegion( RegionUtils.getRegion(region));
		
	}
	

	private Object readPEM(XValue sPrivate) throws IOException, UnsupportedEncodingException,
			CoreException {
		PEMReader reader = new PEMReader( getInput(sPrivate).asReader( this.mSerializeOpts ));
		Object obj = reader.readObject();
		reader.close();
		return obj;
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
				
				
				
				startDocument();
				startElement(sMetaDataElem);
				
				
				attribute("cache-control" , m.getCacheControl() );
				attribute("content-disposition" ,m.getContentDisposition() );
				attribute("content-encoding" , m.getContentEncoding() );
				attribute("md5" , m.getContentMD5() );
				attribute("etag" , m.getETag() );
				attribute("version-id" , m.getVersionId() );
				attribute("content-length" , String.valueOf(m.getContentLength()) );
				attribute("last-modified" , Util.formatXSDateTime(m.getLastModified()) );
				
				startElement("user-metadata");
				for( Entry<String, String> user : m.getUserMetadata().entrySet()  ){
					startElement(sUserMetaDataElem);
					attribute("name", user.getKey() );
					attribute("value", user.getValue() );
					endElement();
					
				
				}
				endElement();
				endElement();
				endDocument();
			
				
				
				
				
				
	}
	
	
	protected S3Path getPath( String bucket , String key )
	{
		if( Util.isBlank(bucket) )
			return new S3Path( key );
		else
			return new S3Path( bucket , key );
	}
	

	@Override
    public void setEndpoint( String endpoint )
    {
    	mAmazon.setEndpoint( endpoint );
    }

	protected CannedAccessControlList getAcl(String acl) {
		
		for(CannedAccessControlList c : CannedAccessControlList.values())
			if( c.toString().equals(acl))
				return c;
		return null ;
		
	}

	protected int setAcl(S3Path src, String acl) throws CoreException, IOException,
			XMLStreamException, SaxonApiException {
		  
         traceCall("setObjectAcl");

		mAmazon.setObjectAcl(src.getBucket(),src.getKey(), getAcl(acl));
		return 0;
		
	}
	

}

//
//
// Copyright (C) 2008-2013    David A. Lee.
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
