package org.xmlsh.aws;

import org.xmlsh.aws.util.AWSS3Command;
import org.xmlsh.aws.util.AWSUtil;
import org.xmlsh.aws.util.S3Path;
import org.xmlsh.core.Options;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;

import java.io.IOException;
import java.util.List;

import com.amazonaws.services.s3.model.CopyObjectResult;


public class s3cp extends AWSS3Command {

	

	private		boolean 	mOnlyIfExists = false ;

	/**
	 * @param args
	 * @throws IOException 
	 */
	@Override
	public int run(List<XValue> args) throws Exception {

		
		Options opts = getOptions();
		opts.parse(args);

		args = opts.getRemainingArgs();
		
		
		try {
			 getS3Client(opts);
		} catch (UnexpectedException e) {
			usage( e.getLocalizedMessage() );
			return 1;
			
		}
		
		
		
		if( args.size() != 2 ){
				usage();
				return 1;
		}

		S3Path src = new S3Path(args.get(0).toString());
		S3Path dest = new S3Path(args.get(1).toString());
		
		int ret = 0;
		try {
			ret = copy(  src , dest );
		} catch( Exception e ){
			mShell.printErr("Exception copying " + src + " to " + dest , e);
			ret = 1;
			
			
		}
			
		
	
		
		return ret;
		
		
	}


	private int copy(S3Path src, S3Path dest) 
	{
		
		
		boolean bCopy = true ;
		
		
		if( mOnlyIfExists ){
			String destETag ;
			String sourceETag ;
			destETag = AWSUtil.getChecksum(mAmazon , dest ); 
			
			if( destETag != null  ){
				sourceETag = AWSUtil.getChecksum( mAmazon , src  );
				if( sourceETag != null )
					if( destETag.equals(sourceETag))
						bCopy = false ;
			}
		}
		
		
		if( bCopy ){
			
			traceCall("copyObject");

			CopyObjectResult result = mAmazon.copyObject(src.getBucket(), src.getKey(), dest.getBucket(), dest.getKey() );
			
			return 0;
			
			
		}	else
			return 0;

	
	}


	public void usage() {
		super.usage("Usage: s3cp source dest");
	}





	

}
