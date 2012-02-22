package org.xmlsh.aws;

import java.io.IOException;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import net.sf.saxon.s9api.SaxonApiException;
import org.xmlsh.aws.util.AWSELBCommand;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;

import com.amazonaws.services.elasticloadbalancing.model.DescribeLoadBalancersResult;
import com.amazonaws.services.elasticloadbalancing.model.HealthCheck;
import com.amazonaws.services.elasticloadbalancing.model.Instance;
import com.amazonaws.services.elasticloadbalancing.model.ListenerDescription;
import com.amazonaws.services.elasticloadbalancing.model.LoadBalancerDescription;


public class elbList<x> extends AWSELBCommand {

	

	private boolean bLongListing;


	/**
	 * @param args
	 * @throws IOException 
	 */
	@Override
	public int run(List<XValue> args) throws Exception {

		
		Options opts = getOptions("l=long");
		opts.parse(args);

		args = opts.getRemainingArgs();
		

		
		mSerializeOpts = this.getSerializeOpts(opts);
		
		
		
		
		bLongListing = opts.hasOpt("l");
		
		
		try {
			mAmazon = getELBClient(opts);
		} catch (UnexpectedException e) {
			usage( e.getLocalizedMessage() );
			return 1;
			
		}
		

		int ret = -1;
		switch(args.size()){
		case	0:
			ret = list();
			break;
		case	1:
			// ret = list( args.get(0).toString());
			break;
			
		default :
				usage();
				return 1;
		}


		
		
		return ret;
		
		
	}


	private int list() throws IOException, XMLStreamException, InvalidArgumentException, SaxonApiException 
	{

		OutputPort stdout = this.getStdout();
		mWriter = stdout.asXMLStreamWriter(mSerializeOpts);
		
		
		startDocument();
		startElement(getName());
		
		DescribeLoadBalancersResult result = mAmazon.describeLoadBalancers();
		
		for(LoadBalancerDescription desc :  result.getLoadBalancerDescriptions() ){
			
			startElement("balancer");
			attribute("name", desc.getLoadBalancerName() );
			attribute("dnsname" , desc.getDNSName());
			attribute("hosted-zone", desc.getCanonicalHostedZoneName());

			
			startElement("zones");
			for( String zone : desc.getAvailabilityZones() ){
				startElement("zone");
				attribute("name" , zone );
				endElement();
			}
			endElement(); // zones
			
			startElement("instances");
			for( Instance instance : desc.getInstances() ){
				startElement("instance");
				attribute( "instance-id" , instance.getInstanceId());
				endElement();

			}
			endElement(); // instances
			HealthCheck health = desc.getHealthCheck();
			startElement("health-check");
			attribute("target", health.getTarget());
			attribute("healthy-threshold", health.getHealthyThreshold().toString());
			attribute("interval", health.getInterval().toString());
			attribute("timeout", health.getTimeout().toString());
			attribute("unhealthy-threshold", health.getUnhealthyThreshold().toString() );			
			endElement() ; // health
			
			startElement("listeners");
			for( ListenerDescription listen : desc.getListenerDescriptions()) {
				startElement("listener");
				
				attribute("protocol",listen.getListener().getProtocol() );
				attribute("ssl-certificate-id",listen.getListener().getSSLCertificateId());
				attribute("instance-port" , listen.getListener().getInstancePort().toString() );
				attribute("load-balancer-port",listen.getListener().getLoadBalancerPort().toString());
				
				 
				for( String policy : listen.getPolicyNames() ){
					startElement("policy");
					attribute("name" , policy);
					endElement();
				}
				
				
				endElement();
			}
			endElement();
			
			
			endElement(); // balancer
		}
		endElement();
		endDocument();
		
		
				
		
		
		
		
		
		closeWriter();
		stdout.writeSequenceTerminator(mSerializeOpts);
		stdout.release();
		

		
		
		return 0;
		
		
		
		
	}
	public void usage() {
		super.usage();
	}



	

}
