/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.sh.shell;

import static org.xmlsh.util.Util.formatMessage;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.xmlsh.util.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;

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
    LoggerContext ctx = (LoggerContext)LogManager.getContext(false);
    Configuration conf = ctx.getConfiguration();
    for(  LoggerConfig logger :conf.getLoggers().values()  ) {
      if( logger.getName().startsWith("xmlsh") )
        logger.setLevel( Level.OFF );
    }
	
	}



}
