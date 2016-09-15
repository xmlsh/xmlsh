package org.xmlsh.core.io;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.core.InputPort;


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
    public ShellIO(ShellReader reader) {
        mReader = reader;
        mUseConsole = false ;
    }
    
    
    public String readCommandLine(String prompt) throws IOException
    {
        return getReader().readLine(prompt);
    }
    
    
    public InputPort getInputPort()
    {
       return getReader().getInputPort();
    }
    
    public OutputPort getOutuptPort()
    {
        return getReader().getOutputPort();
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
