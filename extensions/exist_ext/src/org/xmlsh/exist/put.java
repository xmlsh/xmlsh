package org.xmlsh.exist;


import java.util.List;

import org.xmlsh.core.InputPort;
import org.xmlsh.core.Options;
import org.xmlsh.core.XValue;
import org.xmlsh.exist.util.ExistCommand;
import org.xmlsh.exist.util.ExistConnection;
import org.xmlsh.util.Util;

public class put extends ExistCommand {


	@Override
	public int run(List<XValue> args) throws Exception {
		
		
		
		Options opts = getOptions("f:,uri:,baseuri:,t=text,b=binary,x=xml,q=xquery");
		opts.parse(args);
		args = opts.getRemainingArgs();
		
		String uri = opts.getOptString("uri", "");
		String baseUri = opts.getOptString("baseuri", "");
		if( Util.isBlank(uri) && Util.isBlank(baseUri)){
			usage("One of -uri or -baseuri required");
			return 1;
			
		}
		
		if( !Util.isBlank(uri) && !Util.isBlank(baseUri)){
			usage("Only one of -uri or -baseuri allowed");
			return 1;
			
		}
		

		int ret = 0;
		

		if( args.size() == 0 ){
			if( Util.isBlank(uri)){
				usage("-uri required");
				return 1;
			}
			
			ExistConnection conn = getConnection(opts, uri);
			
			InputPort in = null;
			if( args.size() > 0 )
				in = this.getInput(args.get(0));
			else
				in = getStdin();
			
			try {
				ret = conn.put(in);
			} finally {
				in.release();
			}

	        conn.close();
		}
		else {

			
			for( XValue v : args ){
				
				String suri = uri;
				
				if( Util.isBlank(suri) ){
					if( v.isAtomic() )
						suri = baseUri + "/" +  v.toString();
					else
					{
						usage("-uri required");
						return 1;
					}
				}
				
				
				
				ExistConnection conn = getConnection(opts, suri);
				
				InputPort in = getInput(v);
				ret += conn.put(  in );
				in.release();
				
				conn.close();
			}
		
		}
		
		
		
		
		
		
		return ret;
	}

	
	

}



//
//
//Copyright (C) 2008-2014  David A. Lee.
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
