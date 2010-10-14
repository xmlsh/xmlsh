/**
 * $Id: ScriptCommand.java 27 2008-07-05 14:30:33Z daldei $
 * $Date: 2008-07-05 10:30:33 -0400 (Sat, 05 Jul 2008) $
 *
 */

package org.xmlsh.core;

import java.io.File;
import java.util.List;

import org.xmlsh.sh.core.Command;
import org.xmlsh.sh.core.SourceLocation;
import org.xmlsh.sh.shell.Module;
import org.xmlsh.sh.shell.Shell;

public class FunctionCommand implements ICommand {
	
	String				mName;
	Command			 	mFunction;
	SourceLocation  	mLocation;
	
	public FunctionCommand( String name , Command func ,  SourceLocation loc )
	{
		mName = name ;
		mFunction = func ;
		mLocation = loc ;
	}
	
	
	
	public int run(Shell shell, String cmd, List<XValue> args) throws Exception {
		
		List<XValue> saveArgs = shell.getArgs();
		String saveArg0 = shell.getArg0();

		Variables save_vars = shell.pushLocalVars();

		
		
		try {
			
			shell.setArg0(mName);
			shell.setArgs(args);
			int ret =	shell.execFunction(mFunction);
			return ret;
			
		
		} finally {
			shell.popLocalVars(save_vars);
			
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



	public Module getModule() {
		// TODO: Return the module of the invoking script
		return null;
	}




	/* (non-Javadoc)
	 * @see org.xmlsh.core.ICommand#close()
	 */
	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}



	@Override
	public SourceLocation getLocation() {
		return mLocation ;
	}



	@Override
	public void setLocation(SourceLocation loc) {
		mLocation = loc ;
	}	
	
	
}
