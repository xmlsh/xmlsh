package org.xmlsh.sh.module;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.FunctionCommand;
import org.xmlsh.core.ICommand;
import org.xmlsh.core.IXFunction;
import org.xmlsh.core.ScriptCommand;
import org.xmlsh.core.ScriptCommand.SourceMode;
import org.xmlsh.core.ScriptSource;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.IFunctionDefiniton;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.sh.shell.Shell;

public class ScriptModule extends Module {


	protected final static Logger mLogger = LogManager.getLogger();
	
	static ModuleConfig getConfiguration( Shell shell, ScriptSource script,
			List<URL> classpath ){

	    mLogger.entry(shell,script,classpath);
		ModuleConfig conf =  new ModuleConfig( "script", script.getName() ,  null , script.getURL() ,  classpath, null, shell.getSerializeOpts() );
		conf.setModuleScript(script);
        return conf;

	}
	protected ScriptModule(Shell shell, ModuleConfig config ) throws IOException, CoreException {
		super( config , shell.getClassLoader(config.getClassPath()));
	}
	
	@Override
	public String describe() {
		return getName() + " [ at " + getConfig().getModuleScript().getLocation().toString() + "]";
	}

	protected void finalize() {
		mStaticContext = null ;
	}

	@Override
	public ICommand getCommand(String name) {
		if (mStaticContext == null)
			return null;

		IFunctionDefiniton func = mStaticContext.getFunction(name);
		if (func != null)
			return new FunctionCommand(this, func.getName(), func.getBody(),
					null);
		return null;

	}

	@Override
	public IXFunction getFunction(String name) {
		if (mStaticContext == null)
			return null;

		IFunctionDefiniton func = mStaticContext.getFunction(name);
		if (func != null)
			return func.getFunction();
		return null;

	}

	@Override
	public boolean hasHelp(String name) {
		return false;
	}

	@Override
  public void onInit(Shell shell, List<XValue> args) throws Exception
  {

	super.onInit(shell, args);
    try ( Shell sh = shell.clone() ) {
      if( args != null )
         sh.setArgs(args);
     Module hThis = this ;
    	 ScriptCommand cmd = new ScriptCommand(
        		 // Holds a refernce to module within cmd 
    		  getConfig().getModuleScript() ,  SourceMode.IMPORT, shell.getLocation() , hThis  ) ;
        	 if(  cmd.run(sh, getName(), args) != 0 )
		        shell.printErr("Failed to init script:" + getName() );
		      else {

				   mLogger.debug("Merging script context into script module {} context " , this , mStaticContext );
		    	  // Extracts a clone of the this modules shell context
		    	  mStaticContext = sh.getExportedContext();
		    	  
		    	 
		      }
        	 
		mLogger.exit();
    }
  }

	@Override
	public void onLoad(Shell shell) {
		super.onLoad(shell);
		if ( getConfig().getModuleScript() == null) {
			shell.printErr("script not found: " + getName());
			return;
		}

	}
	
	@Override
	public URL findResource(String res) {
		mLogger.trace("TODO: maybe look in class's package for resource {}",res);
		return(null);
	}



}
