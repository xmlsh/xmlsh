/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.aws;

import net.sf.saxon.s9api.SaxonApiException;
import org.xmlsh.aws.util.AWSASCommand;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.SafeXMLStreamWriter;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;
import org.xmlsh.util.Util;

import java.io.IOException;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.autoscaling.model.AutoScalingGroup;
import com.amazonaws.services.autoscaling.model.DescribeAutoScalingGroupsRequest;
import com.amazonaws.services.autoscaling.model.DescribeAutoScalingGroupsResult;
import com.amazonaws.services.autoscaling.model.EnabledMetric;
import com.amazonaws.services.autoscaling.model.Instance;
import com.amazonaws.services.autoscaling.model.SuspendedProcess;
import com.amazonaws.services.autoscaling.model.TagDescription;

public class asDescribeGroups extends AWSASCommand {

	

	@Override
	public int run(List<XValue> args) throws Exception {
		
		
		
		Options opts = getOptions();
		opts.parse(args);

		args = opts.getRemainingArgs();
		parseCommonOptions(opts);


		
		setSerializeOpts(this.getSerializeOpts(opts));
		
		
		try {
			getASClient(opts);
		} catch (UnexpectedException e) {
			usage( e.getLocalizedMessage() );
			return 1;
			
		}
		
	
        int ret = describe(args);

		
		
		return ret;
		
		
	}



	private int describe(List<XValue> args) throws IOException, XMLStreamException, SaxonApiException, CoreException, InterruptedException {
		

		OutputPort stdout = this.getStdout();
		mWriter = new SafeXMLStreamWriter(stdout.asXMLStreamWriter(getSerializeOpts()));
		
		
		startDocument();
		startElement(this.getName());
		
		
		DescribeAutoScalingGroupsRequest request = new DescribeAutoScalingGroupsRequest();
		if( ! args.isEmpty())
		   request.setAutoScalingGroupNames( Util.toStringList(args));
		
	
		
		traceCall("describeAutoScalingGroups");
		DescribeAutoScalingGroupsResult result = null ; 
		
		
		int retry = rateRetry ;
		int delay = retryDelay ;
		do {
			try {
				result = mAmazon.describeAutoScalingGroups(request);
			    break;
				
			} catch( AmazonServiceException e ){
				mShell.printErr("AmazonServiceException" , e );
				if( retry > 0 && Util.isEqual("RequestLimitExceeded",e.getErrorCode())){
					mShell.printErr("AWS RequestLimitExceeded - sleeping " + delay );
					Thread.sleep( delay );
					retry--;
					delay *= 2 ;
				}
				else
					throw e;
			}
		} while( retry > 0 );
		
		
		
		
		for( AutoScalingGroup group :  result.getAutoScalingGroups())
			write(group);
		
		
		
		
		
		endElement();
		endDocument();
		closeWriter();
		
		stdout.writeSequenceTerminator(getSerializeOpts());
		
		return 0;

	}


	private void write(AutoScalingGroup group) throws XMLStreamException {
		startElement("group");
		attribute( "group-arn" , group.getAutoScalingGroupARN() );
		attribute( "name" , group.getAutoScalingGroupName() );
		

		attribute("create-time" , group.getCreatedTime());
		attribute( "default-cooldown" , group.getDefaultCooldown());
		attribute( "desired-capacity" , group.getDesiredCapacity());

		attribute("health-check-grace-period" , group.getHealthCheckGracePeriod());
		attribute( "health-check-type" , group.getHealthCheckType());

		attribute("launch-configuration-name" , group.getLaunchConfigurationName());

		
		attribute("max-size",group.getMaxSize());
		attribute("min-size",group.getMinSize());
		attribute("placement-group",group.getPlacementGroup());

		attribute("status" , group.getStatus());

		attribute("vpc-zone-id", group.getVPCZoneIdentifier());
		writeStringList( "termination-policies" , "termination-policy" , "name" , group.getTerminationPolicies());
		
		
		writeZones(group.getAvailabilityZones());
		writeMetrics(group.getEnabledMetrics());
		writeInstances(group.getInstances());
		writeELBNames(group.getLoadBalancerNames());
		writeSuspendedProcesses(group.getSuspendedProcesses());
		writeTags(group.getTags());
		endElement();
		
			
		
	}


	private void writeTags(List<TagDescription> tags) throws XMLStreamException {
		startElement("tags");
		for( TagDescription  tag : tags )
			writeTag( tag );
		endElement();
		
	}



	private void writeTag(TagDescription tag) throws XMLStreamException {
		startElement("tag");
		attribute("key",tag.getKey());
	    attribute("propagate-at-launch",tag.getPropagateAtLaunch());
		attribute("resource-id",tag.getResourceId());
		attribute("resource-type",tag.getResourceType());
		attribute("value",tag.getValue());
		endElement();
	    
	}



	private void writeSuspendedProcesses(List<SuspendedProcess> suspendedProcesses) throws XMLStreamException {
       startElement("suspended-processes");
       for( SuspendedProcess proc : suspendedProcesses )
    	   writeSuspendedProcess( proc );
       endElement();
		
	}



	private void writeSuspendedProcess(SuspendedProcess proc) throws XMLStreamException {
		startElement("process");
		attribute("name",proc.getProcessName());
		attribute("reason",proc.getSuspensionReason());
		endElement();
		
		
	}



	private void writeELBNames(List<String> loadBalancerNames) throws XMLStreamException {
		writeStringList( "elb-names" , "elb" , "name" , loadBalancerNames );
		
	}



	private void writeInstances(List<Instance> instances) throws XMLStreamException {
		startElement("instances");
		for( Instance inst : instances ){
			startElement("instance");
			attribute("availability-zone",inst.getAvailabilityZone());
			attribute("health-status",inst.getHealthStatus());
			attribute("instance-id",inst.getInstanceId());
			attribute("launch-configuration",inst.getLaunchConfigurationName());
			attribute("lifecycle-state",inst.getLifecycleState());
			endElement();
			
		}
		endElement();
		
	}



	private void writeMetrics(List<EnabledMetric> enabledMetrics) throws XMLStreamException {
		startElement("enabled-metrics");
		for( EnabledMetric metric : enabledMetrics ){
			startElement("enabled-metric");
			attribute("granularity",metric.getGranularity());
			attribute("metric",metric.getMetric());
			endElement();

		}
		endElement();

	}
		
		
	
}



/*
 * Copyright (C) 2008-2014   David A. Lee.
 * 
 * The contents of this file are subject to the "Simplified BSD License" (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.opensource.org/licenses/bsd-license.php 

 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied.
 * See the License for the specific language governing rights and limitations under the License.
 *
 * The Original Code is: all this file.
 *
 * The Initial Developer of the Original Code is David A. Lee
 *
 * Portions created by (your name) are Copyright (C) (your legal entity). All Rights Reserved.
 *
 * Contributor(s): David A. Lee
 * 
 */