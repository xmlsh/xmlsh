package org.xmlsh.bsh.module;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.annotations.Command;
import org.xmlsh.annotations.Function;
import org.xmlsh.core.AbstractBuiltinFunction;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.Options;
import org.xmlsh.core.XClassLoader;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.module.ExternalModule;
import org.xmlsh.sh.module.ModuleConfig;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.sh.shell.Shell;

import bsh.Capabilities;
import bsh.EvalError;
import bsh.Interpreter;

@org.xmlsh.annotations.Module
public class BSHModule extends ExternalModule {

     static Logger mLogger = LogManager.getLogger();
	
	public BSHModule(ModuleConfig config, XClassLoader loader) throws CoreException {
		super(config, loader);
	  mLogger.entry(config, loader);
		
	}

	@Override
	public void onInit(Shell shell, List<XValue> args) throws Exception {
		super.onInit(shell, args);
	  mLogger.entry(shell, args);
	}

	@Override
	public void onLoad(Shell shell) {
		super.onLoad(shell);
        mLogger.entry(shell);


	}

	@Command( name="bshui" , names={"ui","gui"} )
	public static class bshui extends XCommand {
	    
		  @Override
        public int run(List<XValue> args) throws Exception {
		      requires( Capabilities.classExists( "bsh.util.Util" ) , "Can't find the BeanShell utilities..." );
              requires( Capabilities.haveSwing() , "Can't find javax.swing package: "
                      +" An AWT based Console is available but not built by default." );
            bsh.util.Util.startSplashScreen();
            try {
                new Interpreter().eval("desktop()");
            } catch ( EvalError e ) {
                error("Couldn't start desktop: ",e);
            }
            return 0;
			  
		  }
	}
	

    
    @Function(name="eval") 
    public static class eval extends AbstractBuiltinFunction
    {

        @Override
        public XValue run(Shell shell, List<XValue> args) throws Exception {

            requires( args.size() > 0 , "eval([interpreter],expr)");
            XValue xv = args.remove(0);
            Interpreter bsh = null;

            if( xv.isInstanceOf(Interpreter.class)){
                requires( args.size() > 0 , "eval([interpreter],expr)");
                bsh = xv.asInstanceOf(Interpreter.class);
                xv = args.remove(0);
            }
            else
                bsh = new Interpreter();
            
            String script = xv.toString();
            return XValue.newXValue( bsh.eval(script));
            
        }
        
    }
	
	@Function(name="interpreter") 
    public static class interpreter extends AbstractBuiltinFunction
    {

        @Override
        public XValue run(Shell shell, List<XValue> args) throws Exception {
            
            return XValue.newXValue((Object) new Interpreter());
            
        }
	    
    }


}
