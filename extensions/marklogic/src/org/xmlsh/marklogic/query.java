package org.xmlsh.marklogic;


import java.io.InputStream;
import java.util.List;

import org.xmlsh.core.InputPort;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.XValue;
import org.xmlsh.core.Options.OptionValue;
import org.xmlsh.marklogic.util.MLCommand;
import org.xmlsh.util.Util;

import com.marklogic.xcc.AdhocQuery;
import com.marklogic.xcc.ContentSource;
import com.marklogic.xcc.RequestOptions;
import com.marklogic.xcc.ResultSequence;
import com.marklogic.xcc.Session;
import com.marklogic.xcc.ValueFactory;
import com.marklogic.xcc.types.XName;
import com.marklogic.xcc.types.XSString;
import com.marklogic.xcc.types.XdmVariable;

public class query extends MLCommand {

	private Session session;
	private AdhocQuery request;
	private RequestOptions options;

	@Override
	public int run(List<XValue> args) throws Exception {
		
		Options opts = new Options("c=connect:,q:,v,f:,t");
		opts.parse(args);
		args = opts.getRemainingArgs();
		
		ContentSource cs = getConnection(opts);
	
	    OutputPort out = getStdout();
	    String query = null ;
		if( opts.hasOpt("q"))
			query = opts.getOpt("q").getValue().toString();
		boolean asText = opts.hasOpt("t");
	
		session = cs.newSession();
		request = session.newAdhocQuery (null);

		OptionValue ov = opts.getOpt("f");
		if( ov != null ){
			if( query != null )
				throwInvalidArg(  "Cannot specifify both -q and -f");
			
			InputPort qin = getInput(ov.getValue());
			InputStream is = qin.asInputStream(getSerializeOpts());
			query = Util.readString(is);
			is.close();
			qin.close();
			
			
		}
		

      /*
       *  Add Variables - for now only handle string variables
       */

		if( opts.hasOpt("v")){
			// Read pairs from args to set
			for( int i = 0 ; i < args.size()/2 ; i++ ){
				
				
				String name = args.get(i*2).toString();
				XValue value = args.get(i*2+1);
				XName xname = new XName(name);
				XSString svalue = ValueFactory.newXSString( value.toString() );
				XdmVariable var = ValueFactory.newVariable( xname , svalue );
				request.setVariable(var);
					
				
			}
				
			
		}
		
		request.setQuery (query);
		request.setOptions (options);
		
		
	    ResultSequence rs = session.submitRequest (request);


        writeResult(rs, out, asText );
        rs.close();
		
        session.close();

       // out.close();
	
		
		return 0;
	}

	
	

	
	

}



//
//
//Copyright (C) 2008,2009 , David A. Lee.
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
