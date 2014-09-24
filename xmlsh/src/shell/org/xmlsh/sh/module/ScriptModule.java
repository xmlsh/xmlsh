package org.xmlsh.sh.module;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.FunctionCommand;
import org.xmlsh.core.ICommand;
import org.xmlsh.core.IFunctionExpr;
import org.xmlsh.core.ScriptCommand;
import org.xmlsh.core.ScriptCommand.SourceMode;
import org.xmlsh.core.ScriptSource;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.IFunctionDefiniton;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.sh.shell.StaticContext;

public class ScriptModule extends Module {

	private ScriptSource mScript;
	private StaticContext mStaticContext = null;
	protected final static Logger mLogger = LogManager.getLogger();
	
	protected ScriptModule(Shell shell, ScriptSource script, String nameuri) throws IOException, CoreException {
		super( new ModuleConfig( nameuri , null ,  shell.getSerializeOpts()));
		mScript = script;

	}

	@Override
	public String describe() {
		return getName() + " [ at " + mScript.getLocation().toString() + "]";
	}

	protected void finalize() {
		mScript = null ;
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
	public IFunctionExpr getFunction(String name) {
		if (mStaticContext == null)
			return null;

		IFunctionDefiniton func = mStaticContext.getFunction(name);
		if (func != null)
			return func.getFunction();
		return null;

	}

	@Override
	public StaticContext getStaticContext() {
		return mStaticContext;
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
    		  mScript ,  SourceMode.IMPORT, shell.getLocation() , hThis  ) ;
		     // Should addRef the module in the shell ...  
        	 if(  cmd.run(sh, getName(), args) != 0 )
		        shell.printErr("Failed to init script:" + getName() );
		      else {

		    	  // Extracts a clone of the this modules shell context
		    	  mStaticContext = sh.getExportedContext();
		    	  
				   mLogger.debug("Merging script context into script module {} context " , this , mStaticContext );
		    	 // Detach the module from the shell so it wont get destroyed
		    	 mLogger.trace("Adding a reference to this so it wont get closed {} " , hThis );
		    	 
		      }
        	 
		mLogger.exit();
    }
  }

	@Override
	public void onLoad(Shell shell) {
		super.onLoad(shell);
		if (mScript == null) {
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
