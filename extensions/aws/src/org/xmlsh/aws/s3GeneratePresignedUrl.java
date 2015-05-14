package org.xmlsh.aws;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Date;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.trans.XPathException;

import org.xmlsh.aws.util.AWSS3Command;
import org.xmlsh.aws.util.S3Path;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;
import org.xmlsh.util.Util;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;


public class s3GeneratePresignedUrl extends AWSS3Command {

	private 	OutputStream	mOutput ;


	/**
	 * @param args
	 * @throws IOException 
	 */
	@Override
	public int run(List<XValue> args) throws Exception {

		
		Options opts = getOptions("expiration:,method:");
		opts.parse(args);

		args = opts.getRemainingArgs();
        setSerializeOpts(this.getSerializeOpts(opts));

		try {
			 getS3Client(opts);
		} catch (UnexpectedException e) {
			usage( e.getLocalizedMessage() );
			return 1;
			
		}


		int ret = 0 ;
		if(args.size() != 1 ){
				usage();
				return 1;
		}
		OutputPort outp = this.getStdout();
		mOutput = outp.asOutputStream(getSerializeOpts());
		
		

		S3Path path = new S3Path( args.get(0).toString() );
		ret = generate( path , opts );
		
		
		return ret;
		
		
	}


	private int generate(S3Path path, Options opts) throws IOException, XMLStreamException, InvalidArgumentException, SaxonApiException, XPathException {
		
		

		GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(path.getBucket(), path.getKey());
		
		if( opts.hasOpt("expiration"))
		
			request.setExpiration(parseDate(opts.getOptValue("expiration")));
		
		if( opts.hasOpt("method"))
			request.setMethod(parseMethod(opts.getOptStringRequired("method")));
		
		// request.setResponseHeaders(responseHeaders);
		
		
		traceCall("generatePresignedUrl");

		URL url = mAmazon.generatePresignedUrl(request);
			
		mOutput.write( url.toString().getBytes(getSerializeOpts().getOutputTextEncoding() ));
		mOutput.write( Util.getNewline(getSerializeOpts()));

			
		return 0;
		
		
	}


	private HttpMethod parseMethod(String method) {
		
		return HttpMethod.valueOf(method);
		
		
		
	}


	private Date parseDate(XValue value) throws XPathException, InvalidArgumentException {
		
		return (Date) value.convert( Date.class );
		
		
	}




	

}
