package org.xmlsh.core.io;

import java.io.Console;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;

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
    
    private ShellConsole mConsole ; // Console object 
    
    private Reader mIn;
    private Writer mOut;
    private Writer mErr;
    
    
    


    public ShellIO(boolean interactive ) {
      if( interactive ){
          mConsole =  ShellConsole.getConsole(); // may return null if no console available
      }    
      else
          mConsole = null ;
      
    }
    
    
    
    
    
    
    
    
}
