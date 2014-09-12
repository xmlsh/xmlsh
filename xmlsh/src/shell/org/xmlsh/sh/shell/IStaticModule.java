package org.xmlsh.sh.shell;

import java.net.URL;


/*
 * A Module Refernce that has been resolved and bound to a concrete resource.
 * Implementations are effectively immutable in that they shouldnt have state which
 * affects behaviour if the Static Module is shared across Module instances.
 */
public interface IStaticModule {
	public abstract void onLoad(Shell shell) throws Exception;
	public abstract String getName();
	public abstract boolean hasHelp(String name);
	public abstract String describe();
	public abstract URL getResource(String res);

}