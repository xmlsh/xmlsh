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

public class XEnvironment  {
	
	@SuppressWarnings("unused")
	private static Logger mLogger = LogManager.getLogger( XEnvironment.class );
	private Shell mShell;
	
	private	XIOEnvironment mIO = new XIOEnvironment();
	
	private	  HashMap<String,XVariable>	mVars;
	


	
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
		mVars = new HashMap<String,XVariable>();
	
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
	


	public void setVar(String name, String value) {
		setVar( name , new XValue(value));
		
	}
	
	
	public XEnvironment clone()
	{
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

		that.mVars.putAll( this.mVars );	// clone variables
		that.mIO = mIO.clone();
		
		return that;
	}


	public void close() {
		mIO.release();
	}
	
	public Shell getShell() { 
		return mShell;
	}


	public Collection<String> getVarNames() {
		return mVars.keySet();
	}

	public XIOEnvironment saveIO()
	{
		XIOEnvironment io = mIO;
		mIO = mIO.clone();
		return io;
	}
	
	public void restoreIO( XIOEnvironment io )
	{
		mIO.release();
		mIO = io;
		
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
	 * @see org.xmlsh.core.XIOEnvironment#setStderr(java.io.OutputStream)
	 */
	public void setStderr(OutputStream stderr) throws IOException {
		mIO.setStderr(stderr);
	}


	/**
	 * @param stdin
	 * @throws IOException
	 * @see org.xmlsh.core.XIOEnvironment#setStdin(java.io.InputStream)
	 */
	public void setStdin(InputStream stdin) throws IOException {
		mIO.setStdin(stdin);
	}


	/**
	 * @param stdout
	 * @throws IOException
	 * @see org.xmlsh.core.XIOEnvironment#setStdout(java.io.OutputStream)
	 */
	public void setStdout(OutputStream stdout) throws IOException {
		mIO.setStdout(stdout);
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
