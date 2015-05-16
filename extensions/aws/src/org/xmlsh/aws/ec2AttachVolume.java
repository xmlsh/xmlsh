package org.xmlsh.aws;

import java.io.IOException;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import net.sf.saxon.s9api.SaxonApiException;

import org.xmlsh.aws.util.AWSEC2Command;
import org.xmlsh.aws.util.SafeXMLStreamWriter;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;
import org.xmlsh.util.Util;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.model.AttachVolumeRequest;
import com.amazonaws.services.ec2.model.AttachVolumeResult;


public class ec2AttachVolume extends AWSEC2Command {

	


	/**
	 * @param args
	 * @throws IOException 
	 */
	@Override
	public int run(List<XValue> args) throws Exception {

		
		Options opts = getOptions("i=instance:,d=device:");
		opts.parse(args);
        setSerializeOpts(this.getSerializeOpts(opts));
		parseCommonOptions(opts);
		args = opts.getRemainingArgs();
		
		
		if( args.size() != 1 ){
			usage(null);
			return 1;
		}
		try {
			getEC2Client(opts);
		} catch (UnexpectedException e) {
			usage( e.getLocalizedMessage() );
			return 1;
			
		}
		
		String 	volume = args.get(0).toString();
		String 	device = opts.getOptStringRequired("device");
		String instance = opts.getOptStringRequired("instance");

		
		int ret = attach( volume , instance , device );
		return ret;
		
		
	}


	private int attach( String volume , String instance, String device ) throws IOException, XMLStreamException, SaxonApiException, InterruptedException, CoreException  
	{
	
		
		AttachVolumeRequest request = new AttachVolumeRequest(volume,instance,device);
		
		traceCall("attachVolume");

		AttachVolumeResult result = null ;
		
		int retry = rateRetry ;
		int delay = retryDelay ;
		do {
			try {
				 result = mAmazon.attachVolume(request);
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
	

	private	void writeResult(AttachVolumeResult result) throws IOException, XMLStreamException, SaxonApiException, CoreException 
	{
		
		startResult();
		
		writeAttachment( result.getAttachment() );
		endResult();

	}




}
