package org.xmlsh.sh.module;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.ICommand;
import org.xmlsh.core.IFunctionExpr;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.XClassLoader;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.IFunctionDefiniton;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.sh.shell.StaticContext;
import org.xmlsh.util.JavaUtils;

public abstract class Module implements IModule {

	static Logger mLogger = LogManager.getLogger();

	private ModuleConfig mConfig;
	public List<URL> getClasspath() {
		return mConfig.getClasspath();
	}

	public String getTextEncoding() {
		return mConfig.getInputTextEncoding();
	}

	
	// Predeclared function classes indexed by *simple name* (no package component)
	private HashMap<String, Class<?>> mFunctionClassCache = new HashMap<String, Class<?>>();

	private HashMap<String, Class<?>> mClassCache = new HashMap<String, Class<?>>();
	private HashMap<String, Boolean> mClassCacheMisses = new HashMap<String, Boolean>();

	protected Module( ModuleConfig config ) {
		mConfig = config ;
		mConfig.setClassLoader( getClassLoader( config.getClasspath()));
		
	}

	@Override
	protected void finalize() {
		// Clear refs
		if (mClassCache != null)
			mClassCache.clear();
		mClassCache = null;
		if( mFunctionClassCache != null )
			mFunctionClassCache.clear();
		mFunctionClassCache = null;
		if(mClassCacheMisses != null)
			mClassCacheMisses.clear();
		mClassCacheMisses = null;

	}
	
	
	protected Class<?> findFunctionClass(String className) {

		return mFunctionClassCache.get(className);
	}
	
	
	
	protected Class<?> findClass(String className) {

		mLogger.entry(className);
		// Find cached class name even if null
		// This caches failures as well as successes
		// Consider changing to a WeakHashMap<> if this uses up too much memory
		// caching failed lookups
		if (mClassCache.containsKey(className))
			return mClassCache.get(className);
		

		Class<?> cls = null;
		try {
			cls = Class.forName(className, true, getClassLoader());
		} catch (ClassNotFoundException e) {

		}
		declareClass(className, cls);
		return cls;

	}

	protected void declareClass(String className, Class<?> cls) {
		// Store class in cache even if null
		mClassCache.put(className, cls);
	}

	/*
	 * Most derived classes should override parent classes 
	 * 
	 */
	protected void declareFunctionClass(String name, Class<?> cls) { 
		mLogger.entry(name, cls);
		Class<?> exists = mFunctionClassCache.get(name);
		if( exists == null )
    		mFunctionClassCache.put( name ,  cls  );
		else {
			// Override with most derived type
			if( exists.isAssignableFrom(cls)){ // Exists is equal to or a super class 
				mLogger.trace("Overriding base class {} with derived class {} ",exists, cls );
				// Override it with sub class 
				mFunctionClassCache.put( name , cls );
			}
			
		}
		declareClass( cls.getName() , cls );
		
	}
	
	protected Class<?> findClass(String name, List<String> packages) {
		for (String pkg : packages) {
			Class<?> cls = findClass(pkg + "." + name);
			if (cls != null)
				return cls;
		}
		return null;
	}

	protected URL findResourceInPackages(String name, List<String> packages) {
		/*
		 * Undocumented: When using a classloader to get a resource, then the
		 * name should NOT begin with a "/"
		 */

		/*
		 * Get cached indication of if there is a resource by this name
		 */

		if (hasClassLookupFailed(name))
			return null;

		for (String pkg : packages) {
			URL is = getClassLoader().getResource(toResourceName(name, pkg));
			if (is != null) {
				setCacheHit(name);
				return is;
			}
		}
		setCacheMissed(name);

		return null;
	}
	
	@Override
	public ClassLoader getClassLoader() {
		return getConfig().getClassLoader();
	}

	protected ClassLoader getClassLoader(List<URL> classpath) {
		if (classpath == null || classpath.size() == 0)
			return getClass().getClassLoader();

		return new XClassLoader(classpath.toArray(new URL[classpath.size()]),
				getClass().getClassLoader());

	}

	@Override
	public URL getHelpURL() {
		return null ;
	}

	protected Logger getLogger() {
		return LogManager.getLogger(getClass());

	}


	@Override
	public String getName() {
		return mConfig.getName();
	}

	@Override
	public URL getResource(String res) {
		/*
		 * Undocumented: When using a classloader to get a resource, then the
		 * name should NOT begin with a "/"
		 */
		if (res.startsWith("/"))
			res = res.substring(1);
		return getClassLoader().getResource(res);
	}

	@Override
	public StaticContext getStaticContext() {

		getLogger().entry();
		return null;
	}

	protected boolean hasClassLookupFailed(String name) {
		Boolean hasResource = mClassCacheMisses.get(name);
		if (hasResource != null && !hasResource.booleanValue())
			return true;
		return false;

	}

	@Override
	public void onInit(Shell shell, List<XValue> args) throws Exception {
		getLogger().trace("module {} onInit()", getName());

	}

	@Override
	public void onLoad(Shell shell) {
		getLogger().trace("module {} onLoad()", getName());

	}

	protected void setCacheHit(String name) {
		mClassCacheMisses.put(name, true);
	}

	protected void setCacheMissed(String name) {
		mClassCacheMisses.put(name, false);
	}


	protected String toResourceName(String name, String pkg) {
		String resource = pkg.replace('.', '/') + "/" + name;
		return resource;
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public ModuleConfig getConfig() {
		return mConfig;
	}

	@Override
	public ModuleConfig getModuleConfig(Shell shell , String name , List<URL> at ) throws  Exception {

		mLogger.error("NOT IMPLEMENTED");
		return null;
	
	}
	
	
	/* 
	 * Static check if this is a possible function class
	 */
	protected static boolean isFunctionClass(Class<?> cls) {

		if( cls == null )
			return false ;
		if(  IFunctionDefiniton.class.isAssignableFrom( cls ) ) 
			return true ;
		if( IFunctionExpr.class.isAssignableFrom(cls ))
			return true ;
		if( cls.getAnnotation(org.xmlsh.annotations.Function.class ) != null )
			return true ;
		return false ;
	
	
	}


	/* 
	 * Static check if this is a possible function class
	 */
	protected static boolean isCommandClass(Class<?> cls) {

		if( cls == null )
			return false ;
		if(  ICommand.class.isAssignableFrom( cls ) ) 
			return true ;
		if( IFunctionExpr.class.isAssignableFrom(cls ))
			return true ;
		if( cls.getAnnotation(org.xmlsh.annotations.Function.class ) != null )
			return true ;
		return false ;
	
	
	}
}