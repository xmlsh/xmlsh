/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.aws.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.util.Map.Entry;

import javax.xml.stream.XMLStreamException;

import net.sf.saxon.s9api.SaxonApiException;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMReader;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.XValue;
import org.xmlsh.util.Util;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3EncryptionClient;
import com.amazonaws.services.s3.model.EncryptionMaterials;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;

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
	

	
	
	protected AmazonS3 getS3Client(Options opts) throws UnsupportedEncodingException, IOException, CoreException {
		
		if( opts.hasOpt("crypt")){
			
			synchronized( AWSS3Command.class  ){
				if( Security.getProperty(BouncyCastleProvider.PROVIDER_NAME) == null ) 
					Security.addProvider(new BouncyCastleProvider());
			}
			
			XValue sKeypair = opts.getOptValueRequired("keypair");

			
			KeyPair keyPair = (KeyPair) readPEM(sKeypair);
			
			return new AmazonS3EncryptionClient(
					new AWSPropertyCredentials( mShell, opts  ) ,
					new EncryptionMaterials( keyPair )
			
			);
			
			
		} else
			return new AmazonS3Client(
					new AWSPropertyCredentials( mShell, opts  ) 
			
			);
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
	

}

//
//
// Copyright (C) 2008,2009,2010,2011,2012 , David A. Lee.
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
