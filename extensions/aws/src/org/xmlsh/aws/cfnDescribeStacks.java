/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.aws;

import net.sf.saxon.s9api.SaxonApiException;
import org.xmlsh.aws.util.AWSCFNCommand;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.SafeXMLStreamWriter;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;

import java.io.IOException;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import com.amazonaws.services.cloudformation.model.DescribeStacksRequest;
import com.amazonaws.services.cloudformation.model.DescribeStacksResult;
import com.amazonaws.services.cloudformation.model.Output;
import com.amazonaws.services.cloudformation.model.Stack;

public class cfnDescribeStacks extends AWSCFNCommand {



	@Override
	public int run(List<XValue> args) throws Exception {


		Options opts = getOptions("n=name:");
		opts.parse(args);

		args = opts.getRemainingArgs();



		setSerializeOpts(this.getSerializeOpts(opts));


		try {
			getCFNClient(opts);
		} catch (UnexpectedException e) {
			usage( e.getLocalizedMessage() );
			return 1;

		}


		int ret = describe(opts.getOptString("name", null ) );



		return ret;


	}



	private int describe(String name) throws IOException, XMLStreamException, SaxonApiException, CoreException {


		OutputPort stdout = this.getStdout();
		mWriter = new SafeXMLStreamWriter(stdout.asXMLStreamWriter(getSerializeOpts()));


		startDocument();
		startElement(this.getName());



		DescribeStacksRequest request = new DescribeStacksRequest();
		if( name != null )
			request.setStackName(name);

		traceCall("describeStacks");

		DescribeStacksResult result = mAmazon.describeStacks(request);


		for( Stack  stack : result.getStacks() )
			writeStack( stack )
			;

		endElement();
		endDocument();
		closeWriter();

		stdout.writeSequenceTerminator(getSerializeOpts());

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