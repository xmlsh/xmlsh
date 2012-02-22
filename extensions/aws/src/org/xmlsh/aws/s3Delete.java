package org.xmlsh.aws;

import java.io.IOException;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import net.sf.saxon.s9api.SaxonApiException;
import org.xmlsh.aws.util.AWSS3Command;
import org.xmlsh.aws.util.S3Path;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;
import org.xmlsh.util.Util;

import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;


public class s3Delete extends AWSS3Command {

	


	/**
	 * @param args
	 * @throws IOException 
	 */
	@Override
	public int run(List<XValue> args) throws Exception {

		
		Options opts = getOptions("b=bucket:,r=recurse");
		opts.parse(args);

		args = opts.getRemainingArgs();
		
		String bucket = opts.getOptString("b", null);
		boolean bRecurse = opts.hasOpt("r");
		
		mSerializeOpts = this.getSerializeOpts(opts);
		
		
		
		try {
			mAmazon = getS3Client(opts);
		} catch (UnexpectedException e) {
			usage( e.getLocalizedMessage() );
			return 1;
			
		}


		int ret = 0 ;
		if(args.size() < 1 ){
				usage();
				return 1;
		}

		for( XValue arg : args ){
			ret += delete( 
					bucket != null ? 
						new S3Path( bucket , arg.toString() ) : 
						new S3Path( arg.toString() ), bRecurse  );

		}
		
		return ret;
		
		
	}


	private int delete(S3Path path, boolean bRecurse ) throws IOException, XMLStreamException, InvalidArgumentException, SaxonApiException {
		
		
		
		if( bRecurse ){

			
			ListObjectsRequest request = getListRequest( path ,null );
			ObjectListing list = mAmazon.listObjects(request);
			
			
			do {
				
				
				List<S3ObjectSummary>  objs = list.getObjectSummaries();
				for ( S3ObjectSummary obj : objs ){
					S3Path p = new S3Path( path.getBucket() , obj.getKey() );
					delete( p , false );
					
				}
				if( list.isTruncated()){
					// String marker = list.getNextMarker();
					list = mAmazon.listNextBatchOfObjects(list);
				}
				else
					break;
			} while( true );
			
			return 0;
			
			
		} else {
		
			
			DeleteObjectRequest request = new DeleteObjectRequest(path.getBucket(), path.getKey());
			
			mAmazon.deleteObject(request );
			
			return 0;
		
		}
		
		
	}




	

}
