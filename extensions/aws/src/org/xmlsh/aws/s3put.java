package org.xmlsh.aws;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlsh.aws.util.AWSS3Command;
import org.xmlsh.aws.util.S3Path;
import org.xmlsh.core.InputPort;
import org.xmlsh.core.Options;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.UnimplementedException;
import org.xmlsh.core.XValue;
import org.xmlsh.util.StringPair;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.transfer.MultipleFileUpload;
import com.amazonaws.services.s3.transfer.ObjectMetadataProvider;
import com.amazonaws.services.s3.transfer.Upload;
import com.amazonaws.services.s3.transfer.model.UploadResult;


public class s3put extends AWSS3Command {


	private boolean bRecurse = false ;
	private boolean bVerbose = false ;
	private int mBatchSize = 1000 ;
	
	
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
			 getS3Client(opts);
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
			
		case 1 :
		{
			S3Path dest;
			dest = new S3Path(args.get(0).toString());
			InputPort src = this.getStdin();
			ret = put(  src , dest , meta , storage  );
			src.release();

			break;
		}	
		case 2:  // s3put src dest if 
		{
			S3Path dest;
			dest = new S3Path(args.get(1).toString());
			if( ! bRecurse ){
				InputPort src = mShell.getEnv().getInput(args.get(0));
				if( ! src.isFile() ||  ! src.getFile().isDirectory() ){
					 
			     ret = put(  src , dest , meta , storage  );
     			 src.release();
     		     break ; 
     		     }
				// Fall Through 
				src.release();
			}
			// fall through
		}

		default : 

			List<XValue> srcs = args ;
			S3Path ds  = new S3Path(srcs.remove(args.size()-1).toString());

			ret = put( srcs , ds , meta , storage );

			break ;

		}

		shutdownTransferManager();

		return ret;

	}


	private int put( List<File> files , S3Path dest ,ObjectMetadataProvider metadata , String storeage ){
		
		File pwd = getDirOrDrive();
		String destKey = dest.getKey();
		
		/* DAL: TODO No easy work around 
		// Workaround a TM bug where putting files from root delete the first char of the file
		if( isRoot( pwd )){
			//ArrayList<File> tmp = new ArrayList<File>( files.size());
			//for( File f : files )
			//	tmp.add( new File( ))
			mShell.printErr( destKey );
			
		}
		*/
		
		MultipleFileUpload dirUpload = 
				getTransferManager().uploadFileList(dest.getBucket(), destKey , pwd , files, metadata );
				

		try {
			dirUpload.waitForCompletion();
		} catch (Exception e) {
			this.printErr("Exception putting files to S3" , e);
			return 1;
		}

		return 0;
		
	}

	
	
	
	
	private boolean isRoot(File file) 
	{
		
		for( File root : File.listRoots() )
			if( file.equals(root) )
					return true ;
		return false ;
	}


	private int put(List<XValue> files, S3Path dest, final List<XValue> meta, String storage) throws IOException
	{
		
		traceCall("TransferManager.uploadFileList");
		
		ObjectMetadataProvider provider = new ObjectMetadataProvider(){

			@Override
			public void provideObjectMetadata(File file, ObjectMetadata metadata) {
				if( meta != null ){
					for( XValue xm : meta ){
						StringPair 	pair = new StringPair( xm.toString(), '=' );
						metadata.addUserMetadata(pair.getLeft(), pair.getRight());
					}
				}
				
			}} ;
			
			
		ArrayList<File> afiles = new ArrayList<File>( mBatchSize );
		
		for( File f :  getFiles(files) )
		  batchPut( afiles , f , dest , provider , storage );
			
		if( afiles.size() > 0 )
		  flush( afiles ,  dest , provider , storage );


		return 0;
	}

	private File getDirOrDrive() {
		return mShell.getCurdir();
	}

	

	/* 
	 * Convert a list of XValue to a list of File without recursing
	 */
	private List<File> getFiles(List<XValue> xfiles) throws IOException {
		ArrayList<File> files = new ArrayList<File>( xfiles.size() );
		for( XValue xf : xfiles )
			files.add( mShell.getFile(xf) );

		return files ;
	}

	
	
	
	private void batchPut( ArrayList< File > files , File file , S3Path dest ,ObjectMetadataProvider metadata , String storage ){
		
		
		if( file.isDirectory() ){
			String[] flist = file.list();
			
			for (String ff : flist) 
				batchPut( files , new File( file , ff ) , dest , metadata , storage );
			
		} else
			files.add( file );

		if( files.size() >= mBatchSize )
			flush( files , dest , metadata , storage );
	
	
	}
	
	private void flush( ArrayList< File > files , S3Path dest ,ObjectMetadataProvider metadata , String storage ){
		put( files , dest , metadata , storage );
		files.clear();
		
	}
	
	
	
	/* 
	 * Put a single file from a stream to a specific S3 bucket+key
	 */

	private int put(InputPort src, S3Path dest, List<XValue> meta , String storage ) throws IOException, UnexpectedException, UnimplementedException 
	{

		InputStream is = null;

		if( bVerbose )
			mShell.printErr("Putting to " + dest.toString() );
		
		if( ! dest.hasKey() ){
			if( ! src.isFile() )
				throw new UnexpectedException("Cannot put non named object to S3 without a key");
			
			dest.setKey( src.getFile().getName());
			
		}


		ObjectMetadata metadata = new ObjectMetadata();

		if( meta != null ){
			for( XValue xm : meta ){
				StringPair 	pair = new StringPair( xm.toString(), '=' );
				metadata.addUserMetadata(pair.getLeft(), pair.getRight());
			}
		}

		try {
			PutObjectRequest request;


			if( src.isFile() ){
				request = new PutObjectRequest( dest.getBucket() , dest.getKey() , src.getFile() );
			}
			else {
				is = src.asInputStream( mSerializeOpts );
				request = new PutObjectRequest( dest.getBucket() , dest.getKey() , is , metadata);
			}

			request.setStorageClass(storage);

			// update metadata
			//
			request.setMetadata(metadata);
			
			traceCall("TransferManager.upload");


			
			
			Upload upload = getTransferManager().upload( request );
           
			UploadResult result = upload.waitForUploadResult();
			

			if(bVerbose) printResult(result);
		} catch( Exception e ){
			mShell.printErr("Exception putting to: " + dest.toString() , e);
			return 1;
		}

		
		if (is != null) is.close();

		return 0;

	}



	private void printResult(UploadResult result) {
		mShell.printOut(result.getBucketName() + " + " + result.getKey());
	}

	public void usage() {
		super.usage("Usage: s3put [source] dest");
	}







}
