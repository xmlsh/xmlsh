/**
 * $Id: $
 * $Date: $
 * 
 */

package org.xmlsh.sh.module;

import java.io.FileNotFoundException;
import java.lang.reflect.Constructor;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import org.xmlsh.core.ICommand;
import org.xmlsh.core.IFunctionExpr;
import org.xmlsh.core.ScriptCommand;
import org.xmlsh.core.ScriptCommand.SourceMode;
import org.xmlsh.core.ScriptFunctionCommand;
import org.xmlsh.core.ScriptSource;
import org.xmlsh.core.XCommand;
import org.xmlsh.sh.shell.IFunctionDefiniton;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.JavaUtils;
import org.xmlsh.util.Util;

public class PackageModule extends Module {
	private List<String> mPackages;
	protected String mEncoding;

	protected PackageModule(Shell shell, String name) {
		super(name);
		mEncoding = shell.getInputTextEncoding();

	}

	/*
	 * Constructor for internal modules like xlmsh
	 * These dont get their own thread group
	 */
	protected PackageModule(Shell shell, String name, List<String> packages,
			String helpURL) {
		this(shell, name);
		mEncoding = shell.getInputTextEncoding();
		setPackages(packages);
		setClassLoader(getClassLoader(null));
		// Undocumented - if you use a class loader to find a resource dont start it with "/"
		mHelpURL = getClassLoader().getResource(helpURL.replaceFirst("^/", ""));
	}

	private String convertCamelCase(String name) {
		if (name.indexOf('-') < 0)
			return name;

		String parts[] = name.split("-");
		if (parts.length == 1)
			return name;

		StringBuffer result = new StringBuffer(name.length());

		for (String p : parts) {
			if (p.length() == 0)
				continue;

			if (result.length() == 0)
				result.append(p);
			else {
				result.append(Character.toUpperCase(p.charAt(0)));
				result.append(p.substring(1));
			}

		}

		return result.toString();

	}

	@Override
	public String describe() {
		return getName() + "[ packages " + Util.join(getPackages(), ",") + " ]";
	}

	private String fromReserved(String name) {
		if (JavaUtils.isReserved(name))
			return "_" + name;
		else
			return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xmlsh.sh.shell.IModule#getCommandClass(java.lang.String)
	 */
	@Override
	public ICommand getCommand(String name) throws FileNotFoundException,
			URISyntaxException {

		/*
		 * Convert from hyphen-case to camelCase
		 */

		name = convertCamelCase(name);
		name = fromReserved(name);

		// Store the camel name not the hyphen name
		String origName = name;

		/*
		 * First try to find a class that matches name
		 */

		try {

			// Cached in AbstractModule
			Class<?> cls = findClass(name, getPackages());
			if (cls != null) {
				Constructor<?> constructor = cls.getConstructor();
				if (constructor != null) {
					Object obj = constructor.newInstance();
					if (obj instanceof XCommand) {
						XCommand cmd = (XCommand) obj;
						cmd.setModule(this);
						return cmd;
					} else
						getLogger()
								.warn("Command class found [ {} ] but is not instance of XCommand.",
										cls.getName());
				}
			}

		} catch (Exception e) {
			getLogger().debug("Exception calling constructor for:" + name, e);

		}

		/*
		 * Second
		 * Try a script stored as a resource
		 */

		// mScriptCache caches a Boolean indicating whether the resource is found or not
		// No entry in cache means it has not been tested yet

		// Failures are cached with a null command
		String scriptName = origName + ".xsh";

		URL scriptURL = findResourceInPackages(scriptName, getPackages());
		if (scriptURL != null)

			return new ScriptCommand(new ScriptSource(scriptName, scriptURL,
					mEncoding), SourceMode.RUN, null, this);

		return null;

	}

	/*
	 * Conversts hypen-case to camelCase, also converts from any reserved word
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xmlsh.sh.shell.IModule#getFunctionClass(java.lang.String)
	 */
	@Override
	public IFunctionExpr getFunction(String name) {

		String origName = name;
		/*
		 * Convert from camelCase to hypen-case
		 */

		name = convertCamelCase(name);
		name = fromReserved(name);

		try {
			// Cached in AbstractModule
			Class<?> cls = findClass(name, getPackages());
			if (cls != null) {
				Constructor<?> constructor = cls.getConstructor();
				if (constructor != null) {
					Object obj = constructor.newInstance();
					if (obj instanceof IFunctionExpr)
						return (IFunctionExpr) obj;

					if (obj instanceof IFunctionDefiniton) {
						IFunctionDefiniton cmd = (IFunctionDefiniton) obj;
						return cmd.getFunction();
					}
				}
			}

		} catch (Exception e) {
			;

		}

		/*
		 * Try a script
		 */
		URL scriptURL = findResourceInPackages(origName + ".xsh", getPackages());
		if (scriptURL != null)
			return new ScriptFunctionCommand(name, scriptURL, this);
		return null;

	}

	protected List<String> getPackages() {
		return mPackages;
	}

	protected boolean hasCommandResource(String name) {
		for (String pkg : getPackages()) {
			if (getClassLoader().getResource(toResourceName(name, pkg)) != null)
				return true;
		}
		return false;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xmlsh.sh.shell.IModule#hasCommand(java.lang.String)
	 */
	@Override
	public boolean hasHelp(String name) {

		try {
			// Cached in AbstractModule
			Class<?> cls = findClass(name, getPackages());

			if (cls != null)
				return true;

		} catch (Exception e) {
			;

		}

		return hasCommandResource(name + ".xsh");

	}

	protected void setPackages(List<String> packages) {
		mPackages = packages;
	}

}

/*
 * Copyright (C) 2008-2012 David A. Lee.
 * 
 * The contents of this file are subject to the "Simplified BSD License" (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.opensource.org/licenses/bsd-license.php
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied.
 * See the License for the specific language governing rights and limitations under the License.
 * 
 * The Original Code is: all this file.
 * 
 * The Initial Developer of the Original Code is David A. Lee
 * 
 * Portions created by (your name) are Copyright (C) (your legal entity). All Rights Reserved.
 * 
 * Contributor(s): David A. Lee
 */