package org.xmlsh.aws;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.xmlsh.aws.util.AWSS3Command;
import org.xmlsh.aws.util.S3Path;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.InputPort;
import org.xmlsh.core.Options;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;
import org.xmlsh.util.StringPair;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;


public class s3put extends AWSS3Command {


	private		boolean 	mOnlyIfExists = false ;

	/**
	 * @param args
	 * @throws IOException 
	 */
	@Override
	public int run(List<XValue> args) throws Exception {

		
		Options opts = getOptions("m=meta:+,s=storage:");
		opts.parse(args);

		args = opts.getRemainingArgs();
		
		mSerializeOpts = this.getSerializeOpts(opts);
		
		try {
			mAmazon = getS3Client(opts);
		} catch (UnexpectedException e) {
			usage( e.getLocalizedMessage() );
			return 1;
			
		}
		
		List<XValue> meta = opts.getOptValues("meta");
		
		String storage = opts.getOptString("storage", null);
		S3Path dest;
		InputPort src = null ;
		
		
		
		switch( args.size() ){
		case	1 :
				dest = new S3Path(args.get(0).toString());
				src = this.getStdin();
				break;
		case	2:
				dest = new S3Path(args.get(1).toString());
				src  = this.getInput(args.get(0));
				break ;
				
		default	:
			
			usage() ; 
			return 1; 
				
		}
		
		
		
		
		
		int ret = 0;
		try {
			ret = put(  src , dest , meta , storage  );
		} catch( Exception e ){
			mShell.printErr("Exception putting " + src + " to " + dest , e);
			ret = 1;
			
			
		}
			
		return ret;
		
	}

	private int put(InputPort src, S3Path dest, List<XValue> meta , String storage ) throws CoreException 
	{
		
		
		
		
		try {
			PutObjectRequest request;

			
			ObjectMetadata metadata = new ObjectMetadata();
			

			if( meta != null ){
				for( XValue xm : meta ){
					StringPair 	pair = new StringPair( xm.toString(), '=' );
					metadata.addUserMetadata(pair.getLeft(), pair.getRight());
					
					
					
				}
			}
			
			
			if( src.isFile() ){
				request = new PutObjectRequest( dest.getBucket() , dest.getKey() , src.getFile() );
				request.setMetadata(metadata);
			}
			else {
				InputStream is = src.asInputStream( mSerializeOpts );

				
				request = new PutObjectRequest( dest.getBucket() , dest.getKey() , is , metadata);
				
			}
			
			if( storage != null )
				request.setStorageClass(storage);
			
			
			// update metadata
			//
			request.setMetadata(metadata);
			
			PutObjectResult result = mAmazon.putObject(request);
			printResult(result);
			
			
		} finally {
			src.close();
			
		}
		
		return 0;
		
	}
	
	
	

	private void printResult(PutObjectResult result) {
		// TODO Auto-generated method stub
		
	}

	public void usage() {
		super.usage("Usage: s3put [source] dest");
	}





	

}
