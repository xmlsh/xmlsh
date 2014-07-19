package org.xmlsh.aws;

import org.xmlsh.aws.util.AWSS3Command;
import org.xmlsh.core.Options;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;

import java.io.IOException;
import java.util.List;


public class s3CreateBucket extends AWSS3Command {

	


	/**
	 * @param args
	 * @throws IOException 
	 */
	@Override
	public int run(List<XValue> args) throws Exception {

		
		Options opts = getOptions( );
		opts.parse(args);

		args = opts.getRemainingArgs();
		
		setSerializeOpts(this.getSerializeOpts(opts));
		
		try {
			 getS3Client(opts);
		} catch (UnexpectedException e) {
			usage( e.getLocalizedMessage() );
			return 1;
			
		}


		if(args.size() != 1 ){
				usage();
				return 1;
		}

		String bucketName = args.get(0).toString();

		return createBucket(bucketName);
		
	}


	private int createBucket(String bucketName) {
		
		int ret  = 0;
		try {
			traceCall("createBucket");

			mAmazon.createBucket(bucketName);
		} catch (Exception e) {
			mShell.printErr("Exception creating " + bucketName , e);
			ret = 1;
		}
		
		return  ret;
		
	}

	public void usage() {
		super.usage("Usage: s3CreateBucket  bucketname");
	}


}
