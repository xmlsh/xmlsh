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
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.Source;

import net.sf.saxon.s9api.XdmItem;
import org.xmlsh.sh.shell.Module;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.sh.shell.Shell;

public abstract class XCommand implements ICommand {
	
	private XEnvironment mEnvironment;
	private Module mModule;
	
	
	protected void throwInvalidArg(String string)
			throws InvalidArgumentException {
				printErr(string);
				throw new InvalidArgumentException( string );
				
	}

	public Module getModule()
	{
		return mModule;
	}


	/* (non-Javadoc)
	 * @see org.xmlsh.core.ICommand#run(org.xmlsh.sh.shell.Shell, java.lang.String[])
	 */
	public int run(Shell shell, String cmd, List<XValue> args) throws Exception 
	{

		
		return run(  args , shell.getEnv() );
		
	}
	
	abstract public int run( List<XValue>  args ) throws Exception;

	
   public int run( List<XValue>  args, XEnvironment env ) throws Exception
   {
	   mEnvironment = env ;
	   return run(args);
	   
   }
	
	/* (non-Javadoc)
	 * @see org.xmlsh.core.ICommand#getType()
	 */
	public CommandType getType() {
		return CommandType.CMD_TYPE_INTERNAL ;
	}
	
	public File getFile() {
		return null ; // no associated file 
		
	}

	protected	XEnvironment getEnv() { return mEnvironment;}
	
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


	public	InputPort	getInput( XValue name ) throws CoreException
	{
		return mEnvironment.getInput( name );
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



	protected SerializeOpts getSerializeOpts() {
		return mEnvironment.getShell().getSerializeOpts();
	}

	public void setModule(Module module) {
		mModule = module ;
		
	}

	protected File getFile(XValue fname) throws IOException {
		return mEnvironment.getShell().getFile(fname);
	}

	protected File getFile(String fname) throws IOException {
		return mEnvironment.getShell().getFile(fname);
	}

	protected ClassLoader getClassLoader(XValue classpath) throws MalformedURLException, IOException,
			URISyntaxException {
				if( classpath == null )
					return this.getClass().getClassLoader();
				List<URL> urls = new ArrayList<URL>();
				for( XdmItem item : classpath.asXdmValue() ){
					String cp = item.getStringValue();
					URL url = getEnv().getShell().getURI(cp).toURL();
					urls.add(url);
					
					
				}
				URLClassLoader loader = new URLClassLoader( (URL[]) urls.toArray(new URL[urls.size()]));
				return loader;
			}

	
	
}
//
//
//Copyright (C) 2008,2009 , David A. Lee.
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
