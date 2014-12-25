/**
 * $Id: ScriptCommand.java 27 2008-07-05 14:30:33Z daldei $
 * $Date: 2008-07-05 10:30:33 -0400 (Sat, 05 Jul 2008) $
 *
 */

package org.xmlsh.core;

import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.sh.core.ICommandExpr;
import org.xmlsh.sh.core.SourceLocation;
import org.xmlsh.sh.module.IModule;
import org.xmlsh.sh.shell.Shell;

public class FunctionCommand implements ICommand {

	String mName;
	ICommandExpr mFunction;
	SourceLocation mLocation;
	IModule mModule;
	private static Logger mLogger = LogManager.getLogger();
	@Override
	protected void finalize() {
		// Clear refs
		mFunction = null ;
		mLocation = null ;
		mModule = null ;
		
	}
	public FunctionCommand(IModule iModule, String name,
			ICommandExpr func, SourceLocation loc) {
		mLogger.entry(iModule, name, func, loc);
		assert (iModule != null);

		mName = name;
		mFunction = func;
		mLocation = loc;
		mModule = iModule;
	}

	@Override
	public URL getURL() throws MalformedURLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int run(Shell shell, String cmd, List<XValue> args) throws Exception {
		mLogger.entry(shell, cmd);

		int ret = shell
				.execFunctionAsCommand(mName, mFunction, mLocation, args);
		if (ret == 0)
			ret = shell.getReturnValueAsExitStatus(ret);

		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xmlsh.core.ICommand#getType()
	 */
	@Override
	public CommandType getType() {
		return CommandType.CMD_TYPE_FUNCTION;
	}

	@Override
	public IModule getModule() {
		return mModule;
	}


	@Override
	public SourceLocation getLocation() {
		return mLocation;
	}

	@Override
	public void setLocation(SourceLocation loc) {
		mLocation = loc;
	}

	@Override
	public void print(PrintWriter w, boolean bExec) {
		w.print(mName);
	}

}
