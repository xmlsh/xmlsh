package org.xmlsh.aws;

import net.sf.saxon.s9api.SaxonApiException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import org.xmlsh.aws.util.AWSEC2Command;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.SafeXMLStreamWriter;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;
import org.xmlsh.util.Util;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.Reservation;


public class ec2DescribeInstances extends AWSEC2Command {

	private static Logger mLogger = LogManager.getLogger(ec2DescribeInstances.class);



	/**
	 * @param args
	 * @throws IOException 
	 */
	@Override
	public int run(List<XValue> args) throws Exception {

		
		Options opts = getOptions("f=filter:+");
		opts.parse(args);

		args = opts.getRemainingArgs();
		parseCommonOptions( opts );
		
		setSerializeOpts(this.getSerializeOpts(opts));
		
		
		
		try {
			 getEC2Client(opts);
		} catch (UnexpectedException e) {
			usage( e.getLocalizedMessage() );
			return 1;
		
		}
		
		Collection<Filter> filters = 
				opts.hasOpt("filter") ?
				parseFilters( Util.toStringList(opts.getOptValues("filter"))) : null ;
	

			
		int ret = describe(args,filters);

		
		return ret;
		
		
	}


	private int describe(List<XValue> args, Collection<Filter> filters) throws IOException, XMLStreamException, SaxonApiException, CoreException, InterruptedException {
		

		OutputPort stdout = this.getStdout();
		mWriter = new SafeXMLStreamWriter(stdout.asXMLStreamWriter(getSerializeOpts()));
		
		
		startDocument();
		startElement(this.getName());
		
		DescribeInstancesRequest  request = new DescribeInstancesRequest();
		if( args != null && args.size() > 0 ){
			
			request.setInstanceIds(Util.toStringList(args));
			
		}
	   if( filters != null )
		     request.setFilters(filters);
	
		
		traceCall("describeInstances");

		List<Reservation> result = null;
		
		int retry = rateRetry ;
		int delay = retryDelay ;
		do {
			try {
			   result = mAmazon.describeInstances(request).getReservations();
			   break ;
			} catch( AmazonServiceException e ){
				mShell.printErr("AmazonServiceException" , e );
				if( retry > 0 && Util.isEqual("RequestLimitExceeded",e.getErrorCode())){
					mShell.printErr("AWS RequestLimitExceeded - sleeping " + delay );
					Thread.sleep( delay );
					retry--;
					delay *= 2 ;
					
					
				}
				else
					throw e;
				
			}
		} while( retry > 0 );
			
		
		for( Reservation  res : result ){
			writeReservation(res);
			
		}
		
		endElement();
		endDocument();
		closeWriter();
		
		stdout.writeSequenceTerminator(getSerializeOpts());
		
		return 0;

	}


	

	public void usage() {
		super.usage("Usage: ec2-describe-instances [options] [instance-id]");
	}




	

}
