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
import org.xmlsh.aws.util.AWSCFNCommand;
import org.xmlsh.aws.util.SafeXMLStreamWriter;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;

import com.amazonaws.services.cloudformation.model.DescribeStacksRequest;
import com.amazonaws.services.cloudformation.model.DescribeStacksResult;
import com.amazonaws.services.cloudformation.model.Output;
import com.amazonaws.services.cloudformation.model.Parameter;
import com.amazonaws.services.cloudformation.model.Stack;

public class cfnDescribeStacks extends AWSCFNCommand {

	

	@Override
	public int run(List<XValue> args) throws Exception {
		
		
		
		Options opts = getOptions();
		opts.parse(args);

		args = opts.getRemainingArgs();
		

		
		mSerializeOpts = this.getSerializeOpts(opts);
		
		
		try {
			getCFNClient(opts);
		} catch (UnexpectedException e) {
			usage( e.getLocalizedMessage() );
			return 1;
			
		}
		
	
        int ret = describe(args);

		
		
		return ret;
		
		
	}



	private int describe(List<XValue> args) throws IOException, XMLStreamException, SaxonApiException, CoreException {
		

		OutputPort stdout = this.getStdout();
		mWriter = new SafeXMLStreamWriter(stdout.asXMLStreamWriter(mSerializeOpts));
		
		
		startDocument();
		startElement(this.getName());
		
		
		
		DescribeStacksRequest request = new DescribeStacksRequest();
		if( args.size() > 0 )
			request.setStackName(args.get(0).toString());
		DescribeStacksResult result = mAmazon.describeStacks(request);
		

		for( Stack  stack : result.getStacks() )
			writeStack( stack )
		;
		
		endElement();
		endDocument();
		closeWriter();
		
		stdout.writeSequenceTerminator(mSerializeOpts);
		stdout.release();
		
		return 0;

	}


	private void writeStack(Stack stack) throws XMLStreamException {
		startElement("stack");
		attribute("creation-time" , stack.getCreationTime());
		attribute("description" ,stack.getDescription());
		attribute("disable-rollback" ,stack.getDisableRollback());
		attribute("last-update-time" ,stack.getLastUpdatedTime());
		attribute("stack-id" ,stack.getStackId());
		attribute("name" ,stack.getStackName());
		attribute("status" ,stack.getStackStatus());
		attribute("reason" ,stack.getStackStatusReason());
		
		
		
		writeParameters( stack.getParameters() );

		writeOutputs( stack.getOutputs() );
		
		writeCapibilities(stack.getCapabilities());
		writeNotifications(stack.getNotificationARNs());
		
	}



	private void writeNotifications(List<String> notificationARNs) throws XMLStreamException {
		writeStringList("notification-arns", "notification-arn", "arn" , notificationARNs);		
	}



	private void writeCapibilities(List<String> capabilities) throws XMLStreamException {

		writeStringList("capabilities", "capability", "name" , capabilities);
		
		
	}



	private void writeOutputs(List<Output> outputs) throws XMLStreamException {
		startElement("outputs")	;
		for( Output o : outputs )
			writeOutput( o );
		endElement();
		
	}



	private void writeOutput(Output o) throws XMLStreamException {
		startElement("output")	;
		attribute("description" ,o.getDescription());
		attribute("output-key" ,o.getOutputKey());
		attribute("output-value" ,o.getOutputValue());
		endElement();

	}



	private void writeParameters(List<Parameter> parameters) throws XMLStreamException {
		startElement("parameters");
		for( Parameter p : parameters )
			writeParameter( p );
		endElement();
	}



	private void writeParameter(Parameter p) throws XMLStreamException {
		startElement("parameter");
		attribute("key",p.getParameterKey());
		attribute("value",p.getParameterValue());
		endElement();
		
	}


		
	
}



/*
 * Copyright (C) 2008-2013   David A. Lee.
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