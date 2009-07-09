/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.commands.experimental;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.xmlsh.core.Options;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.util.Util;

public class http extends XCommand {

	
	private static Logger mLogger = LogManager.getLogger( http.class);


	
	
	@Override
	public int run( List<XValue> args )
	throws Exception 
	{
		
		Options opts = new Options( "get,put,post,head,options,delete,connectTimeout:,readTimeout:,+useCaches,+followRedirects" , args );
		opts.parse();
		String method = "GET";
		boolean doInput = true ;
		boolean doOutput = false ;
		
		if( opts.hasOpt("get") )
			method = "GET";
		else
		if(  opts.hasOpt("put") ){
			method = "PUT";
			doInput = false ; 
			doOutput = true ;
		}
		else
		if( opts.hasOpt("post") ){
			method = "POST" ;
			doOutput = true ;
		}
		else
		if( opts.hasOpt("head") )
			method = "HEAD" ;
		else
		if( opts.hasOpt("options") )
			method = "OPTIONS" ;
		else
		if( opts.hasOpt("delete") )
			method = "DELETE" ;
		else
		if( opts.hasOpt("trace") )
			method = "TRACE" ;
	
		
		
		
		
		int ret = 0;
		if( ! opts.hasRemainingArgs()){
			usage();
			return -1;
		}
		String surl = opts.getRemainingArgs().get(0).toString();
		
	
		URL url = new URL(surl);

		URLConnection conn = url.openConnection();
		if( conn instanceof HttpURLConnection ){
			
			
			
			
			
			HttpURLConnection http = (HttpURLConnection) conn;
	
			setOptions( http , opts );
			
			
			http.setRequestMethod(method);
			
			http.setDoInput(doInput);
			http.setDoOutput(doOutput);
			
			if( doOutput ){
				conn.connect();
				OutputStream out = http.getOutputStream();
				Util.copyStream( getStdin().asInputStream(getSerializeOpts()) , out );
				out.close();
				
				
			}
			ret = http.getResponseCode();
			if( ret == 200 )
				ret = 0;
		}
		
		if( doInput ){
			InputStream in = conn.getInputStream();
			Util.copyStream(in, getStdout().asOutputStream());
			in.close();
			
		}
		
		
		
		return 0;
	}



	private void setOptions(HttpURLConnection http, Options opts) {
		
		if( opts.hasOpt("connectTimeout"))
			http.setConnectTimeout( opts.getOptInt("connectTimeout", 0));
		if( opts.hasOpt("readTimeout"))
			http.setReadTimeout( opts.getOptInt("readTimeout", 0));
		if( opts.hasOpt("useCaches"))
			http.setUseCaches( opts.getOpt("useCaches").getFlag());
		if( opts.hasOpt("followRedirects"))
			http.setInstanceFollowRedirects(  opts.getOpt("followRedirects").getFlag());
			
		
			
		
	}



	private void usage() {
		printErr("usage: http [-get|-put|-post url");
		
	}
	
	
	

}



//
//
//Copyright (C) 2008,2009 David A. Lee.
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
