package org.xmlsh.sh.shell;

import java.net.URL;
import java.util.HashMap;
import java.util.List;

import module.ModuleClass;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.builtin.commands.jobs;
import org.xmlsh.core.ReferenceCountedHandle;
import org.xmlsh.core.XClassLoader;
import org.xmlsh.core.XValue;

public abstract class Module implements IModule 
{

	protected String mName;
	protected ClassLoader mClassLoader;
	protected URL mHelpURL = null;
	private HashMap<String, Class<?>> mClassCache = new HashMap<String, Class<?>>();
	protected HashMap<String, Boolean> mScriptCache = new HashMap<String, Boolean>();

	// TODO
	public ModuleClass getModuleClass() 
	{
		return null ;
	}
	protected Module(String name) {
		mName = name ;
	}

	@Override
	public StaticContext getStaticContext() {
	
		getLogger().entry();
		return null;
	}

	@Override
	protected void finalize() {
		// Clear refs
		mClassLoader = null;
		if (mClassCache != null)
			mClassCache.clear();
		mClassCache = null;
	
		if (mScriptCache != null)
			mScriptCache.clear();
		mScriptCache = null;
	
	}

	protected Logger getLogger() {
		return LogManager.getLogger(getClass());
	
	}

	protected Class<?> findClass(String className) {
	
		getLogger().entry(className);
		// Find cached class name even if null
		// This caches failures as well as successes
		// Consider changing to a WeakHashMap<> if this uses up too much memory
		// caching failed lookups
		if (mClassCache.containsKey(className))
			return mClassCache.get(className);
	
		Class<?> cls = null;
		try {
			cls = Class.forName(className, true, mClassLoader);
		} catch (ClassNotFoundException e) {
	
		}
		// Store class in cache even if null
		mClassCache.put(className, cls);
		return cls;
	
	}

	protected Class<?> findClass(String name, List<String> packages) {
		for (String pkg : packages) {
			Class<?> cls = findClass(pkg + "." + name);
			if (cls != null)
				return cls;
		}
		return null;
	}

	protected ClassLoader getClassLoader(List<URL> classpath) {
		if (classpath == null || classpath.size() == 0)
			return getClass().getClassLoader();
	
		return new XClassLoader(classpath.toArray(new URL[classpath.size()]),
				getClass().getClassLoader());
	
	}

	@Override
	public URL getHelpURL() {
		return mHelpURL;
	}

	@Override
	public String getName() {
		return mName;
	}

	@Override
	public URL getResource(String res) {
		/*
		 * Undocumented: When using a classloader to get a resource, then the
		 * name should NOT begin with a "/"
		 */
		if (res.startsWith("/"))
			res = res.substring(1);
		return mClassLoader.getResource(res);
	}

	@Override
	public void onInit(Shell shell, List<XValue> args) throws Exception {
		getLogger().trace("module {} onInit()", getName());
	
	}

	@Override
	public void onLoad(Shell shell) {
		getLogger().trace("module {} onLoad()", getName());
	
	}

	protected String toResourceName(String name, String pkg) {
		String resource = pkg.replace('.', '/') + "/" + name;
		return resource;
	}

	@Override
	public String toString() {
		return getName();
	}



}