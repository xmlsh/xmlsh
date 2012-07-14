package org.xmlsh.exist;


import java.io.InputStream;
import java.util.List;

import net.sf.saxon.s9api.XdmValue;
import org.xmlsh.core.InputPort;
import org.xmlsh.core.Options;
import org.xmlsh.core.Options.OptionValue;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.XValue;
import org.xmlsh.exist.util.ExistCommand;
import org.xmlsh.exist.util.ExistConnection;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.util.Util;



public class query extends ExistCommand {



	@Override
	public int run(List<XValue> args) throws Exception {
		
		Options opts = getOptions("r=raw,start:,max:,session:,q:,v,f:,m=meta,t=text,b=bool,p=prop:+");
		opts.parse(args);
		args = opts.getRemainingArgs();
		String uri = opts.getOptString("uri", "");
		ExistConnection conn = getConnection(opts,uri);
	
	    OutputPort out = getStdout();
	    String query = null ;
		if( opts.hasOpt("q"))
			query = opts.getOpt("q").getValue().toString();

		boolean bBool = opts.hasOpt("b");
		boolean bMeta = opts.hasOpt("meta");
		boolean bCache = opts.hasOpt("cache");
		boolean bRaw = opts.hasOpt("raw");
		int start = opts.getOptInt("start", 1);
		int max   = opts.getOptInt("max", -1);
		String session = opts.getOptString("session", null );
	
		
		OptionValue ov = opts.getOpt("f");
		SerializeOpts serializeOpts = getSerializeOpts(opts);
		if( ov != null ){
			if( query != null )
				throwInvalidArg(  "Cannot specifify both -q and -f");
			
			InputPort qin = getInput(ov.getValue());
			InputStream is = qin.asInputStream(serializeOpts);
			query = Util.readString(is,serializeOpts.getInputTextEncoding());
			is.close();
			qin.close();
			
			
		}
		if( query == null && args.size() < 1 )
			throwInvalidArg("No query specified");
		else
		if( query == null )
			query = args.remove(0).toString();
			
			
		

      /*
       *  Add Variables - for now only handle string variables
       */

		
		if( opts.hasOpt("v")){
			// Read pairs from args to set
			for( int i = 0 ; i < args.size()/2 ; i++ ){
				
				
				String name = args.get(i*2).toString();
				XValue value = args.get(i*2+1);
				
				conn.setVariable(name , value );
				
			}
				
			
		}
		
		
	   int ret = conn.query(query , bRaw , bMeta , bCache , start , max , session ,  out );
	   out.release();
	    
		
        conn.close();
		return ret;
	}


	
	

	
	

}



//
//
//Copyright (C) 2008-2012  David A. Lee.
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
