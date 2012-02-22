package org.xmlsh.aws;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import net.sf.saxon.s9api.SaxonApiException;
import org.xmlsh.aws.util.AWSEC2Command;
import org.xmlsh.aws.util.AWSUtil;
import org.xmlsh.aws.util.SafeXMLStreamWriter;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.util.Util;

import com.amazonaws.services.ec2.model.Address;
import com.amazonaws.services.ec2.model.DescribeAddressesRequest;
import com.amazonaws.services.ec2.model.DescribeAddressesResult;
import com.amazonaws.services.ec2.model.EbsInstanceBlockDevice;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceBlockDeviceMapping;
import com.amazonaws.services.ec2.model.InstanceLicense;
import com.amazonaws.services.ec2.model.Placement;
import com.amazonaws.services.ec2.model.ProductCode;
import com.amazonaws.services.ec2.model.Tag;


public class ec2DescribeAddresses extends AWSEC2Command {

	


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
			mAmazon = getEC2Client(opts);
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


	private int describe(List<XValue> args) throws IOException, XMLStreamException, InvalidArgumentException, SaxonApiException {
		

		OutputPort stdout = this.getStdout();
		mWriter = new SafeXMLStreamWriter(stdout.asXMLStreamWriter(mSerializeOpts));
		
		
		startDocument();
		startElement(this.getName());
		
		DescribeAddressesRequest  request = new DescribeAddressesRequest();
		if( args != null ){
			request.setPublicIps( getIps(args)  );
			
		}
		
		
		
		DescribeAddressesResult result = mAmazon.describeAddresses(request);
		
		
		
		for( Address  addr : result.getAddresses() ){
			startElement("address");
			
			
			attribute("public_ip", addr.getPublicIp() );
			attribute( "instance_id" , addr.getInstanceId() );
			
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
