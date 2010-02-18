/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.sh.shell;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.ICommand;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Path;
import org.xmlsh.core.ScriptCommand;
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

	/*
	 * Constructor for internal modules like xlmsh
	 */
	public Module(String prefix, String name, String pkg, String helpURL) {
		mName = name;
		mPrefix = prefix;
		mPackage = pkg;
		mClassLoader = getClassLoader(null);
		mHelpURL = mClassLoader.getResource(helpURL) ;
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
					throw new InvalidArgumentException("Cannot find module directory: " + mName);

				File config = new File(modDir, "module.xml");
				if (!config.exists())
					throw new InvalidArgumentException("Cannot find module.xml: " + mName);
				configURL = config.toURI().toURL();
			}

			configNode = Util.asXdmNode(configURL);

			List<URL> classpath = new ArrayList<URL>();

			XValue xv = new XValue(configNode);
			mPackage = xv.xpath("/module/@package/string()").toString();
			mName = xv.xpath("/module/@name/string()").toString();

			for (XdmItem item : xv.xpath("/module/classpath/file").asXdmValue()) {
				if (item instanceof XdmNode) {
					String file = ((XdmNode) item).getAttributeValue(new QName("url"));
					URL classurl = new URL(configURL, file);
					classpath.add(classurl);

				}

			}

			mClassLoader = getClassLoader(classpath);
			mHelpURL = mClassLoader.getResource(toResourceName("commands.xml"));
			
			
			

		} catch (Exception e) {
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
		String resource = toResourceName(name);
		InputStream is = mClassLoader.getResourceAsStream(resource);

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

		ClassLoader cl = mClassLoader;

		try {

			Class<?> cls = Class.forName(mPackage + "." + name, true, cl);
			XCommand cmd = (XCommand) cls.newInstance();
			cmd.setModule(this);
			return cmd;

		} catch (Exception e) {
			;

		}

		/*
		 * Try a script
		 */
		InputStream scriptStream = getCommandResource(name + ".xsh");
		if (scriptStream != null)
			return new ScriptCommand(name, scriptStream, false, this);
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
		ClassLoader cl = mClassLoader;

		try {

			Class<?> cls = Class.forName(mPackage + "." + name, true, cl);
			
			return cls != null ;

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
// Copyright (C) 2008,2009 David A. Lee.
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
