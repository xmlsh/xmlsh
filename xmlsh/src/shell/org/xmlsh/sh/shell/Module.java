/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.sh.shell;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import org.xmlsh.core.CoreException;
import org.xmlsh.core.ICommand;
import org.xmlsh.core.IFunction;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Path;
import org.xmlsh.core.ScriptCommand;
import org.xmlsh.core.ScriptFunctionCommand;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XClassLoader;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.core.Command;
import org.xmlsh.util.JavaUtils;
import org.xmlsh.util.Util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Module {
	private String mName;
	private String mPrefix; // may be null
	private String mPackage; // may NOT be null
	private ClassLoader mClassLoader; // Classloader for this module
	private	 URL mHelpURL = null ; 
	
	private		HashMap<String , Class<?>>		mClassCache = new HashMap<String,Class<?>>();
	private		HashMap<String , Boolean>	mScriptCache = new HashMap<String,Boolean>();
	private static Logger mLogger = LogManager.getLogger(Module.class);
	
	
	private Class mJavaClass ; // If this is a Java module
    

	/*
	 * Constructor for internal modules like xlmsh
	 * These dont get their own thread group
	 */
	public Module(String prefix, String name, String pkg, String helpURL) {
		mName = name;
		mPrefix = prefix;
		mPackage = pkg;
		mClassLoader = getClassLoader(null);
		// Undocumented - if you use a class loader to find a resource dont start it with "/"
		mHelpURL = mClassLoader.getResource(helpURL.replaceFirst("^/", "")) ;
	}

	/*
	 * Constructor for external modules nameuri can either be a name found in
	 * XMODPATH or a full URI/filename of the module.xml file (must end in
	 * ".xml")
	 * 
	 */
	public Module(Shell shell, String prefix, String nameuri, List<XValue> args)
			throws CoreException {

		try {
			mPrefix = prefix;

			XdmNode configNode;
			URL configURL;
			URI nameURI;
			File modDir  = null ;
			
			// "java:xxx 
			try {
				nameURI = shell.getURI(nameuri);
				
				

			} catch( Exception e ) {
				mLogger.trace("excpetion parsing module as URI: " + nameuri , e );
				nameURI = null;
			}
			
			if( nameURI != null && Util.isEqual(nameURI.getScheme(),"java") )
				initJavaModule( shell ,  nameURI ,args);
			else {
			
			
			
			if (nameuri.endsWith(".xml")) {
				configURL = shell.getURL(nameuri);
				if( configURL.getProtocol().equals("file"))
					modDir = new File(configURL.getPath()).getParentFile();

			} else {

				Path path = shell.getPath("XMODPATH", true );
				modDir = path.getFirstFileInPath(shell,nameuri);
				if (modDir == null)
					throw new InvalidArgumentException("Cannot find module directory for : " + nameuri);

				File config = new File(modDir, "module.xml");
				if (!config.exists())
					throw new InvalidArgumentException("Cannot find module.xml in directory : " + modDir.getAbsolutePath() );
				configURL = config.toURI().toURL();
			}

			initModule(shell, configURL, modDir);
			
			
			}

		} 
		catch( CoreException e )
		{
			throw e ;
			
		}
		
		catch (Exception e) {
			throw new CoreException(e);
		}

	}
	

	private void initJavaModule(Shell shell, URI nameURI, List<XValue> args) throws CoreException
    {
		List<URL> classpath = null;
		 if( args.size() > 1 && args.remove(0).toString().equals("at") ) {
			    classpath = new ArrayList<URL>();
			    for( XValue xv : args ) {
	    		  URL classurl = shell.getURL(xv.toString());
	    		  classpath.add(classurl);
			    }
		 }
		 
		 mClassLoader = getClassLoader(classpath);
		 mHelpURL = null ;
		 String clsname  = nameURI.getRawSchemeSpecificPart();
		 


		 int ldot = clsname.lastIndexOf('.');
		 mName= clsname.substring(ldot+1);
		 mPackage = clsname.substring(0,ldot);
		 mJavaClass = findClass( mName );
		 if( mJavaClass == null )
			 throw new InvalidArgumentException("Class not found:" + clsname);
		 
		
    }

	private void initModule(Shell shell, URL configURL, File modDir) throws IOException, SaxonApiException,
            UnexpectedException, InvalidArgumentException, MalformedURLException
    {
	    XdmNode configNode;
	    configNode = Util.asXdmNode(configURL);

	    List<URL> classpath = new ArrayList<URL>();

	    XValue xv = new XValue(configNode);
	    mPackage = xv.xpath(shell,"/module/@package/string()").toString();
	    mName = xv.xpath(shell,"/module/@name/string()").toString();
	    String require = xv.xpath(shell,"/module/@require/string()").toString();
	    if( !Util.isBlank(require)){
	    	int ret = shell.requireVersion(mName,require);
	    	if( ret != 0 )
	    		throw new InvalidArgumentException("Module " + mName + " requires version " + require );
	    }
	    
	    

	    for (XdmItem item : xv.xpath(shell,"/module/classpath/file").asXdmValue()) {
	    	if (item instanceof XdmNode) {
	    		String file = ((XdmNode) item).getAttributeValue(new QName("url"));
	    		URL classurl = new URL(configURL, file);
	    		classpath.add(classurl);

	    	}

	    }
	    
	    
	    if( modDir != null )
	    	for (XdmItem item : xv.xpath(shell,"/module/classpath/directory").asXdmValue()) {
	    		if (item instanceof XdmNode) {
	    			String dir = ((XdmNode) item).getAttributeValue(new QName("url"));
	    			
	    			for( String file : listFiles(modDir,dir) ){
	    				URL classurl = new URL(configURL, file);
	    				classpath.add(classurl);
	    			}

	    		}

	    	}
	    	

	    mClassLoader = getClassLoader(classpath);
	    mHelpURL = mClassLoader.getResource(toResourceName("commands.xml"));
    }

	private List<String> listFiles(File modDir, String dir) throws IOException {
		
		List<String> files = new ArrayList<String>();
		File file = new File( modDir , dir );
		for( String f : file.list() )
			if( f.endsWith(".jar"))
				files.add(f);
		
		return files;
		
		
		
	}

	public boolean isJavaModule() {
		return mJavaClass != null ;
	}
	/**
	 * @return the prefix
	 */
	public String getPrefix() {
		return mPrefix;
	}

	public boolean isDefault() {
		return Util.isEmpty(mPrefix);
	}

	private InputStream getCommandResource(String name) {
		/*
		 * Undocumented: When using a classloader to get a resource, then the
		 * name should NOT begin with a "/"
		 * 
		 */
		
		
		/*
		 * Get cached indication of if there is a resource by this name
		 */
		
		Boolean hasResource = mScriptCache.get(name);
		if( hasResource != null && ! hasResource.booleanValue() )
			return null ;
		
		
		String resource = toResourceName(name);
		InputStream is = mClassLoader.getResourceAsStream(resource);

		mScriptCache.put( name , is != null );
		
		return is;
	}

	private String toResourceName(String name) {
		String resource = /* "/" + */
		mPackage.replace('.', '/') + "/" + name;
		return resource;
	}
	
	private boolean hasCommandResource(String name) {
		String resource = toResourceName(name);
		return ( mClassLoader.getResource(resource)  != null );

	}

	public ICommand getCommandClass(String name) {

		/*
		 * Convert from hyphen-case to camelCase 
		 */
		
		name = convertCamelCase( name );
		name = fromReserved( name );
		
		// Store the camel name not the hyphen name
		String origName = name ;
		
		
		
		/*
		 * First try to find a class that matches name
		 */

		try {

			Class<?> cls = findClass(name);
			if( cls != null ){
				Constructor<?> constructor = cls.getConstructor();
				if( constructor != null ){
					Object obj = constructor.newInstance();
					if( obj instanceof XCommand ){
						XCommand cmd = (XCommand) obj ;
						cmd.setModule(this);
						return cmd;
					}
				}
			}

		} catch (Exception e) {
			mLogger.debug("Exception calling constructor for:" + name , e );

		}

		/*
		 * Second 
		 * Try a script stored as a resource
		 */
		
		
		// mScriptCache caches a Boolean indicating whether the resource is found or not
		// No entry in cache means it has not been tested yet
		
		// Failures are cached with a null command
		String scriptName = origName + ".xsh";
		
		InputStream scriptStream = getCommandResource(scriptName);
		if (scriptStream != null)

			return new ScriptCommand(name, scriptStream, false, this);

		return null ;
		
	}
	private String fromReserved( String name )
	{
		if( JavaUtils.isReserved(name ) )
			return "_" + name ;
		else
			return  name;
	}
	
	/*
	 * Conversts hypen-case to camelCase, also converts from any reserved word 
	 */

	private String convertCamelCase(String name) {
		if( name.indexOf('-')  < 0 )
			return name ;
		
		
		String parts[] = name.split("-");
		if( parts.length == 1 )
			return name ;
		
		StringBuffer result = new StringBuffer( name.length() );
		
		for( String p : parts ){
			if( p.length() == 0 )
				continue ;
			
			if( result.length() == 0 )
				result.append(p );
			else {
				result.append( Character.toUpperCase( p.charAt(0)) );
				result.append( p.substring(1));
			}
			
			
		}
		
		return result.toString();
		
		
		
	}

	private Class<?> findClass(String name)  {
		
		
		String 	className = mPackage + "." + name;
		
		// Find cached class name even if null 
		// This caches failures as well as successes
		// Consider changing to a WeakHashMap<> if this uses up too much memory caching failed lookups
		if( mClassCache.containsKey(className))
			return mClassCache.get(className);
		
		Class<?> cls = null;
		try {
			cls = Class.forName(className, true, mClassLoader);
		} catch (ClassNotFoundException e) {
			
		}
		// Store class in cache even if null
		mClassCache.put(className , cls );
		return cls;
		
		
	}
	

	public IFunction getFunctionClass(String name) {

		if( isJavaModule() )
			return getJavaFunctionClass( name );
		
		
		String origName = name ;
		/*
		 * Convert from camelCase to hypen-case
		 */
		
		name = convertCamelCase( name );
		name = fromReserved(name);
		
		
		try {

			Class<?> cls = findClass(name);
			if( cls != null ){
				Constructor<?> constructor = cls.getConstructor();
				if( constructor != null ){
					Object obj = constructor.newInstance();
					if( obj instanceof IFunction ){
						IFunction cmd = (IFunction) obj ;
						
						return cmd;
					}
				}
			}

		} catch (Exception e) {
			;

		}
		
		/*
		 * Try a script
		 */
		InputStream scriptStream = getCommandResource(origName + ".xsh");
		if (scriptStream != null)
			return new ScriptFunctionCommand(name, scriptStream,  this );
		return null;

		
	}

	private IFunction getJavaFunctionClass(final String name)
    {
		
		final Module thisModule = this;
		return new IFunction() {

			@Override
            public String getName()
            {
	           return name ;
            }

			@Override
            public Command getBody()
            {
					return new JavaModuleFunctionCommand(thisModule , name, mJavaClass, mClassLoader);
				
            }
			
			
		} ;
		
		
		
		
		
    }

	private ClassLoader getClassLoader(List<URL> classpath) {
		if (classpath == null || classpath.size() == 0)
			return getClass().getClassLoader();

		return new XClassLoader((URL[]) classpath.toArray(new URL[classpath.size()]), getClass()
				.getClassLoader());

	}

	public String getName() {
		return mName;
	}

	public URL getResource(String res)
	{
		/*
		 * Undocumented: When using a classloader to get a resource, then the
		 * name should NOT begin with a "/"
		 * 
		 */
		if( res.startsWith("/"))
			res = res.substring(1);
		return mClassLoader.getResource(res);
	}
	
	public boolean hasCommand( String name )
	{
	
		try {

			Class<?> cls = findClass(name);
			
			if( cls != null )
				return true ;

		} catch (Exception e) {
			;

		}

		return hasCommandResource(name + ".xsh");
		

	}
	
	public URL getHelpURL()
	{
		return mHelpURL;
	}

	/**
	 * @return the package
	 */
	public String getPackage() {
		return mPackage;
	}
	
	
	
}

//
//
// Copyright (C) 2008-2014 David A. Lee.
//
// The contents of this file are subject to the "Simplified BSD License" (the
// "License");
// you may not use this file except in compliance with the License. You may
// obtain a copy of the
// License at http://www.opensource.org/licenses/bsd-license.php
//
// Software distributed under the License is distributed on an "AS IS" basis,
// WITHOUT WARRANTY OF ANY KIND, either express or implied.
// See the License for the specific language governing rights and limitations
// under the License.
//
// The Original Code is: all this file.
//
// The Initial Developer of the Original Code is David A. Lee
//
// Portions created by (your name) are Copyright (C) (your legal entity). All
// Rights Reserved.
//
// Contributor(s): none.
//
