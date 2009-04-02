/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.s9api.SaxonApiException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.sh.shell.Shell;

public class XEnvironment  {
	
	@SuppressWarnings("unused")
	private 	static Logger mLogger = LogManager.getLogger( XEnvironment.class );
	private 	Shell mShell;
	private		XIOEnvironment mIO = new XIOEnvironment();
	private		Variables	mVars;
	private		Namespaces	mNamespaces = null;


	
	/* (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable {
		close();
	}


	public XEnvironment(Shell shell, boolean bInitIO ) throws IOException
	{
		mShell = shell;
		mVars = new Variables();
	
		if( bInitIO )
			mIO.initStdio();

	}



	/*
	 * Standard Varibles 
	 */
	public XVariable	getVar( String name )
	{
		
		return mVars.get(name);
	}
	
	public void	setVar( XVariable var)
	{
		/*
		 * Special variables
		 */
		String name = var.getName();
		if( name.startsWith("xmlns:")){
			
			declareNamespace( name.substring(6),var.getValue().toString());
		} else
			mVars.put(name , var);
	}
	


	public void	setVar( String name , XValue value)
	{
		/* DO not do this until we have a copy-on-write deep cloning
		 * of the environment
		 * 
		 */
		/*
		XVariable var = mVars.get(name);
		if( var == null )
			setVar( new XVariable( name , value ));
		else
			var.setValue(value);
			*/
		
		setVar( new XVariable( name , value ));
	}
	


	public void setVar(String name, String value) {
		setVar( name , new XValue(value));
		
	}
	
	
	public XEnvironment clone()
	{
		
		// TODO When cloning, only export marked for export vars
		// Add typeset command
		
		
		
		
		try {
			return clone( mShell );
		} catch (IOException e) {
			mShell.printErr("Exception cloning shell", e);
			return null;
		}
	}
	/*
	 * Clone an environment for use in a new thread
	 * 
	 * @see java.lang.Object#clone()
	 */
	
	public XEnvironment clone(Shell shell) throws IOException
	{
		XEnvironment 	that = new XEnvironment(shell, false);
		that.mVars		= new Variables(this.mVars);

		that.mIO = new XIOEnvironment(this.mIO);
		
		if( this.mNamespaces != null )	
			that.mNamespaces = new Namespaces( this.mNamespaces );
		
		return that;
	}


	public void close() {
		mIO.release();
	}
	
	public Shell getShell() { 
		return mShell;
	}

	public Variables getVars() { return mVars ; }
	

	public Collection<String> getVarNames() {
		return mVars.keySet();
	}

	/*
	 * Save the environment by cloning it and pushing it to this
	 * and return the OLD environment
	 */
	public XIOEnvironment saveIO()
	{
		XIOEnvironment io = mIO;
		mIO = new XIOEnvironment(io);
		return io;
	}
	
	public void restoreIO( XIOEnvironment io )
	{
		mIO.release();
		mIO = io;
		
	}

	

	

	/**
	 * @param file
	 * @return
	 * @throws IOException
	 * @throws URISyntaxException
	 * @see org.xmlsh.sh.shell.Shell#getURI(java.lang.String)
	 */
	public URI getURI(String file) throws IOException, URISyntaxException {
		return mShell.getURI(file);
	}


	/**
	 * @param file
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @see org.xmlsh.sh.shell.Shell#getInputStream(java.lang.String)
	 */
	public InputStream getInputStream(String file) throws FileNotFoundException, IOException {
		return mShell.getInputStream(file);
	}


	/**
	 * @param file
	 * @param append
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @see org.xmlsh.sh.shell.Shell#getOutputStream(java.lang.String, boolean)
	 */
	public OutputStream getOutputStream(String file, boolean append) throws FileNotFoundException,
			IOException {
		return mShell.getOutputStream(file, append);
	}


	/**
	 * @param s
	 * @param e
	 * @see org.xmlsh.sh.shell.Shell#printErr(java.lang.String, java.lang.Exception)
	 */
	public void printErr(String s, Exception e) {
		mShell.printErr(s, e);
	}


	/**
	 * @param s
	 * @see org.xmlsh.sh.shell.Shell#printErr(java.lang.String)
	 */
	public void printErr(String s) {
		mShell.printErr(s);
	}


	/**
	 * @return
	 * @see org.xmlsh.sh.shell.Shell#getCurdir()
	 */
	public File getCurdir() {
		return mShell.getCurdir();
	}


	/**
	 * @param cd
	 * @throws IOException 
	 * @see org.xmlsh.sh.shell.Shell#setCurdir(java.io.File)
	 */
	public void setCurdir(File cd) throws IOException {
		mShell.setCurdir(cd);
	}


	public XValue getVarValue(String name) {
		XVariable var = getVar(name);
		if( var == null )
			return null ;
		else
			return var.getValue();
	}


	public void unsetVar(String name ) {
		mVars.remove( name );
	}
	
	public boolean isStdinRedirected() { return mIO.isStdinRedirected() ; }


	/**
	 * @return
	 * @throws IOException 
	 * @see org.xmlsh.core.XIOEnvironment#getStderr()
	 */
	public OutputPort getStderr() throws IOException {
		return mIO.getStderr();
	}


	/**
	 * @return
	 * @throws IOException 
	 * @see org.xmlsh.core.XIOEnvironment#getStdin()
	 */
	public InputPort getStdin() throws IOException {
		return mIO.getStdin();
	}


	/**
	 * @return
	 * @throws IOException 
	 * @see org.xmlsh.core.XIOEnvironment#getStdout()
	 */
	public OutputPort getStdout() throws IOException {
		return mIO.getStdout();
	}


	/**
	 * @param stderr
	 * @throws IOException
	 * @throws InvalidArgumentException 
	 * @see org.xmlsh.core.XIOEnvironment#setStderr(java.io.OutputStream)
	 */
	public void setStderr(OutputStream stderr) throws CoreException {
		mIO.setStderr(stderr);
	}


	/**
	 * @param stdin
	 * @throws IOException
	 * @see org.xmlsh.core.XIOEnvironment#setStdin(java.io.InputStream)
	 */
	
	public void setStdin(InputStream in) throws CoreException {
		setInput(null, in );
	}

	public void setStdin(XVariable variable) throws CoreException {
		
		setInput( null , variable );
	}
	public void setStdin(InputPort in ) throws CoreException {
		
		setInput( null , in );
	}
	
	public InputPort setInput(String name,InputStream in) throws CoreException {
		return mIO.setInput( name,new StreamInputPort(in,null));
	}

	public InputPort setInput(String name, XVariable variable) throws CoreException {
		
		return mIO.setInput( name,new VariableInputPort(variable));
	}
	
	public InputPort setInput( String name , InputPort in ) throws CoreException {
		
		return mIO.setInput( name  , in );
	}


	public void setStdout(OutputStream out) throws CoreException {
		setOutput( null ,  new StreamOutputPort(out));
		
	}
	public void setStdout(OutputPort  port) throws CoreException {
		setOutput( null , port );
	}

	public void setStdout(XVariable xvar) throws CoreException {
		setOutput( null ,  new VariableOutputPort(xvar));
	}

	public void setOutput(String name ,OutputStream out) throws CoreException {
		setOutput( name,new StreamOutputPort(out));
		
	}


	public void setOutput(String name ,XVariable xvar) throws CoreException {
		setOutput( name,new VariableOutputPort(xvar));
	}
	
	public void setOutput(String name , OutputPort out) throws CoreException {
		mIO.setOutput( name, out );
	}
	

	public void declareNamespace(String ns ) {
		if( mNamespaces == null )
			mNamespaces = new Namespaces();
		
		mNamespaces.declare( ns );
		
	}
	private void declareNamespace(String prefix, String uri) {
		if( mNamespaces == null )
			mNamespaces = new Namespaces();
		mNamespaces.declare(prefix, uri);
	}

	
	public Namespaces getNamespaces()
	{
		return mNamespaces;
	}


	public InputStream getInputStream(XValue file,SerializeOpts opts) throws InvalidArgumentException, SaxonApiException, IOException {
		return getInput(file).asInputStream(opts);
	}



	public Source getSource(XValue value,SerializeOpts opts) throws InvalidArgumentException, SaxonApiException, IOException {
		return getInput(value).asSource(opts);

	}


	

	/*
	 * Get an input by name or value
	 * 
	 * If port is null return stdin
	 * If port is a string 
	 * If port is a string 
	 * 	  if equals to "-" return stdin
	 *    if looks like "(name)" return port named "name"
	 *    if looks like "scheme://path" return a port based on an input stream from UI
	 *    if looks like "name" return a port based on an input stream by filename
	 *	  if looks like "{name}" returns a port based on a env variable
	 * if port is a node return an anonymous port based on a value
	 * 
	 */
	public InputPort getInput(XValue port) throws IOException {
		
		if( port == null )
			return getStdin();
		if( port.isString()){
			String name = port.toString().trim();
			if( name.equals("-"))
				return getStdin();
			else
			if( name.startsWith("(") && name.endsWith(")") )
				return mIO.getInputPort( name.substring(1,name.length()-1));
			else
			if( name.startsWith("{") && name.endsWith("}") ){
				String varname = name.substring(1,name.length()-1);
				return new VariableInputPort( getVar(varname));
			}
				
			// Get a stream from name
			InputStream in = getInputStream(name);
			if( in == null )
				return null ;
			
			InputPort p = new StreamInputPort( in,name );
			return p;
			
			
		}
		else
		{
			InputPort p = new VariableInputPort(  new XVariable(null,port) );
			return p;
		}
		
	}



}
//
//
//Copyright (C) 2008, David A. Lee.
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
