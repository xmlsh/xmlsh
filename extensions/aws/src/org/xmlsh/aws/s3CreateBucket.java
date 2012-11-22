package org.xmlsh.aws;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import net.sf.saxon.s9api.SaxonApiException;
import org.xmlsh.aws.util.AWSS3Command;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;

import com.amazonaws.services.s3.model.DeleteBucketRequest;


public class s3CreateBucket extends AWSS3Command {

	


	/**
	 * @param args
	 * @throws IOException 
	 */
	@Override
	public int run(List<XValue> args) throws Exception {

		
		Options opts = getOptions("r=random_name");
		opts.parse(args);

		args = opts.getRemainingArgs();
		boolean bRandom = opts.hasOpt("r");
		
		mSerializeOpts = this.getSerializeOpts(opts);
		
		try {
			mAmazon = getS3Client(opts);
		} catch (UnexpectedException e) {
			usage( e.getLocalizedMessage() );
			return 1;
			
		}


		int ret = 0 ;
		if(args.size() != 1 ){
			System.out.println("args count " + args.size());
				usage();
				return 1;
		}

		String bucketName = args.get(0).toString();
		if( bRandom ) {
			SecureRandom random = new SecureRandom();
			bucketName = bucketName + new BigInteger(130, random).toString(32);
		}
		
		return createBucket(bucketName);
		
	}


	private int createBucket(String bucketName) {
		
		int ret  = 0;
		try {
			mAmazon.createBucket(bucketName);
		} catch (Exception e) {
			mShell.printErr("Exception creating " + bucketName , e);
			ret = 1;
		}
		
		if (ret == 0) mShell.printOut(bucketName);
		return  ret;
		
	}

	public void usage() {
		super.usage("Usage: s3CreateBucket [-r prefix | bucketname ] ");
	}


}
