package org.xmlsh.aws;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlsh.aws.util.AWSS3Command;
import org.xmlsh.aws.util.S3Path;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.FileInputPort;
import org.xmlsh.core.InputPort;
import org.xmlsh.core.Options;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;
import org.xmlsh.util.StringPair;
import org.xmlsh.util.Util;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;


public class s3put extends AWSS3Command {


	private boolean bRecurse = false ;
	private boolean bVerbose = false ;
	/**
	 * @param args
	 * @throws IOException 
	 */
	@Override
	public int run(List<XValue> args) throws Exception {

		
		Options opts = getOptions("m=meta:+,s=storage:,r=recurse,v=verbose");
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

		bRecurse = opts.hasOpt("recurse");
		bVerbose = opts.hasOpt("verbose");
		
		
		
		int ret = 0;
		
		
		switch( args.size() ){
 
		case 0 :
			
			usage() ; 
			return 1; 
		case	1 :
			
		{
			S3Path dest;
				dest = new S3Path(args.get(0).toString());
				InputPort src = this.getStdin();
			    ret = put(  src , dest , meta , storage  );
				src.release();
				
				break;
		}
		default : 
		{
			
		    List<XValue> srcs = args ;
		    S3Path ds  = new S3Path(srcs.remove(args.size()-1).toString());
		
		   
		    
		    for( XValue s : srcs ){
		    	S3Path d = new S3Path( ds , s.toString() );

			   ret = put(  s , d , meta , storage  );
				
		    	
		    	
		    }
		    
			break ;
		}
			

				
		}
		
		
		
		

		
			
		return ret;
		
	}
	private int put(File file, S3Path dest, List<XValue> meta, String storage)
	{
		if( file.isDirectory() ){
			int ret = 0;
			File [] files =  file.listFiles();
			Util.sortFiles(files);
			for( File f : files ){
			     ret += put( f , new S3Path(dest,f.getName()) , meta , storage );
			}
			return ret ;
		}
		
		FileInputPort src;
		try {
			src = new FileInputPort(file);
		} catch (IOException e) {
			mShell.printErr("Exception getting file: " + file.toString() , e );
			return 1;
		}
		return put( src , dest, meta , storage );
	}
	
	
	private int put(XValue s, S3Path dest, List<XValue> meta, String storage) {
		if( bRecurse && s.isAtomic()){
				File file;
				try {
					file = getFile( s );
				} catch (IOException e) {
					mShell.printErr("Exception getting file: " + s.toString() , e );
					return 1;
				}
				return put( file , dest , meta , storage );
				
		}
		InputPort input;
		try {
			input = getInput(s);
		} catch (CoreException e) {
			mShell.printErr("Exception getting input" , e );
			return 1;
		}
		return put( input , dest , meta , storage );
		
		
		
	}



	

	private int put(InputPort src, S3Path dest, List<XValue> meta , String storage ) 
	{
		
		
		if( bVerbose )
			mShell.printErr("Putting to " + dest.toString() );
		
		
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
		} catch( Exception e ){
			mShell.printErr("Exception putting to: " + dest.toString() , e);
			 return 1;
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
