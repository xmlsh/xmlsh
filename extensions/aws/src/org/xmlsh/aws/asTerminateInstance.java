/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.aws;

import java.io.IOException;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import net.sf.saxon.s9api.SaxonApiException;

import org.xmlsh.aws.util.AWSASCommand;
import org.xmlsh.aws.util.SafeXMLStreamWriter;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;

import com.amazonaws.services.autoscaling.model.Activity;
import com.amazonaws.services.autoscaling.model.TerminateInstanceInAutoScalingGroupRequest;
import com.amazonaws.services.autoscaling.model.TerminateInstanceInAutoScalingGroupResult;


public class asTerminateInstance extends AWSASCommand {

	

	@Override
	public int run(List<XValue> args) throws Exception {
		
		
		
		Options opts = getOptions("d=decrement");
		opts.parse(args);

		args = opts.getRemainingArgs();
		
		if( args.size() != 1 )
			usage("as-terminate-instance -decrement instance-id");
		

		boolean bDecrement = opts.hasOpt("decrement");
		String instanceId = args.get(0).toString();

		
		mSerializeOpts = this.getSerializeOpts(opts);
		
		
		try {
			getASClient(opts);
		} catch (UnexpectedException e) {
			usage( e.getLocalizedMessage() );
			return 1;
			
		}
		
	
		int ret = terminate( instanceId , bDecrement );
		
		
		
		return ret;
		
		
	}

	


	private int terminate(String instanceId, boolean bDecrement) throws IOException, XMLStreamException, SaxonApiException, CoreException 
	{

		OutputPort stdout = this.getStdout();
		mWriter = new SafeXMLStreamWriter(stdout.asXMLStreamWriter(mSerializeOpts));
		
		
		startDocument();
		startElement(this.getName());
		 
		
		
		
		
		traceCall("terminateInstanceInAutoScalingGroup");

		TerminateInstanceInAutoScalingGroupRequest request = new TerminateInstanceInAutoScalingGroupRequest( ).
				withInstanceId(instanceId).withShouldDecrementDesiredCapacity(bDecrement);
		TerminateInstanceInAutoScalingGroupResult result = mAmazon.terminateInstanceInAutoScalingGroup(request);
		
		writeActivity( result.getActivity() );
		
		
		
		endElement();
		endDocument();
		
	closeWriter();
		
		stdout.writeSequenceTerminator(mSerializeOpts);
		stdout.release();
	
		
		return 0 ;
	
	
	}




	private void writeActivity(Activity activity) throws XMLStreamException {
		startElement("activity");
		attribute( "activity-id", activity.getActivityId());
		attribute( "group-name", activity.getAutoScalingGroupName()); 
		attribute( "cause", activity.getCause()); 
		attribute( "description", activity.getDescription()); 
		attribute( "details", activity.getDetails());
		attribute( "end-time", activity.getEndTime());
		attribute( "progress", activity.getProgress());
		attribute( "start-time" , activity.getStartTime() );
		attribute( "status-code" , activity.getStatusCode());
		attribute( "status-message" , activity.getStatusMessage() );
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