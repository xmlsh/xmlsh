package org.xmlsh.aws;

import java.io.IOException;
import java.util.List;

import org.xmlsh.aws.util.AWSS3Command;
import org.xmlsh.aws.util.AWSUtil;
import org.xmlsh.aws.util.S3Path;
import org.xmlsh.core.Options;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.CopyObjectResult;
import com.amazonaws.services.s3.model.ObjectMetadata;


public class s3cp extends AWSS3Command {


	/**
	 * @param args
	 * @throws IOException 
	 */
	@Override
	public int run(List<XValue> args) throws Exception {
		Options opts =  getOptions("m=metadata,a=acl,s=storage:,v=verbose,e=exists");
		opts.parse(args);
        args = opts.getRemainingArgs();
	    String storage = opts.getOptString("storage", null);

	    boolean     onlyIfExists = opts.hasOpt("e");
	    boolean     copyMetadata = opts.hasOpt("m");
	    boolean     copyAcl = opts.hasOpt("acl");
				
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
			ret = copy(  src , dest , storage , onlyIfExists , copyAcl , copyMetadata);
		} catch( Exception e ){
			mShell.printErr("Exception copying " + src + " to " + dest , e);
			ret = 1;
		}
		
		return ret;
	}

	private int copy(S3Path src, S3Path dest,String storage, boolean onlyIfExists, boolean copyAcl, boolean copyMetadata) 
	{
		AmazonS3Client client = getAWSClient();
		boolean bCopy = true ;
		if( onlyIfExists ){
			String destETag ;
			String sourceETag ;
			destETag = AWSUtil.getChecksum( client  , dest ); 
			
			if( destETag != null  ){
				sourceETag = AWSUtil.getChecksum( client , src  );
				if( sourceETag != null )
					if( destETag.equals(sourceETag))
						bCopy = false ;
			}
			if( ! bCopy )
			    return 0;
		}
		ObjectMetadata meta = copyMetadata ? client.getObjectMetadata(src.getBucket(), src.getKey()) : null ;
        AccessControlList acl = copyAcl ? client.getObjectAcl(src.getBucket(), src.getKey()) : null ;
        
			traceCall("copyObject");

            CopyObjectRequest request = new CopyObjectRequest(src.getBucket(), src.getKey(), 
			         dest.getBucket(), dest.getKey() );
            if( meta != null )
                request.setNewObjectMetadata(meta);
            if( storage != null )
                request.setStorageClass(storage);
            if( acl != null )
                request.setAccessControlList(acl);
			
			CopyObjectResult result = client.copyObject(request);
			return 0;
			
	}

	public void usage() {
		super.usage("Usage: s3cp source dest");
	}





	

}
