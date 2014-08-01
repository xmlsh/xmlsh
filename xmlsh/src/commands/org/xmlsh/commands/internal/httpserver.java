/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.commands.internal;

import net.sf.saxon.s9api.SaxonApiException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import org.xmlsh.core.CoreException;
import org.xmlsh.core.Options;
import org.xmlsh.core.ThrowException;
import org.xmlsh.core.UnknownOption;
import org.xmlsh.core.VariableOutputPort;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.core.XVariable;
import org.xmlsh.sh.core.Command;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.Util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

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
		private		boolean 	mChunked = false ;

		MyHandler( Shell shell, boolean chunked , String getFunc, String putFunc , String postFunc  ) throws CoreException 
		{
			mShell = shell.clone();	// clone shell for execution
			mChunked = chunked;

			// need to save the CD so we can restore it in the new thread
			mInitialCD = mShell.getCurdir();

			if(  getFunc != null )
				mGet =  mShell.parseEval(getFunc);
			if( putFunc != null )
				mPut =  mShell.parseEval(putFunc);
			if( postFunc != null )
				mPost = mShell.parseEval(postFunc);


		}

		@Override
		public void handle(HttpExchange http) throws IOException {


			try {

				XVariable headers 	= parseHeaders( http );

				mShell.setCurdir(mInitialCD);
				//  System.out.println("Got request in thread: " + Thread.currentThread().getName() );


				String method = http.getRequestMethod();
				// TBD: Headers headers = http.getRequestHeaders();


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


				URI uri = http.getRequestURI();

				InputStream is = http.getRequestBody();


				// If chunked mode we can execute directly to the output
				if( mChunked ){

					http.sendResponseHeaders(200, 0);

					// http.sendResponseHeaders(200, response.length());
					OutputStream os = http.getResponseBody();



					execute(cmd, uri, is, os, headers );
					;


				}

				// Otherwise must buffer up the response
				else {
					ByteArrayOutputStream bos = new ByteArrayOutputStream();


					execute(cmd, uri, is, bos,headers);


					try {
						// Eat the data
						while( is.read() > 0 )
							;

					} catch( Exception e )
					{

					}

					http.getResponseHeaders().add("Connection", "close");

					http.sendResponseHeaders(200, bos.size() );


					// http.sendResponseHeaders(200, response.length());
					OutputStream os = http.getResponseBody();
					os.write(bos.toByteArray());

					os.flush();
					os.close();


				}



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

		private void execute(Command cmd, URI uri, InputStream is, OutputStream os, XVariable headers)
				throws CoreException, ThrowException, IOException {

			mShell.getEnv().setStdin( is );
			mShell.getEnv().setStdout( os  );



			List<XValue> args = new ArrayList<XValue>();
			String query = uri.getQuery();
			String path = uri.getPath();
			args.add( new XValue(path));
			args.add( new XValue(query));

			if( headers != null )
				mShell.getEnv().setVar(headers,false);

			mShell.setArgs(args);
			mShell.exec(cmd);
		}

		/*
		 * Create a document from a paramater map
		 * Map is  String: String[]
		 * 
		 * Format is
		 * 
		 * <headers>
		 * 	<header key="key">
		 * 		<value>value1</value>
		 * 		<value>value2</value>
		 * 	....
		 * </parameters>
		 * 
		 * 
		 */
		@SuppressWarnings("unchecked")
		private XVariable parseHeaders(HttpExchange request) throws IOException  {


			XVariable var = new XVariable("HTTP_HEADERS",null);
			XMLStreamWriter writer = null ;
			try (
					VariableOutputPort port = new VariableOutputPort( var );
					) {
				writer = port.asXMLStreamWriter(null);
				writer.writeStartDocument();
				writer.writeStartElement("headers");

				Headers headers = request.getRequestHeaders();
				Set<String> names = headers.keySet();

				for( String name : names ){

					List<String> values = headers.get(name);


					writer.writeStartElement("header");
					writer.writeAttribute("name", name );

					for( String value : values ){
						writer.writeStartElement("value");
						writer.writeCharacters(value);
						writer.writeEndElement();
					}
					writer.writeEndElement();

				}
				writer.writeEndElement();
				writer.writeEndDocument();
				writer.close();
				return var ;
			} catch (XMLStreamException|SaxonApiException e) {
				throw new IOException(e);
			} finally {
				Util.safeClose( writer );
			}
		}
	}

	@Override
	public int run( List<XValue> args ) throws UnknownOption, IOException, CoreException
	{

		Options opts = new Options( "port:,context:,handler:,get:,put:,post:,chunk" );
		opts.parse(args);

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
		boolean bChunk = opts.hasOpt("chunk");




		if( mServer != null ){
			printErr("Server already running");
			return 1;
		}





		mServer = HttpServer.create(new InetSocketAddress(port), 10 );
		mServer.createContext(context, new MyHandler(getEnv().getShell(),bChunk, get,put,post));
		mServer.setExecutor(null); // creates a default executor

		mServer.start();
		return 0;
	}

}


//
//
//Copyright (C) 2008-2014 David A. Lee.
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
