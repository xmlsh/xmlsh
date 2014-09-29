package org.xmlsh.sh.core;

import org.xmlsh.core.CoreException;
import org.xmlsh.sh.module.IModule;

public interface IExpression {

	public SourceLocation getSourceLocation();
	public boolean hasLocation();
	// Default name if none provided
	public String getName();
	public String describe(boolean execMode);
	public IModule getModule() throws CoreException;


}