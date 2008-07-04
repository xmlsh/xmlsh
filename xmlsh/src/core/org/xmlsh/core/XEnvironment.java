/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.core;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.SynchronizedInputStream;
import org.xmlsh.util.SynchronizedOutputStream;

public class XEnvironment  {
	
	private static Logger mLogger = LogManager.getLogger( XEnvironment.class );
	private Shell mShell;
	
	
	/*
	 * Standard IO
	 */
	
	private SynchronizedInputStream	 mStdin;
	private	 boolean				 mStdinRedirected = false;
	private SynchronizedOutputStream mStdout;
	private SynchronizedOutputStream mStderr;
	
	
	/*
	 * Standard Variables
	 *  May contain any type
	 */
	
	private	  HashMap<String,XVariable>	mVars;
	


	
	/* (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable {
		close();
	}


	public XEnvironment(Shell shell)
	{
		mShell = shell;
		mVars = new HashMap<String,XVariable>();
		getStdin();
		getStdout();
		getStderr();

	}
	
	
	/*
	 * Standard input stream - created on first request
	 */
	
	public	InputStream getStdin() 
	{
		if( mStdin == null )
			mStdin = new SynchronizedInputStream(System.in);
		return mStdin;
	}
	
	/*
	 * Stdandard output stream - created on first request
	 */
	public	OutputStream	getStdout() 
	{
		if( mStdout == null )
			mStdout =  new SynchronizedOutputStream(System.out);
		return mStdout ;
	}
	
	/*
	 * Standard error stream - created on first request
	 */
	public	OutputStream	getStderr() 
	{
		if( mStderr == null )
			mStderr = new SynchronizedOutputStream(System.err);
		return mStderr ;
	}

	/**
	 * @param stdin the stdin to set
	 */
	public void setStdin(InputStream stdin) {
		mStdinRedirected = true ;
		mStdin = new SynchronizedInputStream(stdin);
	}

	/**
	 * @param stdout the stdout to set
	 */
	public void setStdout(OutputStream stdout) {
		mStdout = new SynchronizedOutputStream(stdout);
	}

	/**
	 * @param stderr the stderr to set
	 */
	public void setStderr(OutputStream stderr) {
		mStderr = new SynchronizedOutputStream(stderr);
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
		mVars.put(var.getName(), var);
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
	
	
	
	
	public XEnvironment clone()
	{
		return clone( mShell );
	}
	/*
	 * Clone an environment for use in a new thread
	 * 
	 * @see java.lang.Object#clone()
	 */
	
	public XEnvironment clone(Shell shell)
	{
		XEnvironment 	that = new XEnvironment(shell);

		that.mVars.putAll( this.mVars );	// clone variables
		
		
		// Copy streams, assume they are thread safe streams
		that.mStderr = this.mStderr;
		if( that.mStderr != null )
			that.mStderr.addRef();
		
		
		that.mStdin  = this.mStdin;
		if( that.mStdin != null ){
			that.mStdin.addRef();
			that.mStdinRedirected = this.mStdinRedirected;
		}
		
		
		that.mStdout = this.mStdout;
		if( that.mStdout != null )
			that.mStdout.addRef();
		
		return that;
	}


	public void close() {
		try {
			if( this.mStdout != null)
				this.mStdout.close();
			
			if( this.mStderr != null )
				this.mStderr.close();
			
			if( this.mStdin != null )
				this.mStdin.close();
			
			this.mStderr = null;
			this.mStdout = null;
			this.mStdin = null;
			
		} catch (IOException e) {
			mLogger.error("Exception closing environment",e);
		}
	}
	
	public Shell getShell() { 
		return mShell;
	}


	public Collection<String> getVarNames() {
		return mVars.keySet();
	}


	/**
	 * @param fname
	 * @return
	 * @throws IOException
	 * @see org.xmlsh.sh.shell.Shell#getFile(java.lang.String)
	 */
	public File getFile(String fname) throws IOException {
		return mShell.getFile(fname);
	}


	/**
	 * @param fvalue
	 * @return
	 * @throws IOException
	 * @see org.xmlsh.sh.shell.Shell#getFile(org.xmlsh.core.XValue)
	 */
	public File getFile(XValue fvalue) throws IOException {
		return mShell.getFile(fvalue);
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
	
	public boolean isStdinRedirected() { return mStdinRedirected ; }

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
