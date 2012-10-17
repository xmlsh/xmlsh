package org.xmlsh.aws;

import java.io.IOException;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import net.sf.saxon.s9api.SaxonApiException;
import org.xmlsh.aws.util.AWSEC2Command;
import org.xmlsh.aws.util.SafeXMLStreamWriter;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;

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

		args = opts.getRemainingArgs();
		

		
		
		
		if( args.size() != 1 ){
			usage(null);
			return 1;
		}
		

		mSerializeOpts = this.getSerializeOpts(opts);
		try {
			mAmazon = getEC2Client(opts);
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


	private int attach( String volume , String instance, String device ) throws InvalidArgumentException, IOException, XMLStreamException, SaxonApiException  
	{
	
		
		AttachVolumeRequest request = new AttachVolumeRequest(volume,instance,device);
		
		AttachVolumeResult result = mAmazon.attachVolume(request);
		writeResult(result);

		return 0;
	}
	

	private	void writeResult(AttachVolumeResult result) throws IOException, InvalidArgumentException, XMLStreamException, SaxonApiException 
	{
		
		OutputPort stdout = this.getStdout();
		mWriter = new SafeXMLStreamWriter(stdout.asXMLStreamWriter(mSerializeOpts));
		
		
		startDocument();
		startElement(this.getName());
		
		writeAttachment( result.getAttachment() );
		
		
		
		endElement();
		endDocument();
		closeWriter();		

	}




}
