package org.xmlsh.core.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;

import org.xmlsh.core.InputPort;

abstract class ShellReader extends Reader {

    protected IShellPrompt mShellPrompt ;
    private char buf[];
    private int  boff = -1;
    private int promptLevel = 0;

    
    
    public static class ShellSystemReader extends ShellReader {
        
        private BufferedReader reader ;
        private PrintStream sSystemOut;
        private InputStream sSystemIn;
        
        ShellSystemReader(InputStream sysIn , PrintStream out , IShellPrompt prompt) {
            super(prompt);
            reader = new BufferedReader( new InputStreamReader( sSystemIn  = sysIn ));
            sSystemOut = out ;
        }

        @Override
        protected String readLine( String prompt ) throws IOException {
            if( prompt != null ){
              sSystemOut.print( prompt );
              sSystemOut.flush();
            }
            return reader.readLine();
        }
        
         public  InputPort getInputPort()
        {
            return new StreamInputPort( System.in , null , true    );
         }
         
         public OutputPort getOutputPort()
         {
             return new StreamOutputPort(System.out,false,true)   ;
         }
         public OutputPort getErrorPort()
         {
             return new StreamOutputPort(System.err,false,true)  ;
         }
         
         
         

    }
    protected String getPrompt() {
        return getPrompt(promptLevel);
    }
        
    private String getPrompt(int level) {
        if( mShellPrompt != null && promptLevel >= 0 )
            return mShellPrompt.getPrompt(level);
        else
            return null;
    }
    
    
    public static ShellReader  newSystemReader( IShellPrompt prompt)
    {
        return new ShellSystemReader( System.in, System.out ,prompt);
    }
    
    
    public ShellReader(IShellPrompt prompt) {
        mShellPrompt = prompt;
    }

    @Override
    public void reset() throws IOException {
        promptLevel = 0;
    }
    
    
    @Override
    public int read(char[] cbuf, int coff, int len) throws IOException {

        ShellConsole.mLogger.entry(cbuf, coff, len);
        
        assert( len >0 );
        if( buf == null ){
           String line = readLine();
           if( line == null )
               return -1;
           buf = line.toCharArray();
           boff =0 ;
           promptLevel = 1;
        }
        
        int blen = buf.length - boff ;
        int clen = Math.min(blen, len);
        if( blen > 0){
            if( clen > 0 ){
               System.arraycopy(buf, boff, cbuf, coff , clen );
               boff += clen ;
               coff += clen ;
               len -= clen ;
               blen -= clen ;
               assert( len >= 0);
               assert( blen >=0 );
            }
            // copy NL if reached end of line
        }
        if( blen == 0 && len > 0 ){
            cbuf[coff] = '\n';
            buf = null ;
            boff = -1;
            clen++;
        }
        return ShellConsole.mLogger.exit(clen);
    
    }


    @Override
    public void close() throws IOException {

        ShellConsole.mLogger.entry();
        // just toss input
        buf = null ;
        boff = -1;
        
    }
    protected abstract String readLine(String prompt) throws IOException;
    protected String readLine() throws IOException {
        return readLine(getPrompt());
    }
    
    // InputPort is for Non prompting data reads 
    public abstract InputPort getInputPort();
    public abstract OutputPort getOutputPort();
    public abstract OutputPort getErrorPort();
    
}

