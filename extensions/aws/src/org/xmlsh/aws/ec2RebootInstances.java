package org.xmlsh.aws;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import net.sf.saxon.s9api.SaxonApiException;

import org.xmlsh.aws.util.AWSEC2Command;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.Options;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;
import org.xmlsh.util.Util;

import com.amazonaws.services.ec2.model.RebootInstancesRequest;


public class ec2RebootInstances extends AWSEC2Command {

	


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
		

		
		
		
		if( args.size() < 1 ){
			usage(null);
			return 1;
		}
		

        setSerializeOpts(this.getSerializeOpts(opts));
		try {
			 getEC2Client(opts);
		} catch (UnexpectedException e) {
			usage( e.getLocalizedMessage() );
			return 1;
			
		}
		
		int ret = reboot( Util.toStringArray(args) );
		
		
		
		
		
		return ret;
		
		
	}




	private int reboot( String[] instances ) throws IOException, XMLStreamException, SaxonApiException, CoreException 
	{
	
		RebootInstancesRequest  request = new RebootInstancesRequest( Arrays.asList(instances));
		
		
		traceCall("rebootInstances");
		mAmazon.rebootInstances(request);
		
		return 0;
		
	
	
	}


	
	

}
