/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.sh.shell;

import org.xmlsh.util.Util;

import java.net.URL;

public class ShellConstants
{

	public static final String ARG_SEPARATOR = " ";

	public static final String PROP_USER_HOME = "user.home";

	public static final String ENV_HOME = "HOME";
	public static final String PROP_JAVA_IO_TMPDIR = "java.io.tmpdir";
	public static final String ENV_TMPDIR = "TMPDIR";
	public static final String VAR_RANDOM64 = "RANDOM64";
	public static final String VAR_RANDOM32 = "RANDOM32";
	public static final String VAR_RANDOM = "RANDOM";
	public static final String PROP_USER_DIR = "user.dir";
	public static final String ENV_XDISABLE_LOGGING = "XDISABLE_LOGGING";
	/*
	 * Predefined env and shell variable names
	 */
	public static final String XLOGFILE = "XLOGFILE";
	public static final String XLOG4JPATH = "XLOG4JPATH";
	public static final String XMLSHRC = "XMLSHRC";
	public static final String HOME = "HOME";
	public static final String PATH = "PATH";
	public static final String XPATH = "XPATH";
	public static final String XMODPATH = "XMODPATH";
	public static final String PWD = "PWD";
	public static final String PS1 = "PS1";
	public static final String PS2 = "PS2";
	
	
	/**
	 * Must call initialize atleast once, protects against multiple initializations 
	 */
	public	static	void 	initialize()
	{

		if( Shell.bInitialized )
			return ;

		String logging = System.getenv(ShellConstants.ENV_XDISABLE_LOGGING);
		Logging.configureLogger(Util.parseBoolean(logging) );

		Shell.mLogger.info("xmlsh initialize");

		/*
		 * Workaround a saxon bug - pre-initialize processor
		 */
		// getProcessor();


		// Can only be called once per process
		try {
			URL.setURLStreamHandlerFactory(new ShellURLFactory() );

		} 
		catch( Error e )
		{
			// mLogger.debug("Exception trying to seURLStreamHandlerFactory" , e );
		}


		Shell.mSavedSystemProperties = System.getProperties();
		SystemEnvironment.getInstance().setProperty(ShellConstants.PROP_USER_DIR, System.getProperty(ShellConstants.PROP_USER_DIR));
		System.setProperties( new SystemProperties(System.getProperties()));
		// PropertyConfigurator.configure(Shell.class.getResource("log4j.properties"));


	}

}



/*
 * Copyright (C) 2008-2012 David A. Lee.
 * 
 * The contents of this file are subject to the "Simplified BSD License" (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.opensource.org/licenses/bsd-license.php 

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
 * 
 */