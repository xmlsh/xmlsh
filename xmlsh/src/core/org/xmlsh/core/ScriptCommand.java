/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.core;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.URL;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.xmlsh.sh.core.SourceLocation;
import org.xmlsh.sh.grammar.ParseException;
import org.xmlsh.sh.module.IModule;
import org.xmlsh.sh.module.Module;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.Util;

public class ScriptCommand implements ICommand {

	public enum SourceMode {
		SOURCE, RUN, IMPORT, VALIDATE
	};

	private static Logger mLogger = org.apache.logging.log4j.LogManager
			.getLogger();
	private SourceMode mSourceMode;
	private IModule mModule; // The module in which the script was located
	private SourceLocation mLocation;
	private ScriptSource mSource;

	// Finalize script command make sure to close
	@Override
	protected void finalize() {
		// Clear refs
		mSource = null ;
		mLocation = null ;
		mModule = null ;
		
	}

	public ScriptCommand(ScriptSource source, SourceMode sourceMode,
			SourceLocation location, IModule moduleHandle)
			throws FileNotFoundException {
		mLogger.entry(source, sourceMode, location, moduleHandle);
		assert (moduleHandle != null);
		mSource = source;
		mSourceMode = sourceMode;
		mLocation = location;
		mModule = moduleHandle;

	}

	@Override
	public int run(Shell shell, String cmd, List<XValue> args)
			throws ThrowException, ParseException, IOException,
			UnimplementedException {

		mLogger.entry(shell, cmd);
		assert (mModule != null);

		try (Reader mScriptStreamSource = getScriptSource()) {

			mLogger.trace("Running {} in {} mode" , cmd , mSourceMode);
			switch (mSourceMode) {
			case SOURCE:
				return shell.runScript(mScriptStreamSource,
						mSource.getScriptName(), true).mExitStatus;
			case RUN: {
				try (Shell sh = shell.clone()) {
					if (args != null)
						sh.setArgs(args);
					sh.setArg0(mSource.getScriptName());
					int ret = sh.runScript(mScriptStreamSource,
							mSource.getScriptName(), true).mExitStatus;
					return ret;
				}
			}
			case VALIDATE:

				return shell.validateScript(mScriptStreamSource,
						mSource.getScriptName() ) ? 0 : 1;

			case IMPORT: {
				int ret = shell.runScript(mScriptStreamSource,
						mSource.getScriptName(), true).mExitStatus;
				;
				return ret;
			}

			default:
				mLogger.warn("Run mode not implemented: {}", mSourceMode);
				throw new UnimplementedException("Source mode: "
						+ mSourceMode.toString() + " Not implemented");
			}
		}
	}

	private Reader getScriptSource() throws IOException {
		return mSource.openReader();
		
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xmlsh.core.ICommand#getType()
	 */
	@Override
	public CommandType getType() {
		return CommandType.CMD_TYPE_SCRIPT;
	}

	@Override
	public URL getURL() {
		return mSource.getURL(); // may be null

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

	public String getScriptName() {
		return mSource.getScriptName();
	}

	@Override
	public void print(PrintWriter w, boolean bExec) {
		w.print(mSource.getScriptName());
	}

}
