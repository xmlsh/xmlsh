/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.core;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmValue;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import org.xml.sax.InputSource;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.AutoReleasePool;
import org.xmlsh.util.IManagedObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Stack;

import javax.xml.transform.Source;

public class XEnvironment  {
	
	@SuppressWarnings("unused")
	private 	static Logger mLogger = LogManager.getLogger( XEnvironment.class );
	private 	Shell mShell;
	private		XIOEnvironment mIO = new XIOEnvironment();
	private		Variables	mVars;
	private		Namespaces	mNamespaces = null;
	private		AutoReleasePool  mAutoRelease = null;

	private		Stack<XIOEnvironment>  mSavedIO;
	
	


	
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

	private void	addAutoRelease( IManagedObject obj )
	{
		if( mAutoRelease == null )
			mAutoRelease = new AutoReleasePool();
		mAutoRelease.add(obj);
	}

	/*
	 * Standard Varibles 
	 */
	public XVariable	getVar( String name )
	{
		
		return mVars.get(name);
	}
	
	public void	setVar( XVariable var, boolean local )
	{
		/*
		 * Special variables
		 */
		String name = var.getName();
		if( name.startsWith("xmlns:")){
			
			declareNamespace( name.substring(6),var.getValue().toString());
		} else
			mVars.put(name , var, local );
	}
	


	public void	setVar( String name , XValue value, boolean local ) throws InvalidArgumentException 
	{

		
		XVariable var = mVars.get(name);
		if( var == null )
			var = new XVariable( name , value );
		else
			var = var.clone();
		
		var.setValue(value);
		
		
		setVar( var , local );
	}
	
	
	/*
	 * Append to a variable as a sequence 
	 */
	public void appendVar(String name, XValue value, boolean local ) throws InvalidArgumentException {
		

		XVariable var = mVars.get(name);
		if( var == null ){
			// If no existing variable then dont touch
			setVar(new XVariable( name , value ) , local );
			return ;
		}
		
		
		var = var.clone();
		XdmValue xvalue = value.asXdmValue();
		if( xvalue == null )
			return ;
		
		var.setValue(  new XValue(var.getValue().asXdmValue().append(xvalue)));
		setVar( var , local);
		
	}
		
	

	public void setVar(String name, String value , boolean local ) throws InvalidArgumentException {
		setVar( name , new XValue(value),local);
		
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


	public void close() throws CoreException {
		if( this.mSavedIO != null && ! mSavedIO.isEmpty())
		   throw new CoreException("FIXME");
		
		mIO.release();
		if( mAutoRelease != null ){
			mAutoRelease.close();
			mAutoRelease = null;
		}
	}
	
	public Shell getShell() { 
		return mShell;
	}

	public Variables getVars() { return mVars ; }
	

	public Collection<String> getVarNames() {
		return mVars.getVarNames();
	}
	
	public String getVarString( String key )
	{
		XVariable var = getVar(key);
		if( var== null )
			return null ;

		return var.getValue().toString();
	}

	/*
	 * Save the environment by cloning it and pushing it to this
	 * and return the OLD environment
	 */
	public void saveIO() throws CoreException
	{
		if( mSavedIO == null )
			mSavedIO = new Stack<XIOEnvironment>();
			
		mSavedIO.push(mIO);
		mIO = new XIOEnvironment(mIO);

	}
	
	public void restoreIO()
	{
		mIO.release();
		mIO = mSavedIO.pop();

		
	}

	

	


	/**
	 * @param file
	 * @param append
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @see org.xmlsh.sh.shell.Shell#getOutputStream(java.lang.String, boolean)
	 */
	public OutputStream getOutputStream(String file, boolean append, SerializeOpts opts ) throws FileNotFoundException,
			IOException {
		return mShell.getOutputStream(file, append, opts );
	}

	public OutputStream getOutputStream(File file, boolean append) throws FileNotFoundException {
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


	public void unsetVar(String name ) throws InvalidArgumentException {
		mVars.unset( name );
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
	public InputPort getStdin()  {
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
	
	public OutputPort getOutput( XValue port, boolean append ) throws IOException
	{
		
		
		if( port == null )
			return getStdout();
		if( port.isAtomic()){
			String name = port.toString().trim();
			if( name.equals("-"))
				return getStdout();
			
			return mShell.getOutputPort(name, append);
			
			
			
		}
		else
		{
			OutputPort p = new VariableOutputPort(  new XVariable(null,port) );
			return p;
		}
		
		
	}
	
	public OutputPort getOutput( String port , boolean append) throws IOException
	{
		return getOutput( new XValue(port) , append );
	}

	
	public OutputPort getOutput( File file , boolean append ) throws IOException
	{
		
		return new FileOutputPort( file , append);
			
			
		
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
	public void setStderr(OutputPort stderr) throws CoreException {
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
	public void declareNamespace(String prefix, String uri) {
		if( mNamespaces == null )
			mNamespaces = new Namespaces();
		mNamespaces.declare(prefix, uri);
	}

	
	public Namespaces getNamespaces()
	{
		return mNamespaces;
	}


	public InputStream getInputStream(XValue file,SerializeOpts opts) throws CoreException{
		return getInput(file).asInputStream(opts);
	}



	public Source getSource(XValue value,SerializeOpts opts) throws CoreException {
		return getInput(value).asSource(opts);

	}


	

	/*
	 * Get an input by name or value
	 * 
	 * If port is null return stdin
	 * If port is a string 
	 * If port is a string 
	 * 	  if equals to "-" return stdin
	 *    if looks like "scheme://path" return a port based on an input stream from UI
	 *    if looks like "name" return a port based on an input stream by filename
	 * if port is a node return an anonymous port based on a value
	 * 
	 */
	public InputPort getInput(XValue port) throws CoreException  {
		
		if( port == null )
			return getStdin();
		if( port.isAtomic()){
			String name = port.toString().trim();
			if( name.equals("-"))
				return getStdin();


			
			
			InputPort p;
			try {
				p = mShell.getInputPort(name);
			} catch (IOException e) {
				throw new CoreException(e);
			}
			// Port is not managed, add to autorelease
			addAutoRelease( p );
			
			return p;
			
			
		}
		else
		{
			InputPort p = new VariableInputPort(  new XVariable(null,port) );
			// Port is not managed, add to autorelease
			addAutoRelease(p);
			return p;
		}
		
	}
	
	/*
	 * Get an input port explicitly by its name 
	 */
	public InputPort getInputPort(String name)
	{
		return mIO.getInputPort(name);
	}

	public InputPort getInput( String name ) throws CoreException
	{
		return getInput( new XValue(name));
	}
	
	public OutputPort getOutputPort(String name){
		return mIO.getOutputPort(name);
	}

	
	public String getAbsoluteURI(String sysid) throws URISyntaxException 
	{
		URI uri = new URI(sysid);
		if( uri.isAbsolute())
			return sysid ;
		
		URI absolute = getBaseURI().resolve(sysid );
		return absolute.toString();
		
		
	}


	public URI getBaseURI() {
		return getCurdir().toURI();
	}

	public PortList<InputPort>	getInputPorts()
	{
		return mIO.getInputPorts();
	}
	public PortList<OutputPort>	getOutputPorts()
	{
		return mIO.getOutputPorts();
	}


	public OutputPort getOutputPort(String portname, boolean append) {
		// TODO: Add append mode to output ports 
		return mIO.getOutputPort(portname);
	}


	public InputSource getInputSource(XValue value, SerializeOpts opts) throws CoreException, FileNotFoundException, IOException {
		InputPort in = getInput(value);
		return in.asInputSource(opts);
		
	}


	public void tie(Shell shell, String varname, String expr) throws InvalidArgumentException, SaxonApiException {
		
		XVariable var = mVars.get(varname);
		if( var == null )
			throw new InvalidArgumentException("Unknown variable: " + varname);
		
		var.tie(shell,expr);
	
	}


	public boolean isDefined(String name) {
		return mVars.containsKey(name);
		
	}


	public Variables pushLocalVars() {
		Variables current = mVars ;
		mVars = mVars.pushLocals();
		return current ;
		
	}


	public void popLocalVars(Variables vars) {
		mVars = vars ;
		
	}


	/**
	 * @return the savedIO
	 */
	public XIOEnvironment getSavedIO() {
		return mSavedIO.peek();
	}

    

}
//
//
//Copyright (C) 2008-2014    David A. Lee.
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
