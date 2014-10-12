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

import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.DeleteObjectsResult;
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

		setSerializeOpts(this.getSerializeOpts(opts));



		try {
			getS3Client(opts);
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

	private int deleteBatch( String bucket , String[]  keys ){
		DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(bucket)
		.withKeys(keys)
		.withQuiet(true);

		@SuppressWarnings("unused")
		DeleteObjectsResult result = getAWSClient().deleteObjects(deleteObjectsRequest);
		return 0;




	}

	private int delete(S3Path path, boolean bRecurse ) throws IOException, XMLStreamException, InvalidArgumentException, SaxonApiException {



		if( bRecurse ){


			ListObjectsRequest request = getListRequest( path ,null );
			traceCall("listObjects");

			ObjectListing list = getAWSClient().listObjects(request);


			do {


				List<S3ObjectSummary>  objs = list.getObjectSummaries();
				String[] keys  =  getKeys( objs );
				// Delete in batches, magically the max size of getObjectSummaries matches the max size for batch deletes (1000)
				deleteBatch( path.getBucket() , keys );



				if( list.isTruncated()){
					// String marker = list.getNextMarker();
					list = getAWSClient().listNextBatchOfObjects(list);
				}
				else
					break;
			} while( true );

			return 0;


		} else {


			DeleteObjectRequest request = new DeleteObjectRequest(path.getBucket(), path.getKey());
			traceCall("deleteObject");


			getAWSClient().deleteObject(request );

			return 0;

		}


	}

	private String[] getKeys(List<S3ObjectSummary> objs) {

		String[] keys = new String[ objs.size()];
		int i = 0;
		for( S3ObjectSummary obj : objs )
			keys[i++] = obj.getKey();
		return keys ;


	}






}
