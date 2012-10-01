package org.xmlsh.aws;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.xmlsh.aws.util.AWSS3Command;
import org.xmlsh.aws.util.S3Path;
import org.xmlsh.aws.util.S3TransferManager;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.FileInputPort;
import org.xmlsh.core.InputPort;
import org.xmlsh.core.Options;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;
import org.xmlsh.util.StringPair;
import org.xmlsh.util.Util;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.transfer.MultipleFileUpload;


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
		
		    put( srcs , ds , meta , storage );
		    
			break ;
		}
			

				
		}
		
		
		
		

		
			
		return ret;
		
	}
	
	public static Collection<File> listFileTree(File dir) {
	    Set<File> fileTree = new HashSet<File>();
	    for (File entry : dir.listFiles()) {
	        if (entry.isFile()) fileTree.add(entry);
	        else fileTree.addAll(listFileTree(entry));
	    }
	    return fileTree;
	}

	private int put(File file, S3Path dest, List<XValue> meta, String storage)
	{
		if( file.isDirectory() ){
			Collection<File> files =  listFileTree(file);
			List<File> filelist = new ArrayList<File>(files);
			Util.sortFiles(filelist);
			S3TransferManager ctm = new S3TransferManager(mAmazon);
			MultipleFileUpload dirUpload = ctm.uploadFileList(dest.getBucket(), dest.getKey(), filelist, file);

			try {
				dirUpload.waitForCompletion();
			} catch (Exception e) {
				this.printErr("Exception putting directory to S3" , e);
				return 1;
			}
			
			return 0;
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
	
	private int put(List<XValue> xfiles, S3Path dest, List<XValue> meta, String storage) throws IOException {
    
			List<File> files = getFiles( xfiles);
			// If any file is a directory then revert to single puts
			for( File f : files ){
				if( f.isDirectory()){
					int ret = 0;
					for( File f2 : files )
						ret += put( f2 , dest , meta , storage );
					return ret ;
					
					
				}
			}
			
			
			
			S3TransferManager ctm = new S3TransferManager(mAmazon);
			File root = getShell().getCurdir();
			MultipleFileUpload dirUpload = ctm.uploadFileList(dest.getBucket(), dest.getKey(), files, root);

			try {
				dirUpload.waitForCompletion();
			} catch (Exception e) {
				this.printErr("Exception putting directory to S3" , e);
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
