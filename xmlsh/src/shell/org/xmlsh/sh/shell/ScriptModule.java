package org.xmlsh.sh.shell;

import java.io.IOException;
import java.util.List;

import org.xmlsh.core.CoreException;
import org.xmlsh.core.FunctionCommand;
import org.xmlsh.core.ICommand;
import org.xmlsh.core.IFunction;
import org.xmlsh.core.IFunctionDecl;
import org.xmlsh.core.ScriptCommand;
import org.xmlsh.core.ScriptSource;
import org.xmlsh.core.XValue;
import org.xmlsh.core.ScriptCommand.SourceMode;

public class ScriptModule extends AbstractModule
{

  private ScriptSource mScript ; 
  private Shell.StaticContext mStaticContext;
  
  
  protected ScriptModule(Shell shell, ScriptSource script , String prefix, String nameuri ) throws IOException, CoreException
  {
    super(shell,nameuri , prefix);

    mScript = script ;
    mClassLoader = getClassLoader(null);

  }
  
  
  @Override
  public void onLoad(Shell shell)
  { 
	  super.onLoad(shell);
    if( mScript == null ){
      shell.printErr("script not found: " + getName() );
      return;
    }
    

  }

  @Override
  public void onInit(Shell shell, List<XValue> args) throws Exception
  {

	super.onInit(shell, args);
    try ( Shell sh = shell.clone() ) {
      if( args != null )
        sh.setArgs(args);
      
      ScriptCommand cmd = new ScriptCommand( 
    		  SourceMode.IMPORT ,  shell.getLocation(), shell.getModule() , mScript 
    		  );
      
      if(  cmd.run(sh, getName(), args) != 0 )
        shell.printErr("Failed to init script:" + getName() );
      else {
         importContext( sh );

      }
    }
  }
  
  
  private void importContext(Shell shell) {
	  mStaticContext = shell.getExportedContext();
}


@Override
  public ICommand getCommandClass(String name)
  {
	if( mStaticContext == null )
		return null;
    
	IFunctionDecl func = mStaticContext.getFunctionDecl(name);
	if( func != null )
		return new FunctionCommand( func.getName() , func.getBody() , null  );
	return null ;
	
  }

  @Override
  public IFunction getFunctionClass(String name)
  {
	  if( mStaticContext == null )
			return null;
	    
     IFunctionDecl func = mStaticContext.getFunctionDecl(name);
     if( func != null )
    	 return func.getFunction();
     return null ;
	
  }

  @Override
  public boolean hasHelp(String name)
  {
    return false;
  }

  @Override
  public boolean definesSameModule(IModule mod)
  {
    return false;
  }

  @Override
  public String describe()
  {
    return getName();
  }

}
