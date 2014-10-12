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

import org.xmlsh.annotations.Command;
import org.xmlsh.annotations.Option;
import org.xmlsh.aws.util.AWSASCommand;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.SafeXMLStreamWriter;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;

import com.amazonaws.services.autoscaling.model.DescribeScalingProcessTypesRequest;
import com.amazonaws.services.autoscaling.model.DescribeScalingProcessTypesResult;
import com.amazonaws.services.autoscaling.model.ProcessType;


@Command( name= "as-describe-scaling-process-types" , 
options= { @Option("-a") } )
@Option("bar")
public class asDescribeScalingProcessTypes extends AWSASCommand
{

	@Override
	public int run(List<XValue> args) throws Exception
	{

		Options opts = getOptions();
		opts.parse(args);

		args = opts.getRemainingArgs();


		setSerializeOpts(this.getSerializeOpts(opts));

		try {
			getASClient(opts);
		} catch (UnexpectedException e) {
			usage(e.getLocalizedMessage());
			return 1;

		}

		int ret = describe();

		return ret;

	}

	private int describe() throws XMLStreamException, IOException,
	SaxonApiException, CoreException
	{

		OutputPort stdout = this.getStdout();
		mWriter = new SafeXMLStreamWriter(
				stdout.asXMLStreamWriter(getSerializeOpts()));

		startDocument();
		startElement(this.getName());

		traceCall("describeScalingProcessTypes");

		DescribeScalingProcessTypesRequest request = new DescribeScalingProcessTypesRequest();
		DescribeScalingProcessTypesResult result = getAWSClient()
				.describeScalingProcessTypes(request);

		writeScalingProceessTypes(result);

		endElement();
		endDocument();

		closeWriter();

		stdout.writeSequenceTerminator(getSerializeOpts());

		return 0;

	}

	private void writeScalingProceessTypes(
			DescribeScalingProcessTypesResult stypes) throws XMLStreamException
			{

		startElement("process-types");
		for (ProcessType pt : stypes.getProcesses()) {
			startElement("process-type");

			attribute("name", pt.getProcessName());
			endElement();

		}

		endElement();
			}

}

/*
 * Copyright (C) 2008-2014 David A. Lee.
 * 
 * The contents of this file are subject to the "Simplified BSD License" (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.opensource.org/licenses/bsd-license.php
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * 
 * The Original Code is: all this file.
 * 
 * The Initial Developer of the Original Code is David A. Lee
 * 
 * Portions created by (your name) are Copyright (C) (your legal entity). All
 * Rights Reserved.
 * 
 * Contributor(s): David A. Lee
 */