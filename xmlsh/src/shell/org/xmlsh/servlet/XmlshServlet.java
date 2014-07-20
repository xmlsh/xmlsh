package org.xmlsh.servlet;
import net.sf.saxon.s9api.SaxonApiException;
import org.xmlsh.core.CommandFactory;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.ICommand;
import org.xmlsh.core.StreamOutputPort;
import org.xmlsh.core.VariableOutputPort;
import org.xmlsh.core.XEnvironment;
import org.xmlsh.core.XValue;
import org.xmlsh.core.XVariable;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.NullInputStream;
import org.xmlsh.util.NullOutputStream;
import org.xmlsh.util.Util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * $Id: $
 * $Date: $
 *
 */

@SuppressWarnings("serial")
public class XmlshServlet extends HttpServlet {
	
	
	private	 String mRoot = null ;

	/* (non-Javadoc)
	 * @see javax.servlet.GenericServlet#destroy()
	 */
	@Override
	public void destroy() {
		Shell.uninitialize();
		super.destroy();
		
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException {
	
			OutputStream out = null ;
			
			Shell shell = null ;
			try {
				// String command = request.getParameter("command");
			    //    String cpath  = request.getContextPath(); // "/odd_store
		            String spath  = request.getServletPath();
		        //    String ruri  = request.getRequestURI();
		        //    String qstring  = request.getQueryString();

		        String path = spath.substring(1);
		        if(Util.isBlank(path) || path.equals("/") )
		        	path = "index.xsh";
		        		
		        XVariable xp 		= parseParams( request );
		        XVariable headers 	= parseHeaders( request );
		        
		        
		     	List<XValue> vargs = new ArrayList<XValue>();
		     	
		 		shell = new Shell(false);
				shell.setCurdir( new File(mRoot));
			 			 	
				ICommand	script = CommandFactory.getInstance().getScript( shell , path , true , null );
				if( script != null ){
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					XEnvironment env = shell.getEnv();
					
					env.setStdout( new StreamOutputPort(bos ,false) );
					env.setStderr( new StreamOutputPort(new NullOutputStream(),false) );
					env.setStdin( new NullInputStream() );
					
					// Set properties
					if( xp != null )
						env.setVar(xp,false);
					if( headers != null )
						env.setVar(headers,false);
	
					
					ManagedHttpSession mhs = new ManagedHttpSession( request.getSession());
					shell.getSession().setVar("HTTP_SESSION", mhs);
					mhs.release();
					
					@SuppressWarnings("unused")
					int ret = script.run(shell, path , vargs);
					
					

					String ct = shell.getSerializeOpts().getContent_type() + "; " + shell.getSerializeOpts().getOutputXmlEncoding();
					response.setContentType(ct);

					
					OutputStream os = response.getOutputStream();
					Util.copyStream( new ByteArrayInputStream(bos.toByteArray()), os);
					
					
				}
				

			} 
			catch( Exception e )
			{
				throw new ServletException(e);
			}
			
			finally {
				if( shell != null )
					Util.safeClose(shell);

			}
	
	}
	
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
	    

		OutputStream out = null ;
		InputStream in = null ;

		Shell shell = null ;
		try {
			// String command = request.getParameter("command");
		     // String cpath  = request.getContextPath(); // "/odd_store
	            String spath  = request.getServletPath();
	        //    String ruri  = request.getRequestURI();
	        //    String qstring  = request.getQueryString();

	        
	        String path = spath.substring(1);
	        if(Util.isBlank(path) || path.equals("/") )
	        	path = "index.xsh";
	        		
	        XVariable xp 		= parseParams( request );
	        XVariable headers 	= parseHeaders( request );
	        
	        
	        
	        
	        
	     	List<XValue> vargs = new ArrayList<XValue>();
	 		
	     	
	     	
	 		shell = new Shell(false);
			shell.setCurdir( new File(mRoot));
		 	
			Enumeration<?> names = request.getParameterNames();
			XEnvironment env = shell.getEnv();
			while( names.hasMoreElements() ){
				String name = (String) names.nextElement();
				String value = (String) request.getParameter(name);
				env.setVar(name, value,false);
			}
			
			
			
			ICommand	script = CommandFactory.getInstance().getScript( shell , path , true , null );
			if( script != null ){
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				env.setStdout( new StreamOutputPort(bos ,false) );
				env.setStderr( new StreamOutputPort(new NullOutputStream(),false) );
				
				InputStream is = readInput( request.getInputStream());
				env.setStdin(is );
				
				
				
				// Set properties
				if( xp != null )
					env.setVar(xp,false);
				if( headers != null )
					env.setVar(headers,false);
				
				
				ManagedHttpSession mhs = new ManagedHttpSession( request.getSession());
				shell.getSession().setVar("HTTP_SESSION", mhs);
				mhs.release();
				
				@SuppressWarnings("unused")
				int ret = script.run(shell, path , vargs);
			
				String ct = shell.getSerializeOpts().getContent_type() + "; " + shell.getSerializeOpts().getOutputXmlEncoding();
				response.setContentType(ct);

				
				OutputStream os = response.getOutputStream();
				Util.copyStream( new ByteArrayInputStream(bos.toByteArray()), os);

			
			
			}
			

		} 
		catch( Exception e )
		{
			throw new ServletException(e);
		}
		
		finally {
			if( shell != null )
				shell.close();
		}
		
	}
	
	

	/* (non-Javadoc)
	 * @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
	 */
	@Override
	public void init(ServletConfig config) throws ServletException {
		// TODO Auto-generated method stub
		super.init(config);
		
		mRoot = config.getInitParameter("root");
	
		// Pre-initialize shell so logging can work before executing first task
		Shell.initialize();
		
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
	private XVariable parseHeaders(HttpServletRequest request) throws XMLStreamException, CoreException, SaxonApiException {

        
		XVariable var = new XVariable("HTTP_HEADERS",null);
		VariableOutputPort port = new VariableOutputPort( var );
		XMLStreamWriter writer = port.asXMLStreamWriter(null);
		
		
		writer.writeStartDocument();
		writer.writeStartElement("headers");
		
		
		Enumeration names = request.getHeaderNames();
		if( names != null ){
			while( names.hasMoreElements() ){
				String name = (String) names.nextElement();
				Enumeration values = request.getHeaders(name);
				
				
				writer.writeStartElement("header");
				writer.writeAttribute("name", name );
				
				while( values.hasMoreElements() ){
					String value = (String) values.nextElement();
					writer.writeStartElement("value");
					writer.writeCharacters(value);
					writer.writeEndElement();
				}
				writer.writeEndElement();
			}
		}
		writer.writeEndElement();
		writer.writeEndDocument();
		writer.close();
		port.flush();
		return var ;
	
	
	
	}

	/*
	 * Create a document from a paramater map
	 * Map is  String: String[]
	 * 
	 * Format is
	 * 
	 * <parameters>
	 * 	<param name="key">
	 * 		<value>value1</value>
	 * 		<value>value2</value>
	 * 	....
	 * </parameters>
	 * 
	 * 
	 */
	@SuppressWarnings("unchecked")
	private XVariable parseParams(HttpServletRequest request) throws XMLStreamException, CoreException, SaxonApiException {


        Map params = request.getParameterMap();
        
		XVariable var = new XVariable("HTTP_PARAMETERS",null);
		VariableOutputPort port = new VariableOutputPort( var );
		XMLStreamWriter writer = port.asXMLStreamWriter(null);
		
		
		writer.writeStartDocument();
		writer.writeStartElement("parameters");
		
		if( params != null ){
			Iterator<Entry<String,String[]>> iter = params.entrySet().iterator();
			while( iter.hasNext() ){
				Entry<String,String[]> e = iter.next();
				writer.writeStartElement("param");
				writer.writeAttribute("name", e.getKey() );
				for( String v : e.getValue() ){
					writer.writeStartElement("value");
					writer.writeCharacters(v);
					writer.writeEndElement();
				}
				writer.writeEndElement();
			}
		}
		writer.writeEndElement();
		writer.writeEndDocument();
		writer.close();
		port.flush();
		return var ;
	
	
	
	}

	private InputStream readInput(ServletInputStream inputStream) throws IOException {
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		Util.copyStream( inputStream , bos );
		return new ByteArrayInputStream( bos.toByteArray() );
		
		
		
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
