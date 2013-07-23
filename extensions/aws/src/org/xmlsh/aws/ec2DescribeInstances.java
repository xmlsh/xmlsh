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
import org.xmlsh.util.Util;

import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.Reservation;


public class ec2DescribeInstances extends AWSEC2Command {

	


	private boolean bLongListing;




	/**
	 * @param args
	 * @throws IOException 
	 */
	@Override
	public int run(List<XValue> args) throws Exception {

		
		Options opts = getOptions("l=long");
		opts.parse(args);

		args = opts.getRemainingArgs();
		

		
		mSerializeOpts = this.getSerializeOpts(opts);
		
		
		
		
		bLongListing = opts.hasOpt("l");
		
		
		try {
			 getEC2Client(opts);
		} catch (UnexpectedException e) {
			usage( e.getLocalizedMessage() );
			return 1;
			
		}
		
	

		int ret = describe(args);

		
		
		return ret;
		
		
	}


	private int describe(List<XValue> args) throws IOException, XMLStreamException, SaxonApiException, CoreException {
		

		OutputPort stdout = this.getStdout();
		mWriter = new SafeXMLStreamWriter(stdout.asXMLStreamWriter(mSerializeOpts));
		
		
		startDocument();
		startElement(this.getName());
		
		DescribeInstancesRequest  request = new DescribeInstancesRequest();
		if( args != null && args.size() > 0 ){
			
			request.setInstanceIds(Util.toStringList(args));
			
		}
		
		
		
		traceCall("describeInstances");

		List<Reservation> result = mAmazon.describeInstances(request).getReservations();
		
		
		for( Reservation  res : result ){
			writeReservation(res);
			
		}
		
		
		
		
		
		endElement();
		endDocument();
		closeWriter();
		
		stdout.writeSequenceTerminator(mSerializeOpts);
		stdout.release();
		
		return 0;

	}


	

	public void usage() {
		super.usage("Usage: ec2-describe-instances [options] [instance-id]");
	}




	

}
