/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.core;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.xmlsh.sh.shell.Module;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.PortCopier;
import org.xmlsh.util.StreamCopier;
import org.xmlsh.util.Util;

public class ExternalCommand implements ICommand {

	private 	static		Logger		mLogger = LogManager.getLogger( ExternalCommand.class );
	
	private		File		mCommandFile;		// command path
	
	public ExternalCommand( File cmd )
	{
		mCommandFile = cmd;
	}
	
	
	public int run(Shell shell, String cmd, List<XValue> args) throws Exception 
	{
		File curdir = shell.getCurdir() ;
		mLogger.debug("Run external command: " + mCommandFile.getPath() + " in directory: " + curdir.getPath() );
		
		
		ArrayList<XValue> cmdlist = new ArrayList<XValue>();
		cmdlist.add( new XValue(mCommandFile.getPath()));
		cmdlist.addAll( Util.expandSequences(args));
		Process proc = null ;
		synchronized ( this.getClass() ){
		ProcessBuilder	builder = new ProcessBuilder();
		builder.command( Util.toStringList(cmdlist));
		builder.directory( curdir );
		
		proc = builder.start();
		
		if( proc == null )
			return -1;
		}
		
		// Start copiers for stdout, stderr
		
		
		StreamCopier outCopier = new StreamCopier( proc.getInputStream() , shell.getEnv().getStdout().asOutputStream(),true);
		StreamCopier errCopier = new StreamCopier( proc.getErrorStream() , shell.getEnv().getStderr().asOutputStream(),true);
		
		
		PortCopier inCopier = null;
		
		if( shell.getEnv().isStdinRedirected() )
			inCopier = new PortCopier(  shell.getEnv().getStdin() , proc.getOutputStream() , shell.getSerializeOpts() , true );
		
		else
			proc.getOutputStream().close();
		
	
		errCopier.start();
		
		if( inCopier != null )
			inCopier.start();
		
		
		// outCopier.start();
		outCopier.run(); // In place
		
		
		int ret = proc.waitFor();
		
		// Kill off the input copier
		// inCopier.interrupt();
	
		
		if( inCopier != null )
			inCopier.join();
		
		outCopier.join();
		errCopier.join();
		
		return ret ;
		
		
		
	}
	
	/* (non-Javadoc)
	 * @see org.xmlsh.core.ICommand#getType()
	 */
	public CommandType getType() {
		return CommandType.CMD_TYPE_EXTERNAL;
	}
	
	public File getFile() {
		return mCommandFile ;  
		
	}


	public Module getModule() {
		// TODO Auto-generated method stub
		return null;
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
