/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.aws;

import java.io.IOException;
import java.io.PrintWriter;
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
import org.xmlsh.sh.shell.SerializeOpts;

import com.amazonaws.services.cloudformation.model.GetTemplateRequest;
import com.amazonaws.services.cloudformation.model.GetTemplateResult;

public class cfnGetTemplate extends AWSCFNCommand {

	

	@Override
	public int run(List<XValue> args) throws Exception {
		
		
		
		Options opts = getOptions("j=json,n=name:");
		opts.parse(args);
 
		
		args = opts.getRemainingArgs();
		
		boolean bJson = opts.hasOpt("json");

		
		mSerializeOpts = this.getSerializeOpts(opts);
		
		
		try {
			getCFNClient(opts);
		} catch (UnexpectedException e) {
			usage( e.getLocalizedMessage() );
			return 1;
			
		}
		
	
        int ret = getTemplate(opts.getOptStringRequired("name"),bJson);

		
		
		return ret;
		
		
	}



	private int getTemplate(String stack, boolean bJson) throws IOException, XMLStreamException, SaxonApiException, CoreException {
		


		OutputPort stdout = this.getStdout();
		
		

		GetTemplateRequest request = new GetTemplateRequest().withStackName(stack);
		
		traceCall("getTemplate");

		GetTemplateResult result = mAmazon.getTemplate(request);
		
		
		
		if( bJson ){
			SerializeOpts opts = mSerializeOpts.clone();
			opts.setOutput_text_encoding("utf-8");
			
			PrintWriter w = stdout.asPrintWriter(opts);
			w.print(result.getTemplateBody());
			w.close();

			
		} else {
			mWriter = new SafeXMLStreamWriter(stdout.asXMLStreamWriter(mSerializeOpts));
			
			
			startDocument();
			startElement(this.getName());
			
			
			
	
	 
			characters( result.getTemplateBody());
			
			endElement();
			endDocument();
			closeWriter();
			
			stdout.writeSequenceTerminator(mSerializeOpts);
		}
		stdout.release();
		return 0;

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