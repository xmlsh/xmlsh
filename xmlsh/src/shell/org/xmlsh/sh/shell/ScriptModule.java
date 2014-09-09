package org.xmlsh.sh.shell;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.xmlsh.core.CommandFactory;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.FunctionCommand;
import org.xmlsh.core.ICommand;
import org.xmlsh.core.IFunction;
import org.xmlsh.core.IFunctionDecl;
import org.xmlsh.core.ScriptCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.core.ScriptCommand.SourceMode;

public class ScriptModule extends AbstractModule
{

  private ScriptCommand mScript ; 
  private FunctionDefinitions mFunctions;
  
  
  protected ScriptModule(Shell shell, ScriptCommand script , String prefix, String nameuri ) throws IOException, CoreException
  {
    super(nameuri , prefix);

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
      
      if(  mScript.run(sh, getName(), args) != 0 )
        shell.printErr("Failed to init script:" + getName() );
      else {
         importContext( sh );

      }
    }
  }
  
  
  private void importContext(Shell shell) {
	  mFunctions = shell.getFunctionDelcs();
}


@Override
  public ICommand getCommandClass(String name)
  {
	if( mFunctions == null )
		return null;
    
	IFunctionDecl func = mFunctions.get(name);
	if( func != null )
		return new FunctionCommand( func.getName() , func.getBody() , null  );
	return null ;
	
  }

  @Override
  public IFunction getFunctionClass(String name)
  {
	  if( mFunctions == null )
			return null;
	    
     IFunctionDecl func = mFunctions.get(name);
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
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public String describe()
  {
    return getName();
  }

}
