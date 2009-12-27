/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.sh.shell;

import java.util.Enumeration;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.FileAppender;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;

class Logging {

	
	
	static void configureLogger()
	{
		// Only configure logger if it has not already been configured
		// This avoids adding appenders to embedded invocations of xmlsh
		if( !isLog4JConfigured() )
			configureLogger2();
		
		
	}
	private static void configureLogger2()
	{
		
		
		// First look for a properties file 
		String log4jpath = System.getenv("XLOG4JPATH");
		if( log4jpath != null ){
			try {
				PropertyConfigurator.configure( log4jpath );
				return ;
			} catch( Exception e ) 
			{}
		}

		// If none found log to XLOGFILE
		String filename = System.getenv("XLOGFILE");
		
		// If not found log to XMLSH
		if( filename == null ){
			String xmlsh = System.getenv("XMLSH");
			if( xmlsh != null )
				filename = System.getenv("XMLSH") + "/xmlsh.log" ;
		}
		if( filename == null ){
			String home = System.getProperty("user.home");
			if( home == null )
				home = System.getProperty("user.dir");
			if( home != null )
				filename = home + "/xmlsh.log";
			
		}
		
		
		if( filename != null )
		{
			try {
				BasicConfigurator.configure( 
						new FileAppender( 
								new PatternLayout( "%d %-5r %-5p [%t] %c{2} - %m%n"), 
								filename ,
								true 
						) 
					);
			} catch( Exception e ) {
				
				BasicConfigurator.configure();
				return ;
				
			}
		}
		else
			BasicConfigurator.configure();
				
	
	}

	
	/**
	 * DAL: Ripped code from http://wiki.apache.org/logging-log4j/UsefulCode
	 *  
     * Returns true if it appears that log4j have been previously configured. This code 
     * checks to see if there are any appenders defined for log4j which is the 
     * definitive way to tell if log4j is already initialized 
    */ 
	public static boolean isLog4JConfigured() { 
	    Enumeration<?> appenders = Logger.getRootLogger().getAllAppenders(); 
	    if (appenders.hasMoreElements()) { 
	        return true; 
	    } 
	    else { 
	        Enumeration<?> loggers = LogManager.getCurrentLoggers() ; 
	        while (loggers.hasMoreElements()) { 
	            Logger c = (Logger) loggers.nextElement(); 
	            if (c.getAllAppenders().hasMoreElements()) 
	                return true; 
	        } 
	    } 
	    return false; 
	} 
		
	
	
}
