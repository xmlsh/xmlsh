/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.sh.shell;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.ICommand;
import org.xmlsh.core.IFunction;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Path;
import org.xmlsh.core.ScriptCommand;
import org.xmlsh.core.ScriptFunctionCommand;
import org.xmlsh.core.XClassLoader;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.util.Util;

public class Module {
	private String mName;
	private String mPrefix; // may be null
	private String mPackage; // may NOT be null
	private ClassLoader mClassLoader; // Classloader for this module
	private	 URL mHelpURL = null ; 
	
	private		HashMap<String , Class<?>>		mClassCache = new HashMap<String,Class<?>>();
	private		HashMap<String , Boolean>	mScriptCache = new HashMap<String,Boolean>();
	
	

	/*
	 * Constructor for internal modules like xlmsh
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
			if (nameuri.endsWith(".xml")) {
				configURL = shell.getURL(nameuri);

			} else {

				Path path = shell.getPath("XMODPATH", true );
				File modDir = path.getFirstFileInPath(shell,nameuri);
				if (modDir == null)
					throw new InvalidArgumentException("Cannot find module directory for : " + nameuri);

				File config = new File(modDir, "module.xml");
				if (!config.exists())
					throw new InvalidArgumentException("Cannot find module.xml in directory : " + modDir.getAbsolutePath() );
				configURL = config.toURI().toURL();
			}

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

			mClassLoader = getClassLoader(classpath);
			mHelpURL = mClassLoader.getResource(toResourceName("commands.xml"));
			
			
			

		} 
		catch( CoreException e )
		{
			throw e ;
			
		}
		
		catch (Exception e) {
			throw new CoreException(e);
		}

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
			;

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
	
	/*
	 * Conversts hypen-case to camelCase
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

		String origName = name ;
		/*
		 * Convert from camelCase to hypen-case
		 */
		
		name = convertCamelCase( name );
		
		
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
	
	
	
}

//
//
// Copyright (C) 2008,2009,2010,2011 David A. Lee.
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
