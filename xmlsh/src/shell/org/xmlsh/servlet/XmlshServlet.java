package org.xmlsh.servlet;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xmlsh.core.CommandFactory;
import org.xmlsh.core.ICommand;
import org.xmlsh.core.StreamOutputPort;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.NullOutputStream;
import org.xmlsh.util.Util;

/**
 * $Id: $
 * $Date: $
 *
 */

public class XmlshServlet extends HttpServlet {
	
	private	 String mRoot = null ;

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException {
	
			OutputStream out = null ;
			
	 
			try {
				// String command = request.getParameter("command");
			       String cpath  = request.getContextPath(); // "/odd_store
		            String spath  = request.getServletPath();
		            String ruri  = request.getRequestURI();
		            String qstring  = request.getQueryString();

		        String path = spath.substring(1);
		     	List<XValue> vargs = new ArrayList<XValue>();
		 		
		 		
		 		Shell shell = new Shell(false);
				shell.setCurdir( new File(mRoot));
			 			 	
				ICommand	script = CommandFactory.getInstance().getScript( shell , path , true );
				if( script != null ){
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					shell.getEnv().setStdout( new StreamOutputPort(bos ,false) );
					shell.getEnv().setStderr( new StreamOutputPort(new NullOutputStream(),false) );
					
					int ret = script.run(shell, path , vargs);

					String ct = shell.getSerializeOpts().getContent_type() + "; " + shell.getSerializeOpts().getEncoding();
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
 
		try {
			// String command = request.getParameter("command");
		       String cpath  = request.getContextPath(); // "/odd_store
	            String spath  = request.getServletPath();
	            String ruri  = request.getRequestURI();
	            String qstring  = request.getQueryString();

	        String path = spath.substring(1);
	     	List<XValue> vargs = new ArrayList<XValue>();
	 		
	     	
	     	
	 		Shell shell = new Shell(false);
			shell.setCurdir( new File(mRoot));
		 	
			Enumeration names = request.getParameterNames();
			while( names.hasMoreElements() ){
				String name = (String) names.nextElement();
				String value = (String) request.getParameter(name);
				shell.getEnv().setVar(name, value);
			}
			
			
			
			ICommand	script = CommandFactory.getInstance().getScript( shell , path , true );
			if( script != null ){
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				shell.getEnv().setStdout( new StreamOutputPort(bos ,false) );
				shell.getEnv().setStderr( new StreamOutputPort(new NullOutputStream(),false) );
				
				InputStream is = readInput( request.getInputStream());
				shell.getEnv().setStdin(is );
				
				
				int ret = script.run(shell, path , vargs);
			
				String ct = shell.getSerializeOpts().getContent_type() + "; " + shell.getSerializeOpts().getEncoding();
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

		}
		
	}

	private InputStream readInput(ServletInputStream inputStream) throws IOException {
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		Util.copyStream( inputStream , bos );
		return new ByteArrayInputStream( bos.toByteArray() );
		
		
		
	}

	/* (non-Javadoc)
	 * @see javax.servlet.GenericServlet#destroy()
	 */
	@Override
	public void destroy() {
		Shell.uninitialize();
		super.destroy();
		
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
