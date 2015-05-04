package org.xmlsh.aws;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

import org.xmlsh.aws.util.AWSS3Command;
import org.xmlsh.aws.util.S3Path;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.FileOutputPort;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;
import org.xmlsh.util.Util;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.transfer.Download;


public class s3get extends AWSS3Command {

	private String bucket = null ;

	private boolean bRecurse = false ;
    private boolean bVerbose = false ;
	
    private List<Download> mDownloads = new LinkedList<Download>();

	/**
	 * @param args
	 * @throws IOException 
	 */
	@Override
	public int run(List<XValue> args) throws Exception {

		
		Options opts = getOptions("meta:,r=recurse,b=bucket:,v=verbose");
		opts.parse(args);

		
		OutputPort	metaPort = null ;
		if( opts.hasOpt("meta"))
			metaPort = mShell.getEnv().getOutputPort( opts.getOptStringRequired("meta") , false );
		
		args = opts.getRemainingArgs();
		
		bucket = opts.getOptString("bucket", null);
		
		bRecurse = opts.hasOpt("recurse");
		bVerbose = opts.hasOpt("verbose");
		
		mSerializeOpts = this.getSerializeOpts(opts);
		
		try {
			 getS3Client(opts);
		} catch (UnexpectedException e) {
			usage( e.getLocalizedMessage() );
			return 1;
			
		}
		
		S3Path 		src;
		OutputPort	dest;

		int ret = 0;
		
		switch( args.size() ){
		case 0 :
			
			usage() ; 
			return 1; 
			
		case	1 :
		{
				src  = getPath(args.get(0));

				
				    dest = getStdout();
				
					ret += get(  src , dest , metaPort   );
			
				break ;
		}

		default	:
			
		    List<XValue> srcs = args ;
		    XValue xds  = srcs.remove(args.size()-1);
		
		    for( XValue s : srcs ){
		    	src = getPath( s );
		    	String prefix = null ;
		    	if( bRecurse )
		    		prefix = src.getKey() ;
					
		    	ret += get(  src , xds  , metaPort  , prefix );
		    }
		    
		}
			
		if( metaPort != null )
		       metaPort.release();
		
		waitForDownloads();
		shutdownTransferManager();

		return ret;
		
	}

	private void waitForDownloads() throws AmazonServiceException, AmazonClientException, InterruptedException {
		while( !mDownloads.isEmpty()){
			Download d = mDownloads.remove(0);
			d.waitForCompletion();
		}
		
	}

	private S3Path getPath(XValue key) {
		return getPath( bucket , key.toString() );
	}

	private int get( S3Path src , XValue dest, OutputPort metaPort , String prefix ) throws CoreException, IOException, AmazonServiceException, AmazonClientException, InterruptedException 
	{
		
		
		if( src.isDirectory()){
			int ret = 0;
			if( ! bRecurse ){
				mShell.printErr("Skipping directory: " + src.getKey());
				return 0;
			}

			ListObjectsRequest request = getListRequest( src ,null  );
			traceCall("listObjects");

			ObjectListing list = mAmazon.listObjects(request);
			
			do {
				
				List<S3ObjectSummary>  objs = list.getObjectSummaries();
				for ( S3ObjectSummary obj : objs ){
					S3Path s = getPath( obj.getBucketName() , obj.getKey());
					if( s.isDirectory() )
						continue ;
					ret += get( s , dest , metaPort , prefix );
				}
				waitForDownloads();
				if( list.isTruncated()){
					// String marker = list.getNextMarker();
					list = mAmazon.listNextBatchOfObjects(list);
				}
				else
					break;
			} while( true );
			
			return ret ;
			
			
		}  else 
     		return get( src , getOutput(src,dest, prefix )  , metaPort );
		
		
	}
	
	private OutputPort getOutput(S3Path src , XValue dest , String prefix ) throws CoreException, IOException {
		
		String fname = dest.toString();
		String key = src.getKey();
		
		if( ! Util.isBlank(prefix)){
			if( !key.startsWith(prefix)){
				mShell.printErr("Skipping key - does not start with prefix: " + key );
		        return null ;
			}
			key = key.substring( prefix.length() );
			while( key.startsWith("/"))
				key = key.substring(1);
			
		}
		
		File out = getFile( fname );
	    if( out.isDirectory() || fname.endsWith("/"))
	        out = new File( out , key );
	    
	   File parent = out.getParentFile();
	   if( parent != null && ! parent.exists() )
	        parent.mkdirs();
	    
	   return new FileOutputPort( out , false , false );
		
		
			
		
		

	}

	private int get( S3Path src , OutputPort dest, OutputPort metaPort  ) 
	{
		
		if( bVerbose )
			mShell.printErr("Getting " + src.toString() );
		
		try {

			GetObjectRequest request = 
				new GetObjectRequest(src.getBucket(),src.getKey());
			
			ObjectMetadata meta = null ;
			if( dest.isFile() )
			{
				
				traceCall("getObject");
				Download download = getTransferManager().download(request,  dest.getFile());
				mDownloads.add(download);
				
			} else
			{
				traceCall("getObject");

				S3Object obj = mAmazon.getObject(request);
			    meta = obj.getObjectMetadata() ;
				
				InputStream is = obj.getObjectContent();
				OutputStream os = dest.asOutputStream(mSerializeOpts);
				Util.copyStream(is, os);
				os.close();
				is.close();
			}
			
			if( metaPort != null ){
				mWriter = metaPort.asXMLStreamWriter(mSerializeOpts);
			
				writeMeta( meta );
				mWriter.close();
			}
	
		} catch( Exception e )
		{
			
			mShell.printErr("Exception getting: " + src  , e);
			return 1;
		}
		
		return 0;
		
	}
	
	
	

	public void usage() {
		super.usage("Usage: s3get source [target]");
	}





	

}
