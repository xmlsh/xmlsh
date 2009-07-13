/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.commands.experimental;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.xmlsh.core.CommandFactory;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.ICommand;
import org.xmlsh.core.Options;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.core.Command;
import org.xmlsh.sh.shell.Shell;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class httpserver extends XCommand {
	
	static	HttpServer 	mServer = null;
	

	
	private static Logger mLogger = LogManager.getLogger( httpserver.class);

	
	public class MyHandler implements HttpHandler {
		private		Shell	 	mShell = null;
		private	 	File		mInitialCD;
		private		Command	mGet;
		private		Command mPut;
		private		Command mPost;
		
		MyHandler( Shell shell, String getFunc, String putFunc , String postFunc  ) throws CoreException 
		{
			mShell = shell.clone();	// clone shell for execution
			
			// need to save the CD so we can restore it in the new thread
			mInitialCD = mShell.getCurdir();
			
			if(  getFunc != null )
				mGet =  mShell.parseEval(getFunc);
			if( putFunc != null )
				mPut =  mShell.parseEval(putFunc);
			if( postFunc != null )
				mPost = mShell.parseEval(postFunc);
			

		}
		
		public void handle(HttpExchange http) throws IOException {
				
			
			try {
				
				mShell.setCurdir(mInitialCD);
			  //  System.out.println("Got request in thread: " + Thread.currentThread().getName() );

		       
		       String method = http.getRequestMethod();
		       Headers headers = http.getRequestHeaders();
		       
		   	
				Command cmd = null ;
						
		       if( method.equals("GET") )
				  cmd = mGet;
		       else
			   if( method.equals("PUT") )
					  cmd = mPut;
			   else
		       if( method.equals("POST") && mPost != null )
					  cmd = mPost ;
		       
		       if( cmd == null ){
		    	   http.sendResponseHeaders(405, -1);
		    	   http.close();
		    	   return ;
		       }
		    	   
		       InputStream is = http.getRequestBody();
		       http.sendResponseHeaders(200, 0);
		       
		       URI uri = http.getRequestURI();

		       
		       

	           // http.sendResponseHeaders(200, response.length());
	           OutputStream os = http.getResponseBody();
	           
			   mShell.getEnv().setStdin( is );
			   mShell.getEnv().setStdout( os  );
			   
			   
	
			  List<XValue> args = new ArrayList<XValue>();
			  String query = uri.getQuery();
			  String path = uri.getPath();
			  args.add( new XValue(path));
			  args.add( new XValue(query));
			  
			  mShell.setArgs(args);
			  mShell.exec(cmd);
			  
	          os.close();
	          while( is.read() > 0 )
	        	  ;
	          is.close();
	          http.close();
	          
	          
			} catch( Exception e )
			{
	
				
				mLogger.error("Exception running http handler" , e );
				try {
					mShell.getEnv().setStdin((InputStream)null);
					mShell.getEnv().setStdout((OutputStream)null);
					mShell.close();
					
					} catch( Exception e2 )
					{}
			}
			

		}



	}

	
	@Override
	public int run( List<XValue> args )
	throws Exception 
	{
		
		Options opts = new Options( "port:,context:,handler:,get:,put:,post:" , args );
		opts.parse();
		
		if( ! opts.hasRemainingArgs() ){
			usage();
			return -1;
			
		}
		
		String arg = opts.getRemainingArgs().get(0).toString();
		if( arg.equals("stop")){
			if( mServer != null )
				mServer.stop(0);
			mServer = null ;
			return 0;
		}
		else
		if( arg.equals("start"))
			return start( opts );
		else
			usage();
		return 0;
		


	}


	private int start(Options opts) throws IOException, CoreException {
		int port = opts.getOptInt("port",80);
		String context = opts.getOptString("context", "/");
		String get = opts.getOptString("get", null);
		String put = opts.getOptString("put", null);	
		String post = opts.getOptString("post", null);	
		
	
		
		
		if( mServer != null ){
			printErr("Server already running");
			return 1;
		}
		
		

		
		
		mServer = HttpServer.create(new InetSocketAddress(port), 10 );
		mServer.createContext(context, new MyHandler(getEnv().getShell(),get,put,post));
		mServer.setExecutor(null); // creates a default executor

		mServer.start();
		return 0;
	}


	private void usage() {
		printErr("usage: httpserver [options] start|stop");
		
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
