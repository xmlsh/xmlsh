package org.xmlsh.aws;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import net.sf.saxon.s9api.SaxonApiException;
import org.xmlsh.aws.util.AWSS3Command;
import org.xmlsh.aws.util.S3Path;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;
import org.xmlsh.util.Util;

import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;


public class s3get extends AWSS3Command {

	

	/**
	 * @param args
	 * @throws IOException 
	 */
	@Override
	public int run(List<XValue> args) throws Exception {

		
		Options opts = getOptions("meta:");
		opts.parse(args);

		
		OutputPort	metaPort = null ;
		if( opts.hasOpt("meta"))
			metaPort = mShell.getEnv().getOutputPort( opts.getOptStringRequired("meta") , false );
		
		args = opts.getRemainingArgs();
		
		mSerializeOpts = this.getSerializeOpts(opts);
		
		try {
			mAmazon = getS3Client(opts);
		} catch (UnexpectedException e) {
			usage( e.getLocalizedMessage() );
			return 1;
			
		}
		
		S3Path 		src;
		OutputPort	dest;
		
		switch( args.size() ){
		case	1 :
				src  = new S3Path(args.get(0).toString());
				dest = getStdout();
				break;
		case	2:
				src	 = new S3Path(args.get(0).toString());
				dest  = getOutput(args.get(1), false);
				break ;
				
		default	:
			
			usage() ; 
			return 1; 
				
		}
		
		
		
		int ret = 0;
		try {
			ret = get(  src , dest , metaPort  );
		} catch( Exception e ){
			mShell.printErr("Exception getting " + src + " to " + dest , e);
			ret = 1;
			
			
		}
			
		return ret;
		
	}

	private int get( S3Path src , OutputPort dest, OutputPort metaPort ) throws CoreException, IOException, XMLStreamException, SaxonApiException 
	{
		

			GetObjectRequest request = 
				new GetObjectRequest(src.getBucket(),src.getKey());
			
			
			if( dest.isFile() )
			{
				
				ObjectMetadata data = mAmazon.getObject(request  , dest.getFile() );
				
			} else
			{
				S3Object obj = mAmazon.getObject(request);
				if( metaPort != null ){
					mWriter = metaPort.asXMLStreamWriter(mSerializeOpts);
				
					writeMeta( obj.getObjectMetadata()  );
					mWriter.close();
					metaPort.release();
				}
				
				
				InputStream is = obj.getObjectContent();
				OutputStream os = dest.asOutputStream(mSerializeOpts);
				Util.copyStream(is, os);
				os.close();
				is.close();
			}
		
		return 0;
		
	}
	
	
	

	public void usage() {
		super.usage("Usage: s3get source [target]");
	}





	

}
