package org.xmlsh.aws;

import java.io.IOException;
import java.util.List;


import org.xmlsh.aws.util.AWSS3Command;
import org.xmlsh.aws.util.S3Path;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;
import org.xmlsh.util.Util;

import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.GetObjectMetadataRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;


public class s3SetObjectAcl extends AWSS3Command {

	

	/**
	 * @param args
	 * @throws IOException 
	 */
	@Override
	public int run(List<XValue> args) throws Exception {

		
		Options opts = getOptions("acl:");
		opts.parse(args);

		String acl = opts.getOptStringRequired("acl");
		
		args = opts.getRemainingArgs();
		
		mSerializeOpts = this.getSerializeOpts(opts);
		
		try {
			 getS3Client(opts);
		} catch (UnexpectedException e) {
			usage( e.getLocalizedMessage() );
			return 1;
			
		}
			
		int ret = 0 ;
		for( String arg : Util.toStringList(args)){
			S3Path path = new S3Path( arg );
			
			ret += setAcl( path , acl );
			
			
		}
		
		
		return ret;
		
	}






	

}
