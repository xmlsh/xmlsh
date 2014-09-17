package org.xmlsh.sh.shell;

import java.io.IOException;

import org.xmlsh.core.ICommand;
import org.xmlsh.core.IFunction;


/*
 * Top level root module for the shell
 */
public class RootModule extends Module {

	
	// TEMP
	private static RootModule _instance = new RootModule();
	
	public static Module getInstance() {
		return _instance ;
	}
	
	private RootModule() {
		super("builtin");
	}
	
	@Override
	public String toString() {
		return getName();
	}
	@Override
	public ICommand getCommand(String name) throws IOException {
		return null;
	}

	@Override
	public IFunction getFunction(String name) {
		return null;
	}

	@Override
	public boolean hasHelp(String name) {
		return false;
	}

	@Override
	public String describe() {
		return getName();
	}

	@Override
	public void close() {
		getLogger().error("Root Module should never be closed");
		assert(false);
	}
}
