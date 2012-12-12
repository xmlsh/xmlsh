package org.xmlsh.aws;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.xmlsh.aws.util.AWSS3Command;
import org.xmlsh.aws.util.S3Path;
import org.xmlsh.aws.util.S3TransferManager;
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
import com.amazonaws.services.s3.transfer.MultipleFileUpload;
import com.amazonaws.services.s3.transfer.Upload;
import com.amazonaws.services.s3.transfer.model.UploadResult;


public class s3put extends AWSS3Command {


	private boolean bRecurse = false ;
	private boolean bVerbose = false ;
	private boolean bTest = false ;
	private S3TransferManager ctm = null;
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	@Override
	public int run(List<XValue> args) throws Exception {


		Options opts = getOptions("m=meta:+,s=storage:,r=recurse,v=verbose,t=test");
		opts.parse(args);

		args = opts.getRemainingArgs();

		mSerializeOpts = this.getSerializeOpts(opts);

		try {
			mAmazon = getS3Client(opts);
		} catch (UnexpectedException e) {
			usage( e.getLocalizedMessage() );
			return 1;

		}

		ctm = new S3TransferManager(mAmazon);

		List<XValue> meta = opts.getOptValues("meta");

		String storage = opts.getOptString("storage", null);

		bRecurse = opts.hasOpt("recurse");
		bVerbose = opts.hasOpt("verbose");
		bTest = opts.hasOpt("test");

		int ret = 0;


		switch( args.size() ){

		case 0 :

			usage() ; 
			return 1; 
			
		case 1 :

			S3Path dest;
			dest = new S3Path(args.get(0).toString());
			InputPort src = this.getStdin();
			ret = put(  src , dest , meta , storage  );
			src.release();

			break;

		default : 

			List<XValue> srcs = args ;
			S3Path ds  = new S3Path(srcs.remove(args.size()-1).toString());

			ret = put( srcs , ds , meta , storage );

			break ;

		}

		ctm.shutdownNow();

		return ret;

	}

	private int put(List<XValue> files, S3Path dest, List<XValue> meta) throws IOException
	{
		List<XValue> filelist = getFilelist(files);
		if (bTest) {
			Iterator<XValue> it = filelist.iterator();
			while( it.hasNext() ) mShell.printOut(it.next().toString());
			return 0;
		}
		
		ObjectMetadata metadata = new ObjectMetadata();

		if( meta != null ){
			for( XValue xm : meta ){
				StringPair 	pair = new StringPair( xm.toString(), '=' );
				metadata.addUserMetadata(pair.getLeft(), pair.getRight());
			}
		}
		
		MultipleFileUpload dirUpload = ctm.uploadFileList(dest.getBucket(), dest.getKey(), filelist, mShell.getCurdir(), metadata);

		try {
			dirUpload.waitForCompletion();
		} catch (Exception e) {
			this.printErr("Exception putting directory to S3" , e);
			return 1;
		}

		return 0;
	}

	private int put(List<XValue> xfiles, S3Path dest, List<XValue> meta, String storage) throws IOException {

		List<File> files = getFiles(xfiles);

		if (bRecurse) return put(xfiles, dest, meta);

		int ret = 0;
		for( File f : files ){
			InputPort src = new FileInputPort(f);
			if( dest.isDirectory() )
				ret += 	put(src, new S3Path(dest, f.getName() ) ,  meta ,  storage );
			else
				ret += 	put(src,  dest,  meta , storage );
		}
		return ret ;
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
				for (String ff : flist) dirfiles.add(new XValue(mShell.getFile(xf.toString()+File.separator+ff)));
				files.addAll(getFilelist(dirfiles));
			}
			else files.add(new XValue(mShell.getFile(xf.toString())));
		}
		return files ;
	}

	private int put(InputPort src, S3Path dest, List<XValue> meta , String storage ) throws IOException 
	{

		InputStream is = null;

		if( bVerbose )
			mShell.printErr("Putting to " + dest.toString() );


		ObjectMetadata metadata = new ObjectMetadata();

		if( meta != null ){
			for( XValue xm : meta ){
				StringPair 	pair = new StringPair( xm.toString(), '=' );
				metadata.addUserMetadata(pair.getLeft(), pair.getRight());
			}
		}

		/* smcs
		 * if storage is not null, then use a PutObjectRequest to upload the data
		 * since this is the only model that supports specifying a storage class
		 */
		if (storage != null) {
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

				PutObjectResult result = mAmazon.putObject(request);
				if(bVerbose) printResult(result);
			} catch( Exception e ){
				mShell.printErr("Exception putting to: " + dest.toString() , e);
				return 1;
			}

		} else try {
			if( src.isFile() )
				is = new FileInputStream(src.getFile());
			else
				is = src.asInputStream( mSerializeOpts );

			Upload upload = ctm.upload( dest.getBucket() , dest.getKey() , is , metadata);

			UploadResult result = upload.waitForUploadResult();

			if(bVerbose) printResult(result);			
		} catch( Exception e ){
			if (is != null) is.close();
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
