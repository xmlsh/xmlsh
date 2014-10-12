/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.core;

import java.io.File;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.sh.core.SourceLocation;
import org.xmlsh.sh.module.IModule;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.sh.shell.ShellConstants;
import org.xmlsh.util.PortCopier;
import org.xmlsh.util.StreamCopier;
import org.xmlsh.util.Util;

public class ExternalCommand implements ICommand {

	private 	static			Logger		mLogger = LogManager.getLogger( ExternalCommand.class );

	private		File			mCommandFile;		// command path
	private		SourceLocation 	mLocation ;
	private    IModule mModule ;
	
	
	@Override
	protected void finalize() {
		// Clear refs
		mCommandFile = null ;
		mLocation = null ;
		mModule = null ;
		
	}
	public ExternalCommand( File cmd , SourceLocation location, IModule module )
	{
		mLogger.entry(cmd,location,module);
		assert( module != null );
		mCommandFile = cmd;
		mLocation = location ;
		mModule = module;
	}
	@Override
	public URL getURL() throws MalformedURLException {
		return mCommandFile.toURI().toURL();
	}
	@Override
	public int run(Shell shell, String cmd, List<XValue> args) throws Exception
	{
		File curdir = Shell.getCurdir().getAbsoluteFile();
		mLogger.debug("Run external command: " + mCommandFile.getPath() + " in directory: " + curdir.getPath());

		ArrayList<XValue> cmdlist = new ArrayList<XValue>();
		cmdlist.add(XValue.newXValue(mCommandFile.getPath()));
		cmdlist.addAll(Util.expandSequences(args));
		Process proc = null;
		synchronized (this.getClass()) {
			ProcessBuilder builder = new ProcessBuilder();
			builder.command(Util.toStringList(cmdlist));
			builder.directory(curdir);

			setEnvironment(shell, builder);

			proc = builder.start();

			if(proc == null)
				return -1;
			shell.addChildProcess( proc );

		}

		// Start copiers for stdout, stderr

		SerializeOpts serializeOpts = shell.getSerializeOpts();
		StreamCopier outCopier = new StreamCopier(cmd + "-out", proc.getInputStream(), shell.getEnv().getStdout()
				.asOutputStream(serializeOpts));
		StreamCopier errCopier = new StreamCopier(cmd + "-err", proc.getErrorStream(), shell.getEnv().getStderr()
				.asOutputStream(serializeOpts));

		PortCopier inCopier = null;

		if(!shell.getEnv().isStdinSystem())
			inCopier = new PortCopier(cmd + "-in", shell.getEnv().getStdin(), proc.getOutputStream(), serializeOpts);

		else
			proc.getOutputStream().close();

		errCopier.start();

		if(inCopier != null)
			inCopier.start();

		// outCopier.start();
		outCopier.run(); // In place

		// Close input just in case we have a broken pipe
		outCopier.closeIn();

		int ret;
		try {
			ret = proc.waitFor();
		} catch (InterruptedException e) {
			mLogger.warn("Interrupted waiting for process to complete: " , e  );
			ret = -1;
		}


		shell.removeChildProcess(proc);

		// Kill off the input copier
		// inCopier.interrupt();
		// Process has exited, close the output

		if(inCopier != null) {
			// Input copier is pointless - try to close it but dont worry if you
			// cant
			// This may interrupt the copier
			inCopier.close();

		}

		outCopier.join();
		outCopier.close();
		errCopier.join();
		errCopier.close();

		return ret;

	}

	/*
	 * Set the environment for a subprocess by the following
	 * 
	 * 1) If a variable does not exist in the shell then delete it
	 * 2) For any "EXPORT" variables which are atomic Update any existing variables with new content from the shell
	 * 3) Add any unset "EXPORT" atomic variables
	 * 4) PATH and XPATH are re-set by re-serializing the sequence using the path seperator
	 *    and the native directory seperator
	 */
	private void setEnvironment(Shell shell, ProcessBuilder builder) {
		XEnvironment xenv = shell.getEnv();
		Map<String,String> env = builder.environment();
		if( env == null )
			return ;

		// 1) delete any env vars not in the shell 
		// Use Iterator so we can call remove
		Iterator<Entry<String,String>> iter = env.entrySet().iterator();
		while( iter.hasNext() ){
			Entry<String,String> e = iter.next();
			String name = e.getKey();

			// Remove PATH and XPATH as well as non-defined names
			if( Util.isPath(name) || ! xenv.isDefined(name) )
				iter.remove();
		}



		/*
		 *  2) For any "EXPORT" variables Update any existing variables with new content from the shell
		 * 3) Add any unset "EXPORT" variables of type string
		 */

		for( String name : xenv.getVarNames() ){
			if( !Util.isPath(name) ){
				XVariable var = xenv.getVar(name);
				if( var.isExport() && var.getValue() != null && 
						! var.getValue().isNull() && var.getValue().isAtomic() )
					env.put(name , var.getValue().toString() );
			}


		}

		// Special case for PATH and XPATH
		XVariable vpath = xenv.getVar(ShellConstants.PATH);
		if( vpath != null  && vpath.isExport() ){
			SearchPath p = new SearchPath( vpath.getValue() );
			String ps = p.toOSString();
			env.put(ShellConstants.PATH, ps);
		}

		XVariable vxpath = xenv.getVar(ShellConstants.XPATH);
		if( vxpath != null && vxpath.isExport() ){
			SearchPath p = new SearchPath( vxpath.getValue() );
			String ps = p.toOSString();
			env.put(ShellConstants.XPATH, ps);
		}





	}
	/* (non-Javadoc)
	 * @see org.xmlsh.core.ICommand#getType()
	 */
	@Override
	public CommandType getType() {
		return CommandType.CMD_TYPE_EXTERNAL;
	}






	@Override
	public SourceLocation getLocation() {
		return mLocation ;
	}


	@Override
	public void setLocation(SourceLocation loc) {
		mLocation = loc ;
	}

  @Override
  public void print(PrintWriter w, boolean bExec)
  {
    w.print(  mCommandFile.getPath() );
    
  }
@Override
public IModule getModule() {

	return mModule ;

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
