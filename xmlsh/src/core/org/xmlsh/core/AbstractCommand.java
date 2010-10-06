/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.Source;

import net.sf.saxon.s9api.XdmItem;
import org.apache.log4j.Logger;
import org.xmlsh.sh.core.SourceLocation;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.HelpUsage;
import org.xmlsh.util.Util;

public abstract class AbstractCommand implements ICommand {

	private static Logger mLogger = Logger.getLogger(AbstractCommand.class);
	
	protected	Shell mShell;
	protected XEnvironment mEnvironment;
	protected SourceLocation		mLocation ;
	
	public AbstractCommand() {
		
	}
	

	public abstract String getName();
	protected SerializeOpts getSerializeOpts(Options opts) throws InvalidArgumentException {
		return mShell.getSerializeOpts(opts);
	}

	protected SerializeOpts getSerializeOpts() throws InvalidArgumentException {
		return mShell.getSerializeOpts(null);
	}

	@Override
	public void close() {
		mShell = null ;
		mEnvironment = null ;
		
	}

	protected XEnvironment getEnv() { return mEnvironment;}

	protected InputPort getStdin() throws IOException { return mEnvironment.getStdin();}

	protected OutputPort getStdout() throws IOException { return mEnvironment.getStdout();}

	protected OutputPort getStderr() throws IOException { return mEnvironment.getStderr();}

	/**
	 * @return
	 * @see org.xmlsh.core.XEnvironment#getCurdir()
	 */
	public File getCurdir() {
		return mEnvironment.getCurdir();
	}

	public String getAbsoluteURI(String sysid) throws URISyntaxException {
		return mEnvironment.getAbsoluteURI( sysid );
	}

	public InputStream getInputStream(XValue file) throws CoreException {
		return mEnvironment.getInputStream(file,getSerializeOpts());
	}

	public Source getSource(XValue value) throws CoreException {
		return mEnvironment.getSource(value,getSerializeOpts());
	}

	/**
	 * @param file
	 * @param append
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @see org.xmlsh.core.XEnvironment#getOutputStream(java.lang.String, boolean)
	 */
	public OutputStream getOutputStream(String file, boolean append) throws FileNotFoundException,
			IOException {
				return mEnvironment.getOutputStream(file, append);
			}

	public InputPort getInput(XValue name) throws CoreException {
		return mEnvironment.getInput( name );
	}

	public OutputPort getOutput(XValue name, boolean append) throws CoreException, IOException {
		return mEnvironment.getOutput( name , append );
	}

	/**
	 * @param s
	 * @param e
	 * @see org.xmlsh.core.XEnvironment#printErr(java.lang.String, java.lang.Exception)
	 */
	public void printErr(String s, Exception e) {
		mEnvironment.printErr(s, e);
	}

	/**
	 * @param s
	 * @see org.xmlsh.core.XEnvironment#printErr(java.lang.String)
	 */
	public void printErr(String s) {
		mEnvironment.printErr(s);
	}


	protected File getFile(String fname) throws IOException {
		return mShell.getFile(fname);
	}


	protected File getFile(XValue fname) throws IOException {
		return mShell.getFile(fname);
	}	
	
	protected Shell getShell() {
		return mShell;
	}

	public void usage(String message)
	{
		String cmdName = this.getName();
		SourceLocation sloc = getLocation();
		if( sloc != null )
			mShell.printErr(sloc.toString());
		if( !Util.isBlank(message))
			mShell.printErr(cmdName + ": " + message);
		else
			mShell.printErr(cmdName + ":" );
		HelpUsage helpUsage = new HelpUsage( getShell() );
		try {
			helpUsage.doUsage(mEnvironment.getStdout(), cmdName);
		} catch (Exception e) {
			mLogger.warn("Exception printing usage" , e );
			mShell.printErr("Usage: <unknown>");
		}
	}
	public void usage()
	{
		usage(null);
	}
	
	public SourceLocation  getLocation() { return mLocation ; }
	public void setLocation(SourceLocation source) { mLocation = source ; }

	protected ClassLoader getClassLoader(XValue classpath) throws CoreException
	{
		return mShell.getClassLoader(classpath);
	}
	


}

//
//
//Copyright (C) 2008,2009,2010 David A. Lee.
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
