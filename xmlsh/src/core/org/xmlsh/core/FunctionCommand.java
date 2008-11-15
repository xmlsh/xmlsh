/**
 * $Id: ScriptCommand.java 27 2008-07-05 14:30:33Z daldei $
 * $Date: 2008-07-05 10:30:33 -0400 (Sat, 05 Jul 2008) $
 *
 */

package org.xmlsh.core;

import java.io.File;
import java.util.List;

import org.xmlsh.sh.core.Command;
import org.xmlsh.sh.shell.Shell;

public class FunctionCommand implements ICommand {
	
	private String	mName;
	private Command mBody;
	
	public FunctionCommand( String name, Command body )
	{
		mName = name ;
		mBody = body ;
	}
	
	
	
	public int run(Shell shell, String cmd, List<XValue> args) throws Exception {
		
		List<XValue> saveArgs = shell.getArgs();
		String saveArg0 = shell.getArg0();
		
		
		try {
			
			shell.setArg0(mName);
			shell.setArgs(args);
			return	shell.exec(mBody);
			
		
		} finally {
			
			
			shell.setArg0(saveArg0);
			shell.setArgs(saveArgs);
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
}
