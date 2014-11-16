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
import org.xmlsh.core.CoreException;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.SafeXMLStreamWriter;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;
import org.xmlsh.core.io.OutputPort;
import org.xmlsh.types.xtypes.IXValueContainer;
import org.xmlsh.types.xtypes.IXValueMap;
import org.xmlsh.util.StringPair;
import org.xmlsh.util.Util;

import com.amazonaws.services.cloudformation.model.CreateStackRequest;
import com.amazonaws.services.cloudformation.model.CreateStackResult;
import com.amazonaws.services.cloudformation.model.OnFailure;
import com.amazonaws.services.cloudformation.model.Parameter;
import com.amazonaws.services.cloudformation.model.Tag;

public class cfnCreateStack extends AWSCFNCommand {



	@Override
	public int run(List<XValue> args) throws Exception {



		Options opts = getOptions("capability:+,disable-rollback,fail=on-failure:,notification-arn:+,name:,template-file=f:,template-url=url:,timeout:,tag:+,params=parameters:");
		opts.parse(args);

		args = opts.getRemainingArgs();



		setSerializeOpts(this.getSerializeOpts(opts));


		try {
			getCFNClient(opts);
		} catch (UnexpectedException e) {
			usage( e.getLocalizedMessage() );
			return 1;

		}


		int ret = createStack( opts);



		return ret;


	}



	private int createStack(Options opts) throws IOException, XMLStreamException, SaxonApiException, CoreException {


		OutputPort stdout = this.getStdout();
		mWriter = new SafeXMLStreamWriter(stdout.asXMLStreamWriter(getSerializeOpts()));


		startDocument();
		startElement(this.getName());

		CreateStackRequest request = new CreateStackRequest();

		// "capability:+,disable-rollback,notification-arn:+,name:,template:,timeout:,tag:+");

		if( opts.hasOpt("capability") )
			request.setCapabilities(Util.toStringList(opts.getOptValues("capability")));

		String onFail = opts.getOptString("on-failure", null);
		if( onFail != null)
			request.setOnFailure(OnFailure.fromValue(onFail));
		else
			request.setDisableRollback(opts.getOptFlag("disable-rollback", false));


		if( opts.hasOpt("notification-arn"))
			request.setNotificationARNs(Util.toStringList(opts.getOptValues("notification-arn")));

		request.setStackName( opts.getOptStringRequired("name"));

		if( opts.hasOpt("template-file"))
			request.setTemplateBody( Util.readString( mShell.getFile(opts.getOptValue("template-file")), getSerializeOpts().getInput_text_encoding()));
		else
			request.setTemplateURL( opts.getOptStringRequired("template-url"));

		if( opts.hasOpt("timeout" ))
			request.setTimeoutInMinutes((int) opts.getOptLong("timeout", 10));
		if( opts.hasOpt("tag"))
			request.setTags( getTags( opts.getOptValues("tag")));

		request.setParameters(getParameters( opts ));

		traceCall("createStack");

		CreateStackResult result = getAWSClient().createStack(request);

		writeStackResult(result,request.getStackName());




		endElement();
		endDocument();
		closeWriter();

		stdout.writeSequenceTerminator(getSerializeOpts());

		return 0;

	}



	/*
	 * Get CF parameters either as pairs of name/value arguments (legacy)
	 * Or as a Properties object 
	 */
	private Collection<Parameter> getParameters(Options opts) throws InvalidArgumentException {
		
		if( opts.hasOpt("parameters")){
			List<Parameter> parameters = new ArrayList<>();
			XValue xp = opts.getOptValue("parameters");
			for( XValue x : xp ) { // iterate over a sequenced param 
				Collection<? extends Parameter> ps = getParameters( x );
				if( ps != null )
				parameters.addAll( ps );
			}
			return parameters ;
		}
		return getParameters( opts.getRemainingArgs());
	}
		

		  
		
  // Get one or more parameters from a single XValue
	private Collection<? extends Parameter> getParameters(XValue x) throws InvalidArgumentException {
		if( x.isInstanceOf( IXValueMap.class )){
			List<Parameter> parameters = new ArrayList<>();

			IXValueMap<?> xmap  = x.asInstanceOf(IXValueMap.class);
			for( String key : xmap.keySet() ){
				XValue value = xmap.get(key);
				Parameter param = new Parameter().withParameterKey( key)
						.withParameterValue(value.toString());
				parameters.add( param );
				
			}
		
			return parameters ;
		}
		if( x.isInstanceOf(IXValueContainer.class)){
			List<Parameter> parameters = new ArrayList<>();
			IXValueContainer<?> c   = x.asInstanceOf(IXValueContainer.class);

			for( XValue xv : c  ){
				Collection<? extends Parameter> ps = getParameters( xv );
				if( ps != null )
				  parameters.addAll( ps );
				
			}
			
			return parameters ;
		}
		return null;
   
   
   }



private Collection<Parameter> getParameters(List<XValue> args) {

		int sz = args.size();
		Collection<Parameter> params = new ArrayList<Parameter>( sz );
		for( int i = 0 ; i < sz ; i += 2 ){
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



	private void writeStackResult(CreateStackResult result, String name ) throws XMLStreamException {
		startElement("stack");
		attribute("stack-id",result.getStackId());
		attribute("stack-name",name);
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