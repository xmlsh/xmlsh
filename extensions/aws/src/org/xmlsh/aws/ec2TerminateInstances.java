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

import com.amazonaws.services.ec2.model.InstanceStateChange;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.ec2.model.TerminateInstancesResult;


public class ec2TerminateInstances extends AWSEC2Command {




	/**
	 * @param args
	 * @throws IOException 
	 */
	@Override
	public int run(List<XValue> args) throws Exception {


		Options opts = getOptions();
		opts.parse(args);

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

		int ret = stop( Util.toStringArray(args) );





		return ret;


	}




	private int stop( String[] instances ) throws IOException, XMLStreamException, SaxonApiException, CoreException 
	{

		TerminateInstancesRequest  request = new TerminateInstancesRequest( Arrays.asList(instances));

		traceCall("terminateInstances");
		TerminateInstancesResult result = getAWSClient().terminateInstances(request);

		List<InstanceStateChange> changes = result.getTerminatingInstances();
		writeStateChages( changes);
		return 0;



	}





}
