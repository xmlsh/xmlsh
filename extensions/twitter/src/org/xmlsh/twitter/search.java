/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.twitter;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Array;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;
import javax.management.remote.JMXConnector;
import javax.xml.stream.XMLStreamException;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.twitter.util.TwitterCommand;

public class search extends TwitterCommand {

	@Override
	public int run(List<XValue> args) throws Exception {
		
		Options opts = new Options(sCOMMON_OPTS + ",q=query:",SerializeOpts.getOptionDefs());
		opts.parse(args);
		mSerializeOpts = this.getSerializeOpts(opts);
		
		
		args = opts.getRemainingArgs();
		
		String query = opts.getOptStringRequired("query");
		
		
		try {
			
			
			String u = "http://search.twitter.com/search.json?";
			
			u = u + "q=" +  URLEncoder.encode(query, "UTF8");
			
			String result = httpGetString( u );
			
			
			

			OutputPort out = this.getStdout();
			mWriter = out.asXMLStreamWriter( mSerializeOpts  );
			
			startDocument();
			startElement("twitter");
			mWriter.writeDefaultNamespace(kTWITTER_NS);

			;

			JSONTokener tokenizer = new JSONTokener(result);

			/*
			 * Assume JSON file is wrapped by an Object
			 */
			JSONObject obj = new JSONObject(tokenizer);
			writeJSON(obj);
			
			
			endElement();
			endDocument();
			closeWriter();
	
				
			out.release();
		} finally {
			
		
		}
		return 0;
		
		
		
	}

		

}



//
//
//Copyright (C) 2008,2009,2010,2011,2012 David A. Lee.
//
//The contents of this file are subject to the "Simplified BSD License" (the "License");
//you may not use this file except in compliance with the License. You may obtain a copy of the
//License at http://www.opensource.org/licenses/bsd-license.php 
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied.
//See the License for the specific language governing rights and limitations under the License.
//
//The Original Code is: all this file.
//
//The Initial Developer of the Original Code is David A. Lee
//
//Portions created by (your name) are Copyright (C) (your legal entity). All Rights Reserved.
//
//Contributor(s): none.
//
