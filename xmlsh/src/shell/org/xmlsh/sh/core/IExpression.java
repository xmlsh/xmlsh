package org.xmlsh.sh.core;

import org.xmlsh.sh.shell.IModule;

public interface IExpression {

	public SourceLocation getSourceLocation();
	public boolean hasLocation();
	// Default name if none provided
	public String getName();
	public String describe(boolean execMode);
	public IModule getModule();


}