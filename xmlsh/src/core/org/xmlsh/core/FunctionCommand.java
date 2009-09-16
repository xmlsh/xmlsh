/**
 * $Id: ScriptCommand.java 27 2008-07-05 14:30:33Z daldei $
 * $Date: 2008-07-05 10:30:33 -0400 (Sat, 05 Jul 2008) $
 *
 */

package org.xmlsh.core;

import java.io.File;
import java.util.List;

import org.xmlsh.sh.core.FunctionDefinition;
import org.xmlsh.sh.shell.Module;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.xpath.ShellContext;

public class FunctionCommand implements ICommand {
	
	FunctionDefinition 	mFunction;
	
	public FunctionCommand( FunctionDefinition func )
	{
		mFunction = func ;
	}
	
	
	
	public int run(Shell shell, String cmd, List<XValue> args) throws Exception {
		
		List<XValue> saveArgs = shell.getArgs();
		String saveArg0 = shell.getArg0();

		if( mFunction.isSubShell() )
			shell = shell.clone();

		
		Shell saved_shell = ShellContext.set( shell );
		
		try {
			
			shell.setArg0(mFunction.getName());
			shell.setArgs(args);
			int ret =	shell.execFunction(mFunction.getBody());
			return ret;
			
		
		} finally {
			ShellContext.set(saved_shell);
			
			if( mFunction.isSubShell() )
				shell.close();
			else {
				shell.setArg0(saveArg0);
				shell.setArgs(saveArgs);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.xmlsh.core.ICommand#getType()
	 */
	public CommandType getType() {
		return CommandType.CMD_TYPE_FUNCTION ;
	}

	public 	File 	getFile() 
	{ 
		return null; 
	}



	public Module getModule() {
		// TODO: Return the module of the invoking script
		return null;
	}	
}
