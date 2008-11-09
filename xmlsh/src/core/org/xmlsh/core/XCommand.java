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
import java.util.List;

import org.xmlsh.sh.shell.Shell;

public abstract class XCommand implements ICommand {
	
	private XEnvironment mEnvironment;
	
	
	protected void throwInvalidArg(String string)
			throws InvalidArgumentException {
				printErr(string);
				throw new InvalidArgumentException( string );
				
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
	
	protected InputStream getStdin() { return mEnvironment.getStdin();}
	protected OutputStream getStdout() { return mEnvironment.getStdout();}
	protected OutputStream getStderr() { return mEnvironment.getStderr();}
	

	/**
	 * @return
	 * @see org.xmlsh.core.XEnvironment#getCurdir()
	 */
	public File getCurdir() {
		return mEnvironment.getCurdir();
	}



	/**
	 * @param fname
	 * @return
	 * @throws IOException
	 * @see org.xmlsh.core.XEnvironment#getFile(java.lang.String)
	 */
	public File getFile(String fname) throws IOException {
		return mEnvironment.getFile(fname);
	}



	/**
	 * @param fvalue
	 * @return
	 * @throws IOException
	 * @see org.xmlsh.core.XEnvironment#getFile(org.xmlsh.core.XValue)
	 */
	public File getFile(XValue fvalue) throws IOException {
		return mEnvironment.getFile(fvalue);
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
