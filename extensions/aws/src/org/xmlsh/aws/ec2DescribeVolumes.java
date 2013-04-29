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

import com.amazonaws.services.ec2.model.DescribeVolumesRequest;
import com.amazonaws.services.ec2.model.DescribeVolumesResult;
import com.amazonaws.services.ec2.model.Volume;


public class ec2DescribeVolumes extends AWSEC2Command {

	


	/**
	 * @param args
	 * @throws IOException 
	 */
	@Override
	public int run(List<XValue> args) throws Exception {

		
		Options opts = getOptions();
		opts.parse(args);

		args = opts.getRemainingArgs();
		

		
		mSerializeOpts = this.getSerializeOpts(opts);
		
			
		try {
			 getEC2Client(opts);
		} catch (UnexpectedException e) {
			usage( e.getLocalizedMessage() );
			return 1;
			
		}
		
		

		int ret;
		switch(args.size()){
		case	0:
			ret = describe(null);
			break;
		case	1:
			ret = describe(args);
			break;
			
		default :
				usage();
				return 1;
		}


		
		
		return ret;
		
		
	}


	private int describe(List<XValue> args) throws IOException, XMLStreamException, SaxonApiException, CoreException {
		

		OutputPort stdout = this.getStdout();
		mWriter = new SafeXMLStreamWriter(stdout.asXMLStreamWriter(mSerializeOpts));
		
		
		startDocument();
		startElement(this.getName());
		
		DescribeVolumesRequest  request = new DescribeVolumesRequest();
		if( args != null ){
			request.setVolumeIds(Util.toStringList(args));
			
		}
		
		
		
		DescribeVolumesResult result = mAmazon.describeVolumes(request);
		
		
		
		
		for( Volume  volume : result.getVolumes() ){
			writeVolume(volume);
			
			
			
			endElement();
			
		}
		
		
		
		
		
		endElement();
		endDocument();
		closeWriter();
		
		stdout.writeSequenceTerminator(mSerializeOpts);
		stdout.release();
		
		return 0;

	}
	

}
