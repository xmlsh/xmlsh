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
import com.amazonaws.services.ec2.model.CreateVolumeRequest;
import com.amazonaws.services.ec2.model.CreateVolumeResult;


public class ec2CreateVolume extends AWSEC2Command {

	


	/**
	 * @param args
	 * @throws IOException 
	 */
	@Override
	public int run(List<XValue> args) throws Exception {

		
		Options opts = getOptions("t=type:,s=size:,zone=availability-zone:,iops:,snapshot=snapshot-id:");
		opts.parse(args);

		args = opts.getRemainingArgs();
		

		
		

		mSerializeOpts = this.getSerializeOpts(opts);
		try {
			getEC2Client(opts);
		} catch (UnexpectedException e) {
			usage( e.getLocalizedMessage() );
			return 1;
			
		}
		
	
		int ret = create( opts );
		return ret;
		
		
	}


	private int create(Options opts ) throws InvalidArgumentException, IOException, XMLStreamException, SaxonApiException  
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
		
		CreateVolumeResult result = mAmazon.createVolume(request);
		writeResult(result);

		return 0;
	}
	

	private	void writeResult(CreateVolumeResult result) throws IOException, InvalidArgumentException, XMLStreamException, SaxonApiException 
	{
		
		OutputPort stdout = this.getStdout();
		mWriter = new SafeXMLStreamWriter(stdout.asXMLStreamWriter(mSerializeOpts));
		
		
		startDocument();
		startElement(this.getName());


		writeVolume( result.getVolume() );
		
		
		
		endElement();
		endDocument();
		closeWriter();		

	}




}
