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
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StartInstancesResult;


public class ec2StartInstances extends AWSEC2Command {






	/**
	 * @param args
	 * @throws IOException 
	 */
	@Override
	public int run(List<XValue> args) throws Exception {


		Options opts = getOptions();
        parseOptions(opts, args);

		args = opts.getRemainingArgs();
        setSerializeOpts(this.getSerializeOpts(opts));




		if( args.size() < 1 ){
			usage(null);
			return 1;
		}


		try {
			getEC2Client(opts);
		} catch (UnexpectedException e) {
			usage( e.getLocalizedMessage() );
			return 1;

		}

		int ret = start( Util.toStringArray(args) );





		return ret;


	}




	private int start( String[] instances ) throws IOException, XMLStreamException, SaxonApiException, CoreException 
	{

		StartInstancesRequest  request = new StartInstancesRequest( Arrays.asList(instances));

		traceCall("startInstances");
		StartInstancesResult result = getAWSClient().startInstances(request);

		List<InstanceStateChange> changes = result.getStartingInstances();
		writeStateChages( changes);
		return 0;



	}






}
