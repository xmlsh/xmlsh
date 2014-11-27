package org.xmlsh.core.io;

import java.io.Console;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.core.InputPort;

import jline.ConsoleReader;

/*
 * Shell IO interface, may be shared across shells and threads.
 * May refer to ShellConsole, other streams, or nothing at all
 * 
 */

public class ShellIO {
    
    private boolean mUseConsole ;
    

    static Logger mLogger = LogManager.getLogger();
    private IShellPrompt mPrompt = null ;


    private ShellReader mReader;
    
    
    

    public ShellReader getReader(){
        if( mReader == null )
            // TODO: Dont allocatre ShellConsole if not needed 
          setReader(ShellConsole.getConsole().newReader( mUseConsole , mPrompt )); // may return null if no console available
        return mReader;
    }
    
    

    public ShellIO(boolean bUseConsole ) {
        mUseConsole = bUseConsole ;
    }
    
    
    public String readCommandLine( int promptLevel ) throws IOException
    {
        return getReader().readLine(promptLevel);
        
        
    }
    
    public void   writeOutputLine( String line ) {
        
        
    }
    public void   writeErrorLine( String line ) {
    }
    
    
    public InputPort getInputPort()
    {
       return getReader().getInputPort();
    }
    
    public OutputPort getOutuptPort()
    {
        return getReader().getOutuptPort();
    }
    public OutputPort getErrorPort()
    {
        return getReader().getErrorPort();
    }



    public IShellPrompt getPrompt() {
        return mPrompt;
    }



    public void setPrompt(IShellPrompt prompt) {
        mPrompt = prompt;
    }




    private void setReader(ShellReader reader) {
        mReader = reader;
    }

    
    
}
