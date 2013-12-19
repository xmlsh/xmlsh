/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.core;

import java.io.File;
import java.util.List;

import org.xmlsh.sh.shell.Module;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.xpath.ShellContext;


public abstract class BuiltinCommand extends AbstractCommand implements ICommand {
	
	private String mName;

	public String getName()
	{
		return mName;
	}
	
	/* (non-Javadoc)
	 * @see org.xmlsh.core.ICommand#getType()
	 */
	public CommandType getType() {
		return CommandType.CMD_TYPE_BUILTIN ;
	}

	
	public File getFile() {
		return null ; // builtins have no file  
		
	}
	public Module getModule()
	{
		return null;
	}
	
	abstract protected int run( List<XValue> args) throws Exception;

	public int 	run( Shell shell , String cmd , List<XValue> args )  throws Exception
	{
		
		
		mShell 		 = shell;
		mEnvironment = shell.getEnv();
		mName  = cmd ;
		Shell saved_shell = ShellContext.set( shell );
		try {
			return run(args);
		}  
		catch( UnknownOption e )
		{
			usage( e.getMessage() );
			return -1;
		}
		
		finally {
			ShellContext.set(saved_shell);
		}
		
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
