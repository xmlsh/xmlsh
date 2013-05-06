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
import com.amazonaws.services.cloudformation.model.GetTemplateRequest;
import com.amazonaws.services.cloudformation.model.GetTemplateResult;
import com.amazonaws.services.cloudformation.model.Output;
import com.amazonaws.services.cloudformation.model.Parameter;
import com.amazonaws.services.cloudformation.model.Stack;

public class cfnGetTemplate extends AWSCFNCommand {

	

	@Override
	public int run(List<XValue> args) throws Exception {
		
		
		
		Options opts = getOptions();
		opts.parse(args);

		args = opts.getRemainingArgs();
		if( args.size() != 1 ){
			usage("stackname");
			return 1;
		}
		

		
		mSerializeOpts = this.getSerializeOpts(opts);
		
		
		try {
			getCFNClient(opts);
		} catch (UnexpectedException e) {
			usage( e.getLocalizedMessage() );
			return 1;
			
		}
		
	
        int ret = getTemplate(args.get(0).toString());

		
		
		return ret;
		
		
	}



	private int getTemplate(String stack) throws IOException, XMLStreamException, SaxonApiException, CoreException {
		

		OutputPort stdout = this.getStdout();
		mWriter = new SafeXMLStreamWriter(stdout.asXMLStreamWriter(mSerializeOpts));
		
		
		startDocument();
		startElement(this.getName());
		
		
		GetTemplateRequest request = new GetTemplateRequest().withStackName(stack);
		GetTemplateResult result = mAmazon.getTemplate(request);
		

 
		characters( result.getTemplateBody());
		
		endElement();
		endDocument();
		closeWriter();
		
		stdout.writeSequenceTerminator(mSerializeOpts);
		stdout.release();
		
		return 0;

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