package org.xmlsh.aws;

import java.io.IOException;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import net.sf.saxon.s9api.SaxonApiException;

import org.xmlsh.aws.util.AWSS3Command;
import org.xmlsh.aws.util.S3Path;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.Options;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;
import org.xmlsh.core.io.OutputPort;

import com.amazonaws.services.s3.model.GetObjectMetadataRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;


public class s3GetObjectMetadata extends AWSS3Command {



	/**
	 * @param args
	 * @throws IOException 
	 */
	@Override
	public int run(List<XValue> args) throws Exception {


		Options opts = getOptions();
		opts.parse(args);


		args = opts.getRemainingArgs();

		setSerializeOpts(this.getSerializeOpts(opts));

		try {
			getS3Client(opts);
		} catch (UnexpectedException e) {
			usage( e.getLocalizedMessage() );
			return 1;

		}

		S3Path 		src;

		switch( args.size() ){
		case	1 :
			src  = new S3Path(args.get(0).toString());
			break;

		default	:

			usage() ; 
			return 1; 

		}


		OutputPort metaPort = getStdout();

		int ret = 0;
		try {
			ret = getMetadata( src ,  metaPort  );
		} catch( Exception e ){
			mShell.printErr("Exception getting metadata from " + src );
			ret = 1;


		}

		return ret;

	}

	private int getMetadata( S3Path src , OutputPort metaPort ) throws CoreException, IOException, XMLStreamException, SaxonApiException 
	{


		GetObjectMetadataRequest request = 
				new GetObjectMetadataRequest(src.getBucket(),src.getKey());


		traceCall("getObjectMetadata");

		ObjectMetadata data = getAWSClient().getObjectMetadata(request  );
		mWriter = metaPort.asXMLStreamWriter(getSerializeOpts());
		writeMeta( data );
		mWriter.close();

		return 0;

	}



	@Override
	public void usage() {
		super.usage("Usage: s3get source [target]");
	}







}
