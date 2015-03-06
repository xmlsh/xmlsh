package org.xmlsh.modules.text;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.InputPort;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.Options.OptionDefs;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.UnimplementedException;
import org.xmlsh.core.UnknownOption;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.core.io.OutputPort;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.util.Util;

import com.sun.javafx.scene.EnteredExitedHandler;


abstract public class TextCommand extends XCommand {
    protected Logger mLogger = LogManager.getLogger( this.getClass() );
	
	@Override
	public int run(List<XValue> args) throws Exception {
		mLogger.entry(args);
		parseOpts(args);
		process();
		return mLogger.exit(0);
	}

	protected  InputPort mInput ;
	protected  OutputPort mOutput;
	protected  Options mOptions;
	private static OptionDefs sOptionDefs =  new OptionDefs( "i=input:,o=output:").withOptions(SerializeOpts.getOptionDefs());
	
	protected abstract void process() throws Exception;
	
	// Override 
	protected OptionDefs getOptionDefs()
	{
		return sOptionDefs;
	}
	
	protected Options getOptions() {
		return mOptions;
	}

    protected List<XValue>  parseOpts(List<XValue> args) throws UnexpectedException, InvalidArgumentException, UnknownOption, IOException, CoreException
    {
	    mOptions  = new Options(getOptionDefs());
	    mOptions.parse(args);

	  
	    mInput = mOptions.hasOpt("i") ? 
	            getShell().getEnv().getInput(mOptions.getOptValue("i")) : getShell().getEnv().getStdin() ;

	    mOutput = mOptions.hasOpt("o") ? 
	            getShell().getEnv().getOutput(mOptions.getOptValue("o"),false) : getShell().getEnv().getStdout() ;
    
	    	    return  mOptions.getRemainingArgs();
	    	    
    }


    protected void  processStream( ) throws UnsupportedEncodingException, IOException, CoreException
	{
    	mLogger.entry();
        try ( Reader r = mInput.asReader(getSerializeOpts()) ;
            PrintWriter w = mOutput.asPrintWriter(getSerializeOpts()) ){
        	processStream( r , w );
        
        } 
    	mLogger.exit();

	}
    

	protected void processStream(Reader r, PrintWriter w) throws IOException, UnimplementedException { 
		throw new UnimplementedException( "processStream() not implemented");
		
	}

    

}
