/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.sh.shell;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;

class Logging {

	static void configureLogger()
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
				
		Logger.getLogger( Shell.class).info("started xmlsh");
	
	}

}
