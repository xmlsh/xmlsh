/**

 * $Id: xpwd.java 21 2008-07-04 08:33:47Z daldei $
 * $Date: 2008-07-04 04:33:47 -0400 (Fri, 04 Jul 2008) $
 *
 */

package org.xmlsh.commands.json;

import org.xmlsh.core.InputPort;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.json.JSONSerializeOpts;
import org.xmlsh.json.JXConverter;
import org.xmlsh.json.JXONConverter;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.util.JsonUtils;
import org.xmlsh.util.Util;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

/*
 * 
 * Convert XML files to an CSV file
 * 
 * Arguments
 * 
 * -header		Add a header row
 * 
 * 
 */

public class json2xml extends XCommand
{
	private static final String	kJXML_URI	  = "http://www.xmlsh.org/jxml";

	private boolean	            bFirstElement	= true;
	private int	                depth	      = 0;

	public int run(List<XValue> args) throws Exception
	{
		Options opts = new Options("+jsonp,p=port:" ,SerializeOpts.getOptionDefs());
		opts.parse(args);

		args = opts.getRemainingArgs();
		String port = opts.getOptString("p", null);
		boolean jsonp = opts.getOptFlag("jsonp",false);

		OutputPort stdout = getStdout();

		setSerializeOpts(opts);
		SerializeOpts inputOpts = getSerializeOpts();

		InputPort in = args.isEmpty() ? this.getStdin() : this.getInput(args.get(0));
		XMLStreamWriter sw = null ;
		InputStream is = null;
		try {
				
	
			JsonFactory jsonFactory = new JsonFactory(); // or, for data binding,
														 // org.codehaus.jackson.mapper.MappingJsonFactory
			is = in.asInputStream(inputOpts);
			
			if( jsonp ) {
				String jsonpFunc = Util.skipToByte(is, '(');
				if( jsonpFunc == null ) {
					mShell.printErr("No JSONP prefix found");
					return 1;
				}
			}
			 
			/* Note: no encoding argument is taken since it can always be
			     * auto-detected as suggested by JSON RFC.
			     */
			JsonParser jp = jsonFactory.createParser(is); // or URL, Stream,

			JSONSerializeOpts jopts = new JSONSerializeOpts();
													   // Reader, String, byte[]
			/*
			 * Assume JSON file is wrapped by an Object
			 */
	
			sw = stdout.asXMLStreamWriter(getSerializeOpts());
		
			JXConverter converter = new JXONConverter(jopts,getSerializeOpts());

			try {  
				
				converter.convertFromJson( jp , sw );
				
			} 
			finally {
			
				if( jp != null ) {
					jp.close();
				}
				Util.safeClose(sw);
			}
			
		} finally {
		
			if( sw != null ) {
				sw.flush();
				sw.close();
			}

			Util.safeRelease(in);
		}
		return 0;

	}

	

}

//
//
// Copyright (C) 2008-2014 David A. Lee.
//
// The contents of this file are subject to the "Simplified BSD License" (the
// "License");
// you may not use this file except in compliance with the License. You may
// obtain a copy of the
// License at http://www.opensource.org/licenses/bsd-license.php
//
// Software distributed under the License is distributed on an "AS IS" basis,
// WITHOUT WARRANTY OF ANY KIND, either express or implied.
// See the License for the specific language governing rights and limitations
// under the License.
//
// The Original Code is: all this file.
//
// The Initial Developer of the Original Code is David A. Lee
//
// Portions created by (your name) are Copyright (C) (your legal entity). All
// Rights Reserved.
//
// Contributor(s): none.
//
