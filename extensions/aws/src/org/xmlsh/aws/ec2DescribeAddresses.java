package org.xmlsh.aws;

import java.io.IOException;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import net.sf.saxon.s9api.SaxonApiException;

import org.xmlsh.aws.util.AWSEC2Command;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.SafeXMLStreamWriter;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;

import com.amazonaws.services.ec2.model.Address;
import com.amazonaws.services.ec2.model.DescribeAddressesRequest;
import com.amazonaws.services.ec2.model.DescribeAddressesResult;


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



		setSerializeOpts(this.getSerializeOpts(opts));


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
		mWriter = new SafeXMLStreamWriter(stdout.asXMLStreamWriter(getSerializeOpts()));


		startDocument();
		startElement(this.getName());

		DescribeAddressesRequest  request = new DescribeAddressesRequest();
		if( args != null ){
			request.setPublicIps( getIps(args)  );

		}



		traceCall("describeAddresses");

		DescribeAddressesResult result = getAWSClient().describeAddresses(request);



		for( Address  addr : result.getAddresses() ){
			startElement("address");


			attribute("public-ip", addr.getPublicIp() );
			attribute( "instance-id" , addr.getInstanceId() );

			endElement();

		}





		endElement();
		endDocument();
		closeWriter();

		stdout.writeSequenceTerminator(getSerializeOpts());

		return 0;

	}




}
