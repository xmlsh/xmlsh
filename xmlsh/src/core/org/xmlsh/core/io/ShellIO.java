package org.xmlsh.core.io;

import java.io.Console;
import java.io.InputStream;
import java.io.PrintWriter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jline.ConsoleReader;

/*
 * Shell IO interface, may be shared across shells and threads.
 * May refer to ShellConsole, other streams, or nothing at all
 * 
 */

public class ShellIO {
    
    static Logger mLogger = LogManager.getLogger();
    
    private ShellConsole mConsole ; // Console if available
    
    
    
    public ShellIO() {
        this( true );
    }



    public ShellIO(boolean bTryConsole ) {
      if( bTryConsole )
          mConsole =  ShellConsole.getConsole(); // may return null if no console available
      else
          mConsole = null ;

      
    }
    
    
    
}
