/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.sh.shell;

import java.io.File;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.LoggerConfig;

class Logging {

  static {
    
    String log4jpath = System.getenv(ShellConstants.XLOG4JPATH);
    if( log4jpath != null  ) {
      File f = new File(log4jpath ) ;
      if( f.isFile() && f.canRead() ) {
        System.setProperty(ConfigurationFactory.CONFIGURATION_FILE_PROPERTY, log4jpath);
      }
    }
    
    // XLOGFILE and HOME are handled by the log4j config itself
    
  }


	static void configureLogger(boolean bDisabled)
	{
		// Only configure logger if it has not already been configured
		// This avoids adding appenders to embedded invocations of xmlsh
		if( bDisabled )
		  disableLogging();


	}

	public static void disableLogging()
	{
		@SuppressWarnings("unchecked")
    org.apache.logging.log4j.core.LoggerContext ctx = (org.apache.logging.log4j.core.LoggerContext)LogManager.getContext(false);
    Configuration conf = ctx.getConfiguration();
    for(  LoggerConfig logger :conf.getLoggers().values()  ) {
      if( logger.getName().startsWith("xmlsh") )
        logger.setLevel( Level.OFF );
    }
	
	}



}
