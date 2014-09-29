/**
 * $Id: $
 * $Date: $
 * 
 */

package org.xmlsh.sh.module;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import org.xmlsh.core.CoreException;
import org.xmlsh.core.ICommand;
import org.xmlsh.core.IFunctionExpr;
import org.xmlsh.core.IHandleable;
import org.xmlsh.core.IReferenceCounted;
import org.xmlsh.core.IReferenceCountedHandleable;
import org.xmlsh.core.IReferencedCountedHandle;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.XClassLoader;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.sh.shell.StaticContext;

/*
 * A runtime instance of a module that may have state and may be shared or duplicated
 * in different contexts.
 */
public interface IModule  {

	public abstract String describe();
	public abstract URL getResource(String res);
	public abstract boolean hasHelp(String name);
	public abstract void onLoad(Shell shell) throws Exception;

	public ICommand getCommand(String name) throws IOException, URISyntaxException;

	// close() is called for an unload

	public IFunctionExpr getFunction(String name);

	public URL getHelpURL();
	// locate a resource in the module ( directory or package )
	public URL findResource(String res);
	
	

	public String getName();

	StaticContext getStaticContext();

	public void onInit(Shell shell, List<XValue> args) throws Exception;
	public  ModuleConfig getModuleConfig(Shell shell , String name , List<URL> ata ) throws  Exception;
	public ModuleConfig getConfig();
	public XClassLoader getClassLoader();
	public void addClassPaths(Shell shell, List<URL> urls) throws CoreException;
}

/*
 * Copyright (C) 2008-2012 David A. Lee.
 * 
 * The contents of this file are subject to the "Simplified BSD License" (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.opensource.org/licenses/bsd-license.php
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * 
 * The Original Code is: all this file.
 * 
 * The Initial Developer of the Original Code is David A. Lee
 * 
 * Portions created by (your name) are Copyright (C) (your legal entity). All
 * Rights Reserved.
 * 
 * Contributor(s): David A. Lee
 */