package org.xmlsh.aws;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlsh.aws.util.AWSS3Command;
import org.xmlsh.aws.util.S3Path;
import org.xmlsh.aws.util.S3TransferManager;
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
import com.amazonaws.services.s3.transfer.Upload;
import com.amazonaws.services.s3.transfer.model.UploadResult;


public class s3put extends AWSS3Command {


	private boolean bRecurse = false ;
	private boolean bVerbose = false ;
	private S3TransferManager tm = null;
	
	
	private S3TransferManager getTransferManager()
	{
		if( tm == null )
			tm =  new S3TransferManager(mAmazon);
		return tm ;

	}
	
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

		if( tm != null)
		    tm.shutdownNow();

		return ret;

	}

	private int put(List<XValue> files, S3Path dest, List<XValue> meta, String storage) throws IOException
	{
		List<XValue> filelist = getFilelist(files);
		
		ObjectMetadata metadata = new ObjectMetadata();

		if( meta != null ){
			for( XValue xm : meta ){
				StringPair 	pair = new StringPair( xm.toString(), '=' );
				metadata.addUserMetadata(pair.getLeft(), pair.getRight());
			}
		}
		
		traceCall("TransferManager.uploadFileList");

		
		MultipleFileUpload dirUpload = getTransferManager().
				uploadFileList(dest.getBucket(), dest.getKey(), getFiles( filelist), mShell.getCurdir(), metadata.getUserMetadata() , storage );

		try {
			dirUpload.waitForCompletion();
		} catch (Exception e) {
			this.printErr("Exception putting files to S3" , e);
			return 1;
		}

		return 0;
	}

	

	private List<File> getFiles(List<XValue> xfiles) throws IOException {
		ArrayList<File> files = new ArrayList<File>();
		for( XValue xf : xfiles )
			files.add( mShell.getFile(xf) );

		return files ;
	}

	private List<XValue> getFilelist(List<XValue> xfiles) throws IOException {
		ArrayList<XValue> files = new ArrayList<XValue>();
		for( XValue xf : xfiles ) {
			File f = mShell.getFile(xf);
			if (bRecurse && f.isDirectory()) {
				List<XValue> dirfiles = new ArrayList<XValue>();
				String[] flist = f.list();
				for (String ff : flist) dirfiles.add(new XValue(mShell.getFile(xf.toString()+File.separator+ff).getAbsolutePath()));
				files.addAll(getFilelist(dirfiles));
			}
			else files.add(new XValue(mShell.getFile(xf.toString()).getAbsolutePath()));
		}
		return files ;
	}

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



	private void printResult(PutObjectResult result) {
		mShell.printOut(result.getETag() + " + " + result.getVersionId());
	}

	private void printResult(UploadResult result) {
		mShell.printOut(result.getBucketName() + " + " + result.getKey());
	}

	public void usage() {
		super.usage("Usage: s3put [source] dest");
	}







}
