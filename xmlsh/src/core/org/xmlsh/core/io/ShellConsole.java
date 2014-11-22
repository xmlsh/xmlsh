package org.xmlsh.core.io;

import java.io.Console;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;

import jline.ConsoleReader;

/*
 * Singleton Console support for all shells
 * Console interface for interactive input , 
 * provides a structured IO as well as stream IO
 * 
 */

public class ShellConsole {
    
    private static volatile ShellConsole _instance = null ;
    private Console sJavaConsole ;               // JRE  6 Console if available 
    private ConsoleReader sJLineConsole ;        // JLine 2 Console Reader if available
    private InputStream   sSystemIn;             // System.in 
    private PrintStream   sSystemOut;            // System.out
    private PrintStream   sSystemErr;            // System.err 
    
    public static ShellConsole getConsole() {
    
        if( _instance == null ){
            synchronized( ShellConsole.class){
                if( _instance == null )
                    _instance = new ShellConsole();
              
            }
        }
        return _instance;
        
    }
        
      

    private ShellConsole() 
    {
        sJavaConsole = System.console();
        sSystemIn = System.in;
        sSystemOut = System.out;
        sSystemErr = System.err;

        if( sSystemOut == null )
            sSystemOut = sSystemErr ;
        if( sSystemErr == null && sSystemOut != null )
            sSystemErr = sSystemOut;
    }
    
    public boolean hasInput() {
        if( sJavaConsole != null )
            return true ;
        if( sJLineConsole != null )
            return true ;
    
        return sSystemIn != null ;
    
    }
    public boolean hasOutput() {
        if( sJavaConsole != null )
            return true ;
        if( sJLineConsole != null )
            return true ;
    
        return sSystemOut != null ;
    
    }
    public boolean hasErr() {
        if( sJavaConsole != null )
            return true ;
        if( sJLineConsole != null )
            return true ;
    
        return sSystemErr != null ;
    }
    
    
    
    
}
