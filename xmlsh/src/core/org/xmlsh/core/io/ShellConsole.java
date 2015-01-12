package org.xmlsh.core.io;

import static org.xmlsh.util.Util.stringConcat;

import java.io.Console;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;

import jline.Terminal;
import jline.TerminalFactory;
import jline.console.ConsoleReader;
import jline.Logger.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.core.InputPort;
import org.xmlsh.util.FileUtils;
import org.xmlsh.util.Util;

/*
 * Singleton Console support for all shells
 * Console interface for interactive input ,
 * provides a structured IO as well as stream IO
 */

public class ShellConsole {
    static Logger mLogger = LogManager.getLogger();

    private static class JLineLogger implements jline.Logger {
        static org.apache.logging.log4j.Level[] levels = {
            org.apache.logging.log4j.Level.TRACE ,
            org.apache.logging.log4j.Level.DEBUG ,
            org.apache.logging.log4j.Level.INFO,
            org.apache.logging.log4j.Level.WARN,
            org.apache.logging.log4j.Level.ERROR
        };

        //Sstatic Logger mLogger = LogManager.getLogger("jline.internal.Log");
        @Override
        public void log( jline.Logger.Level level, Object... messages) {

            org.apache.logging.log4j.Level l4l = levels[level.ordinal()] ;
            if( !mLogger.isEnabled(l4l))
                return ;
     
            if( messages.length > 0 && messages[messages.length-1] instanceof Throwable ){
                Throwable t = (Throwable) messages[messages.length-1];
                messages = Arrays.copyOf(messages, messages.length-1);
                if( messages.length > 0)
                  mLogger.log( l4l, Util.stringJoin("{}"," ",messages.length) , messages , t );
                else
                  mLogger.log(l4l,t);
           
            } else
                mLogger.log(l4l, Util.stringJoin("{}"," ",messages.length) , messages);
        }
        
    }
    private static volatile ShellConsole _instance = null;
    private Console sJavaConsole;               // JRE 6 Console if available
    private Terminal sJLineTerminal;        // JLine 2 Console Reader if available
    private InputStream sSystemIn;             // System.in
    private PrintStream sSystemOut;            // System.out
    private PrintStream sSystemErr;            // System.err

    private String sEncoding;
    private ConsoleReader jJLineConsole;

    /*
     * private Reader mReader ;
     * 
     * 
     * public ShellReader getReader() {
     * mLogger.entry();
     * if( sJavaConsole != null )
     * return new ShellConsoleReader(ps1,ps2) ;
     * else {
     * return new ShellSystemReader(ps1,ps2);
     * }
     * 
     * }
     */

    public static ShellConsole getConsole() {

        mLogger.entry();
        if (_instance == null) {
            synchronized (ShellConsole.class) {
                if (_instance == null)
                    _instance = new ShellConsole();
            }
        }
        return _instance;
    }

    public static boolean hasConsole() {
        return mLogger.exit(getConsole().sJavaConsole != null);
    }

    private ShellConsole()
    {
        mLogger.entry();
        sEncoding = FileUtils.getSystemTextEncoding();
        sJavaConsole = System.console();
        // If no console dont use jLine for now
        // cygwin note:
        
        // if running cygwin under cmd.exe then use do NOT use jline
        //    Java does not detect a Console and TERM=cygwin
        // if running cygwin under mintty then do USE jline
        //    Java does detect a console and TERM=xterm 
        

        
        String  sUseJLine = System.getProperty("xmlsh.jline");
        boolean useJLine = 
                (sUseJLine != null ? Util.parseBoolean(sUseJLine) : 
                (sJavaConsole != null ));
        
        mLogger.debug("Has Console: {} xmlsh.jline: {} useJLine: {}",
            sJavaConsole != null ,
            sUseJLine,
            useJLine);
                
        if ( useJLine ){
            mLogger.debug("Setting jline logger");
            jline.internal.Log.setLogger( new JLineLogger() );
              sJLineTerminal = TerminalFactory.get();
            jline.internal.Log.trace("Trace event from xmlsh");
        }
        if (sJLineTerminal != null) {
            mLogger.trace("Using JLine Terminal");
            try {
                jJLineConsole = new ConsoleReader("xmlsh", System.in,
                        System.out, sJLineTerminal, sEncoding );
                mLogger.trace("Using jline ConsoleReader");
            } catch (IOException e) {
                mLogger.catching(e);
                mLogger.debug("Failed to load jline console");
                jJLineConsole = null;
                sJLineTerminal = null;
            }
        }

        sSystemIn = System.in;
        sSystemOut = System.out;
        sSystemErr = System.err;

        if (sSystemOut == null)
            sSystemOut = sSystemErr;
        if (sSystemErr == null && sSystemOut != null)
            sSystemErr = sSystemOut;
    }

    public boolean hasInput() {
        if (sJavaConsole != null)
            return true;
        if (jJLineConsole != null)
            return true;

        return sSystemIn != null;

    }

    public boolean hasOutput() {
        if (sJavaConsole != null)
            return true;
        if (jJLineConsole != null)
            return true;

        return sSystemOut != null;

    }

    public boolean hasErr() {
        if (sJavaConsole != null)
            return true;
        if (jJLineConsole != null)
            return true;
        return sSystemErr != null;
    }

    private class ShellConsoleReader extends ShellReader {

        ShellConsoleReader(IShellPrompt prompt) {
            super(prompt);
            mLogger.entry();
        }

        @Override
        protected String readLine(String prompt) {

            return sJavaConsole.readLine(prompt);
        }

        public InputPort getInputPort()
        {
            return new StreamInputPort(sSystemIn, null, true);
        }

        public OutputPort getOutputPort()
        {
            return new StreamOutputPort(sSystemOut, false, true);
        }

        public OutputPort getErrorPort()
        {
            return new StreamOutputPort(sSystemErr, false, true);
        }

    }

    private class ShellJLineReader extends ShellReader {

        ShellJLineReader(IShellPrompt prompt) {
            super(prompt);
            mLogger.entry(prompt);
        }

        @Override
        protected String readLine(String prompt) throws IOException {

            return jJLineConsole.readLine(prompt);
        }

        public InputPort getInputPort()
        {
            mLogger.entry();

            // TODO: Make a ReaderInputPort
            InputStream in = new InputStream() {
                private byte[] line = null;
                private int pos = 0; // set to -1 for EOF

                @Override
                public int read() throws IOException {
                    if( pos  < 0 )
                        return -1;
                    if( line == null || pos >= line.length ){
                        pos = 0;
                        line = null;
                        String sline = jJLineConsole.readLine("");
                        if( sline == null )
                           return (pos = -1) ;
                    
                        if( sline.isEmpty() )
                            line = Util.mNewLineBytes;
                        else
                            line = (sline+Util.getNewlineString()).getBytes(sEncoding);
                    }
                    assert( line != null );
                    return line[pos++];
                }
            };

            return new StreamInputPort(in, null, true);
        }

        @Override
        public OutputPort getOutputPort()
        {
            OutputStream out = new OutputStream() {

                @Override
                public void write(int b) throws IOException {
                    String s = String.valueOf((char) b); // horrible
                    jJLineConsole.print(s);

                }

                @Override
                public void write(byte[] b, int off, int len)
                        throws IOException {
                    jJLineConsole.print( new String(b,off,len,sEncoding));
                }

                @Override
                public void flush() throws IOException {
                    jJLineConsole.flush();
                }

                @Override
                public void close() throws IOException {
                    flush();
                    super.close();
                }
            };

            return new StreamOutputPort(out, false, true);
        }

        @Override
        public OutputPort getErrorPort()
        {
            return getOutputPort();
        }

    }

    public ShellReader newReader(boolean bUseConsole, IShellPrompt prompt) {

        mLogger.entry();
        if (this.jJLineConsole != null){
            mLogger.debug("Using ShellJLineReader");
            return new ShellJLineReader(prompt);
        }
        if (sJavaConsole != null){
            mLogger.debug("Using ShellConsoleReader");
            return new ShellConsoleReader(prompt);
        }
        else {
                mLogger.debug("Using ShellSystemReader");

            return new ShellReader.ShellSystemReader(sSystemIn, sSystemOut,
                    prompt);
        }

    }

}
