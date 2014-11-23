package org.xmlsh.core.io;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.lang.reflect.Constructor;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.util.Util;

import jline.ConsoleReader;

/*
 * Singleton Console support for all shells
 * Console interface for interactive input , 
 * provides a structured IO as well as stream IO
 * 
 */

public class ShellConsole {
    
    static Logger mLogger = LogManager.getLogger();
    
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
    
    public static boolean hasConsole() {
        return mLogger.exit(getConsole().sJavaConsole != null) ;
    }
        

    private ShellConsole() 
    {
        mLogger.entry();
        
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

    private abstract class ShellReader extends Reader {

        private String ps1;
        private String ps2;
        private String prompt;
        private char buf[];
        private int  boff = -1;
        
        ShellReader( String ps1, String ps2 ){
            this.ps1 = ps1 ;
            this.ps2 = ps2 ;
        }
        
        
        @Override
        public void reset() throws IOException {
            prompt = ps1;
            // empty buf ?
        }
        
        
        @Override
        public int read(char[] cbuf, int coff, int len) throws IOException {

            mLogger.entry(cbuf, coff, len);
            
            assert( len >0 );
            if( buf == null ){
               String line = readLine(prompt);
               if( line == null )
                   return -1;
               buf = line.toCharArray();
               boff =0 ;
               if( ps2 != null )
                   prompt = ps2 ;
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
            return mLogger.exit(clen);
        
        }


        @Override
        public void close() throws IOException {

            mLogger.entry();
            // just toss input
            buf = null ;
            boff = -1;
            
        }
        protected abstract String readLine(String prompt) throws IOException;

        
    }
    
    private class ShellConsoleReader extends ShellReader {

        ShellConsoleReader(String ps1, String ps2) {
            super(ps1,ps2);
        }

        @Override
        protected String readLine(String prompt) {
            return sJavaConsole.readLine(prompt, "");
        }
    }
    
    
    private class ShellSystemReader extends ShellReader {
        
        BufferedReader reader ;
        ShellSystemReader(String ps1,String ps2) {
            super(ps1,ps2);
            reader = new BufferedReader( new InputStreamReader( sSystemIn ));
        }

        @Override
        protected String readLine(String prompt) throws IOException {
            sSystemOut.print(prompt);
            sSystemOut.flush();
            return reader.readLine();
        }
 
    }
    
    
    public static Reader newConsoleReader(String ps1, String ps2) { 
        
        mLogger.entry();
        
        return mLogger.exit(getConsole().reader(ps1,ps2));
        
    }

    private Reader reader(String ps1, String ps2) {
    
        mLogger.entry();
        if( sJavaConsole != null )
            return new ShellConsoleReader(ps1,ps2) ;
        else {
            return new ShellSystemReader(ps1,ps2);
        }
    
    }

/*
 *      Class<?> consoleReaderClass = Class
                            .forName("jline.ConsoleReader");

                    if (consoleReaderClass != null) {
                        Class<?> consoleInputClass = Class
                                .forName("jline.ConsoleReaderInputStream");
                        if (consoleInputClass != null) {
                            // ConsoleReader jline = new ConsoleReader();
                            Object jline = consoleReaderClass.newInstance();

                            Constructor<?> constructor = consoleInputClass
                                    .getConstructor(consoleReaderClass);
                            // mCommandInput = new
                            // ConsoleReaderInputStream(jline);

                        //  if (constructor != null) {
                        //      mCommandInput = (InputStream) constructor
                        //              .newInstance(jline);
                                // System.err.println("using jline");
                            }

                        }
                        
                            
 */
    
    
    
}
