package org.xmlsh.aws;

import java.io.IOException;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import net.sf.saxon.s9api.SaxonApiException;

import org.xmlsh.aws.util.AWSEC2Command;
import org.xmlsh.aws.util.AWSUtil;
import org.xmlsh.aws.util.SafeXMLStreamWriter;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;
import org.xmlsh.util.Util;

import com.amazonaws.services.ec2.model.AssociateAddressRequest;
import com.amazonaws.services.ec2.model.AssociateAddressResult;


public class ec2AssociateAddress extends AWSEC2Command {

	


	/**
	 * @param args
	 * @throws IOException 
	 */
	@Override
	public int run(List<XValue> args) throws Exception {

		
		Options opts = getOptions();
		opts.parse(args);

        setSerializeOpts(this.getSerializeOpts(opts));
		args = opts.getRemainingArgs();
		

		
		
		
		if( args.size() != 2 ){
			usage(null);
			return 1;
		}

    
		try {
			getEC2Client(opts);
		} catch (UnexpectedException e) {
			usage( e.getLocalizedMessage() );
			return 1;
			
		}
		
		String 	instance = args.get(0).toString();
		String	ip = args.get(1).toString();
		
		
		int ret = associate( instance , ip );
		return ret;
		
		
	}


	private int associate( String instance , String ip ) throws IOException, XMLStreamException, SaxonApiException, CoreException  
	{
	
		String raw_ip = AWSUtil.resolveDNS( ip );
		if( Util.isBlank(raw_ip))
			throw new InvalidArgumentException("Unknown IP address or format: " + ip );
		
		AssociateAddressRequest request = new AssociateAddressRequest(instance,raw_ip);
		
		traceCall("associateAddress");

		AssociateAddressResult result = mAmazon.associateAddress(request);
		writeResult(result);

		return 0;
	}
	

	private	void writeResult(AssociateAddressResult result) throws IOException, XMLStreamException, SaxonApiException, CoreException 
	{
		
		startResult();
		startElement("association" );
		attribute("associate-id" , result.getAssociationId() );
		endElement();
		endResult();

	}




}
