/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.aws;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
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
import org.xmlsh.util.StringPair;
import org.xmlsh.util.Util;

import com.amazonaws.services.cloudformation.model.CreateStackRequest;
import com.amazonaws.services.cloudformation.model.CreateStackResult;
import com.amazonaws.services.cloudformation.model.Parameter;
import com.amazonaws.services.cloudformation.model.Tag;

public class cfnCreateStack extends AWSCFNCommand {

	

	@Override
	public int run(List<XValue> args) throws Exception {
		
		
		
		Options opts = getOptions("capability:+,disable-rollback,notification-arn:+,name:,template-file=f:,template-url=url:,timeout:,tag:+");
		opts.parse(args);

		args = opts.getRemainingArgs();
		

		
		mSerializeOpts = this.getSerializeOpts(opts);
		
		
		try {
			getCFNClient(opts);
		} catch (UnexpectedException e) {
			usage( e.getLocalizedMessage() );
			return 1;
			
		}
		
	
        int ret = createStack(args, opts);

		
		
		return ret;
		
		
	}



	private int createStack(List<XValue> args, Options opts) throws IOException, XMLStreamException, SaxonApiException, CoreException {
		

		OutputPort stdout = this.getStdout();
		mWriter = new SafeXMLStreamWriter(stdout.asXMLStreamWriter(mSerializeOpts));
		
		
		startDocument();
		startElement(this.getName());
		
		CreateStackRequest request = new CreateStackRequest();
		
		// "capability:+,disable-rollback,notification-arn:+,name:,template:,timeout:,tag:+");

		if( opts.hasOpt("capability") )
			request.setCapabilities(Util.toStringList(opts.getOptValues("capability")));
		
		request.setDisableRollback(opts.getOptFlag("disable-rollback", false));
		
		if( opts.hasOpt("notification-arn"))
			request.setNotificationARNs(Util.toStringList(opts.getOptValues("notification-arn")));
		
		request.setStackName( opts.getOptStringRequired("name"));

		if( opts.hasOpt("template-file"))
			request.setTemplateBody( Util.readString( mShell.getFile(opts.getOptValue("template-file")), mSerializeOpts.getInput_text_encoding()));
		else
			request.setTemplateURL( opts.getOptStringRequired("template-url"));
		
		if( opts.hasOpt("timeout" ))
			request.setTimeoutInMinutes((int) opts.getOptLong("timeout", 10));
		if( opts.hasOpt("tag"))
			request.setTags( getTags( opts.getOptValues("tag")));
		
		request.setParameters(getParameters( args ));
		
		CreateStackResult result = mAmazon.createStack(request);
		
		writeStackResult(result);
		
		
		
		
		endElement();
		endDocument();
		closeWriter();
		
		stdout.writeSequenceTerminator(mSerializeOpts);
		stdout.release();
		
		return 0;

	}



	private Collection<Parameter> getParameters(List<XValue> args) {
		int sz = args.size();
		Collection<Parameter> params = new ArrayList<Parameter>( sz );
		for( int i = 0 ; i < sz+1 ; i += 2 ){
			String name = args.get(i).toString();
			
			String value = i+1 >= sz ? "" : args.get(i+1).toString();
			params.add( new Parameter().withParameterKey(name).withParameterValue(value)) ;
			
		}
		return params;
		
		
	}



	private Collection<Tag> getTags(List<XValue> values) {
	
		Collection<Tag> tags = new ArrayList<Tag>( values.size() );
		for( XValue v : values )
			tags.add( parseTag( v ));
		return tags ;
	}



	private Tag parseTag(XValue v) {
		StringPair pair = new StringPair( v.toString() , '=');
		return new Tag().withKey(pair.getLeft()).withValue( pair.getRight());
		
		
	}



	private void writeStackResult(CreateStackResult result) throws XMLStreamException {
		startElement("stack");
		attribute("stack-id",result.getStackId());
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