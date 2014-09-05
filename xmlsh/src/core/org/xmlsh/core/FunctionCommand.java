/**
 * $Id: ScriptCommand.java 27 2008-07-05 14:30:33Z daldei $
 * $Date: 2008-07-05 10:30:33 -0400 (Sat, 05 Jul 2008) $
 *
 */

package org.xmlsh.core;

import org.xmlsh.sh.core.CommandExpr;
import org.xmlsh.sh.core.ICommandExpr;
import org.xmlsh.sh.core.SourceLocation;
import org.xmlsh.sh.shell.IModule;
import org.xmlsh.sh.shell.Shell;

import java.io.File;
import java.util.List;

public class FunctionCommand implements ICommand {

	String				mName;
	ICommandExpr			 	mFunction;
	SourceLocation  	mLocation;

	public FunctionCommand( String name , ICommandExpr func ,  SourceLocation loc )
	{
		mName = name ;
		mFunction = func ;
		mLocation = loc ;
	}



	@Override
	public int run(Shell shell, String cmd, List<XValue> args) throws Exception {

		int ret =  shell.execFunction(mName,mFunction,mLocation,args);
		if( ret == 0 )
			ret = shell.getReturnValueAsExitValue() ;

		return ret;
	}

	/* (non-Javadoc)
	 * @see org.xmlsh.core.ICommand#getType()
	 */
	@Override
	public CommandType getType() {
		return CommandType.CMD_TYPE_FUNCTION ;
	}

	@Override
	public 	File 	getFile() 
	{ 
		return null; 
	}



	@Override
	public IModule getModule() {
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
