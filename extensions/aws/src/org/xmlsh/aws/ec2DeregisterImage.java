package org.xmlsh.aws;

import java.io.IOException;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import net.sf.saxon.s9api.SaxonApiException;
import org.xmlsh.aws.util.AWSEC2Command;
import org.xmlsh.aws.util.SafeXMLStreamWriter;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;

import com.amazonaws.services.ec2.model.DeregisterImageRequest;


public class ec2DeregisterImage extends AWSEC2Command {

	




	/**
	 * @param args
	 * @throws IOException 
	 * 
	 
	 */
	@Override
	public int run(List<XValue> args) throws Exception {

		
		Options opts = getOptions();
		opts.parse(args);

		args = opts.getRemainingArgs();
		

		
		
		
		if( args.size() != 1 ){
			usage(null);
			return 1;
		}
		

		mSerializeOpts = this.getSerializeOpts(opts);
		try {
			 getEC2Client(opts);
		} catch (UnexpectedException e) {
			usage( e.getLocalizedMessage() );
			return 1;
			
		}
		
		traceCall("deregisterImage");

		int ret = deregisterImage( args.get(0).toString());
		
		
		
		return ret;	
	}

	private int deregisterImage( String image ) throws IOException, XMLStreamException, SaxonApiException, CoreException 
	{

		
		
		DeregisterImageRequest deregisterImageRequest= new DeregisterImageRequest(image);
		
		mAmazon.deregisterImage(deregisterImageRequest);
		


		writeResult(  );
		
		
		return 0;
	}

	private void writeResult() throws IOException, XMLStreamException, SaxonApiException, CoreException {
		OutputPort stdout = this.getStdout();
		mWriter = new SafeXMLStreamWriter(stdout.asXMLStreamWriter(mSerializeOpts));
		
		
		startDocument();
		startElement(this.getName());
		
		
		endElement();
		endDocument();
		closeWriter();
		
		stdout.writeSequenceTerminator(mSerializeOpts);
		stdout.release();
		
		
	}

	
	

}
