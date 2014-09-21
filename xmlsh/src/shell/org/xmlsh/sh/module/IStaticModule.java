package org.xmlsh.sh.module;

import java.net.URL;

import org.xmlsh.sh.shell.Shell;


/*
 * A Module Refernce that has been resolved and bound to a concrete resource.
 * Implementations are effectively immutable in that they shouldnt have state which
 * affects behaviour if the Static Module is shared across Module instances.
 */
public interface IStaticModule {
	public abstract String describe();
	public abstract ModuleClass getModuleClass();
	public abstract String getName();
	public abstract URL getResource(String res);
	public abstract boolean hasHelp(String name);
	public abstract void onLoad(Shell shell) throws Exception;

}