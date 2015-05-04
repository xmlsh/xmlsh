package org.xmlsh.aws;

import java.io.IOException;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import net.sf.saxon.s9api.SaxonApiException;

import org.xmlsh.aws.util.AWSELBCommand;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;
import org.xmlsh.util.Util;

import com.amazonaws.services.elasticloadbalancing.model.DescribeInstanceHealthRequest;
import com.amazonaws.services.elasticloadbalancing.model.DescribeInstanceHealthResult;
import com.amazonaws.services.elasticloadbalancing.model.DescribeLoadBalancersRequest;
import com.amazonaws.services.elasticloadbalancing.model.DescribeLoadBalancersResult;
import com.amazonaws.services.elasticloadbalancing.model.HealthCheck;
import com.amazonaws.services.elasticloadbalancing.model.Instance;
import com.amazonaws.services.elasticloadbalancing.model.InstanceState;
import com.amazonaws.services.elasticloadbalancing.model.ListenerDescription;
import com.amazonaws.services.elasticloadbalancing.model.LoadBalancerDescription;


public class elbList	 extends  AWSELBCommand {

	

	private List<InstanceState> states;
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
			 getELBClient(opts);
		} catch (UnexpectedException e) {
			usage( e.getLocalizedMessage() );
			return 1;
			
		}
		

		int ret = -1;
		ret = list(Util.toStringList(args));

		
		
		return ret;
		
		
	}


	private int list(List<String> elbs) throws IOException, XMLStreamException, SaxonApiException, CoreException 
	{

		OutputPort stdout = this.getStdout();
		mWriter = stdout.asXMLStreamWriter(mSerializeOpts);
		
		DescribeLoadBalancersRequest request = 
				elbs.size() == 0 ?
				new DescribeLoadBalancersRequest(): 
					new DescribeLoadBalancersRequest(elbs)
	          ;
		
		
		startDocument();
		startElement(getName());
         
		
		traceCall("describeLoadBalancers");
		DescribeLoadBalancersResult result = mAmazon.describeLoadBalancers(request);
		
		for(LoadBalancerDescription desc :  result.getLoadBalancerDescriptions() ){
			
			startElement("balancer");
			attribute("name", desc.getLoadBalancerName() );
			attribute("dnsname" , desc.getDNSName());
			attribute("hosted-zone", desc.getCanonicalHostedZoneName());

			List<String> zones = desc.getAvailabilityZones();
			
			writeZones(zones);
			DescribeInstanceHealthRequest healthRequest = 
					new DescribeInstanceHealthRequest(desc.getLoadBalancerName());
			DescribeInstanceHealthResult healthResult = mAmazon.describeInstanceHealth(healthRequest);
			List<InstanceState> instanceStates = healthResult.getInstanceStates();
			
			
			startElement("instances");
			for( Instance instance : desc.getInstances() ){
				startElement("instance");
				attribute( "instance-id" , instance.getInstanceId());
				for( InstanceState s : instanceStates ){
					if( s.getInstanceId().equals( instance.getInstanceId()  )){
						writeInstanceState( s );
						break ; 
					}
				}

				
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


	private void writeInstanceState( InstanceState s) throws XMLStreamException {
		attribute(	"description",	s.getDescription() );
		attribute( "reason_code" ,  s.getReasonCode() );
		attribute( "state" , s.getState() );
		
		
		
	}


	public void usage() {
		super.usage();
	}



	

}
