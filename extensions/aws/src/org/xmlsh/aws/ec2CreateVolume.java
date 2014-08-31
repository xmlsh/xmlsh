package org.xmlsh.aws;

import net.sf.saxon.s9api.SaxonApiException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.xmlsh.aws.util.AWSEC2Command;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.SafeXMLStreamWriter;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;
import org.xmlsh.util.Util;

import java.io.IOException;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.model.CreateVolumeRequest;
import com.amazonaws.services.ec2.model.CreateVolumeResult;


public class ec2CreateVolume extends AWSEC2Command {


	private static Logger mLogger = LogManager.getLogger();


	/**
	 * @param args
	 * @throws IOException 
	 */
	@Override
	public int run(List<XValue> args) throws Exception {


		Options opts = getOptions("t=type:,s=size:,zone=availability-zone:,iops:,snapshot=snapshot-id:");
		opts.parse(args);

		args = opts.getRemainingArgs();

		rateRetry = opts.getOptInt("rate-retry", 0);




		setSerializeOpts(this.getSerializeOpts(opts));
		try {
			getEC2Client(opts);
		} catch (UnexpectedException e) {
			usage( e.getLocalizedMessage() );
			return 1;

		}


		int ret = create( opts );
		return ret;


	}


	private int create(Options opts ) throws IOException, XMLStreamException, SaxonApiException, InterruptedException, CoreException  
	{


		CreateVolumeRequest request = new CreateVolumeRequest()
		.withVolumeType( opts.getOptString("type", "standard"))
		.withAvailabilityZone(opts.getOptStringRequired("zone"));


		if( opts.hasOpt("size"))
			request.setSize( opts.getOptInt("size", 10));
		if( opts.hasOpt("snapshot"))
			request.setSnapshotId(opts.getOptStringRequired("snapshot"));
		if( opts.hasOpt("iops"))
			request.setIops(opts.getOptInt("iops", 100));

		traceCall("createVolume");

		CreateVolumeResult result = null ;

		int retry = rateRetry ;
		int delay = retryDelay ;
		do {
			try {
				result=	mAmazon.createVolume(request);
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


		writeResult(result);

		return 0;
	}


	private	void writeResult(CreateVolumeResult result) throws IOException, XMLStreamException, SaxonApiException, CoreException 
	{

		OutputPort stdout = this.getStdout();
		mWriter = new SafeXMLStreamWriter(stdout.asXMLStreamWriter(getSerializeOpts()));


		startDocument();
		startElement(this.getName());


		writeVolume( result.getVolume() );



		endElement();
		endDocument();
		closeWriter();		

	}




}
