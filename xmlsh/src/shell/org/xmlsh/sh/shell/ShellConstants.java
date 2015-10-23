/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.sh.shell;

import java.net.URL;

import org.xmlsh.util.Util;

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
    public static final String ENV_XMLSH = "XMLSH";
    public static final String ENV_XMLSH_HOME = "XMLSH_HOME";

	public static final String XLOGFILE = "XLOGFILE";
	public static final String XLOG4JPATH = "XLOG4JPATH";
	public static final String XMLSHRC = "XMLSHRC";
	public static final String HOME = "HOME";
	public static final String PATH = "PATH";
	public static final String ENV_XPATH = "XPATH";
	public static final String ENV_XMODPATH = "XMODPATH";
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
		// Force xerces for XML so saxon.jar doesnt get picked
		try {
		    System.setProperty("javax.xml.parsers.SAXParserFactory","org.apache.xerces.jaxp.SAXParserFactoryImpl");
		} catch( Throwable e)
		{
		    // logging not yet initialized
		}
		
		
		String logging = Shell.getSystemProperty(ShellConstants.ENV_XDISABLE_LOGGING);
		Logging.configureLogger(Util.parseBoolean(logging,false) );

		Shell.postInit("Logging Initialized");

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


    public static final char kDOT_CHAR = '.';

    public static final String XSH_EXTENSION = ".xsh";

	public static final String kENCODING_UTF_8 = "UTF-8";

	static final String kXMLSH_PROP_PREFIX = "xmlsh.";


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