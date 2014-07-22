package org.xmlsh.aws;

import net.sf.saxon.s9api.SaxonApiException;

import org.xmlsh.aws.util.AWSELBCommand;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;

import java.io.IOException;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import com.amazonaws.services.elasticloadbalancing.model.DeregisterInstancesFromLoadBalancerRequest;
import com.amazonaws.services.elasticloadbalancing.model.DeregisterInstancesFromLoadBalancerResult;
import com.amazonaws.services.elasticloadbalancing.model.Instance;


public class elbDeregister<x> extends AWSELBCommand {

	

	private boolean bLongListing;


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
		
		if( args.size()  < 2 ){ 
			usage();
			return -1;
		}
		
		String elb = args.remove(0).toString();
		
		try {
			 getELBClient(opts);
		} catch (UnexpectedException e) {
			usage( e.getLocalizedMessage() );
			return 1;
			
		}
		
	    int ret = deregister( elb , args );

			
		return ret;
		
		
	}


		
	private int deregister(String elb, List<XValue> args) throws XMLStreamException, IOException, SaxonApiException, CoreException {
		

		OutputPort stdout = this.getStdout();
		mWriter = stdout.asXMLStreamWriter(getSerializeOpts());
		
		startDocument();
		startElement(getName());
		
		
		DeregisterInstancesFromLoadBalancerRequest request = new DeregisterInstancesFromLoadBalancerRequest(elb , instances(args));
		traceCall("deregisterInstancesFromLoadBalancer");
		DeregisterInstancesFromLoadBalancerResult result = mAmazon.deregisterInstancesFromLoadBalancer(request);
	    for( Instance instance : result.getInstances()){
	    	
			startElement("instance");
			attribute( "instance-id" , instance.getInstanceId());
			endElement();

	    }
		

		
		
		
		
		endElement();
		endDocument();
		closeWriter();		
		
		
		return 0;
	
	
	}



	public void usage() {
		super.usage();
	}



	

}
