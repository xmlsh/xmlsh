package org.xmlsh.core.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;

import org.xmlsh.core.InputPort;
import org.xmlsh.util.NullInputStream;
import org.xmlsh.util.NullOutputStream;

public abstract class ShellReader extends Reader {

    protected IShellPrompt mShellPrompt ;
    private char buf[];
    private int  boff = -1;
    private int promptLevel = 0;

    public static class ShellNullReader extends ShellReader {

		public ShellNullReader() {
			super(null);
		}

		@Override
		protected String readLine(String prompt) throws IOException {
			return null;
		}

		@Override
		public InputPort getInputPort() {
				return new StreamInputPort( new NullInputStream(), "");
		}

		@Override
		public OutputPort getOutputPort() {
			return new StreamOutputPort( new NullOutputStream());
		}

		@Override
		public OutputPort getErrorPort() {
			return new StreamOutputPort( new NullOutputStream());
		}
    	
    }

    /*
     * Shell Reader with stdio from supplied streams and Interactive/command IO from supplied in/out 
     * 
     */
    public static class ShellStreamReader extends ShellReader {
        
    	// Command/Interaractive in/out
        private BufferedReader consoleReader ;
        private PrintStream consoleOut;
        private InputStream stdin;
        private OutputStream stdout ;
        private OutputStream stderr ;
        
        ShellStreamReader(InputStream consoleIn , PrintStream consoleOut , 
        		InputStream stdin, OutputStream stdout , OutputStream stderr , IShellPrompt prompt) {
            super(prompt);
            consoleReader = new BufferedReader( new InputStreamReader( consoleIn ));
            this.consoleOut = consoleOut ;
            this.stdin = stdin ;
            this.stdout = stdout ;
            this.stderr = stderr ;
        }

        @Override
        protected String readLine( String prompt ) throws IOException {
            if( prompt != null ){
              consoleOut.print( prompt );
              consoleOut.flush();
            }
            return consoleReader.readLine();
        }
        
        public  InputPort getInputPort()
        {
            return new StreamInputPort( stdin , null , stdin == System.in     );
         }
         
         public OutputPort getOutputPort()
         {
             return new StreamOutputPort(stdout,false,stdout == System.out )   ;
         }
         public OutputPort getErrorPort()
         {
             return new StreamOutputPort(stderr,false,stderr == System.err )  ;
         }

    }
    
    
    /*
     * Shell Reader with stdio from System and Interactive/command IO from supplied in/out 
     * 
     */
    public static class ShellSystemReader extends ShellStreamReader {
         
        
        ShellSystemReader(InputStream consoleIn , PrintStream consoleOut , IShellPrompt prompt) {
        	super(consoleIn, consoleOut , System.in, System.out , System.err , prompt );
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
    
    public static ShellReader newNullReader() {
    	return new ShellNullReader();
    }
    public static ShellReader  newSystemReader( IShellPrompt prompt)
    {
        return new ShellSystemReader( System.in, System.out ,prompt);
    }
    public static ShellReader newStreamReader(InputStream consoleIn , PrintStream consoleOut , 
    		InputStream stdin, OutputStream stdout , OutputStream stderr , IShellPrompt prompt) {
    	return new ShellStreamReader(stdin, consoleOut, stdin, stderr, stderr, prompt);
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

