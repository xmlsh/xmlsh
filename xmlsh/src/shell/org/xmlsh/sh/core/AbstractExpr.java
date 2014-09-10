package org.xmlsh.sh.core;

import java.util.List;

import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.HelpUsage;
import org.xmlsh.util.Util;

public abstract class AbstractExpr 
{
	
	public String toString() {
		return "Expr: " + getName();
	}
	
	
  protected String mName  = null ;

  public AbstractExpr(String name)
  {
    mName = name ;
  }

  public AbstractExpr()
  {
  }

  protected void requires(boolean condition, String message) throws InvalidArgumentException
  {
  	if( ! condition )
  		throw new InvalidArgumentException( getName() + ":" + message );
  
  }

  protected void error(Shell shell, Exception e)
  {
    shell.printErr( getName() , e);
    usage( shell ,  e.toString() );
  }

  public void usage(Shell shell, String message)
  {
    String cmdName = this.getName();
    SourceLocation sloc = getLocation(shell);
    if( !Util.isBlank(message))
      shell.printErr(cmdName + ": " + message,sloc);
    else
      shell.printErr(cmdName + ":", sloc );
    HelpUsage helpUsage = new HelpUsage( shell );
    try {
      helpUsage.doUsage(shell.getEnv().getStdout(), cmdName);
    } catch (Exception e) {
      shell.printErr("Usage: <unknown>",sloc);
    }
  }

  protected SourceLocation getLocation(Shell shell)
  {
    
    if( hasLocation() ) 
      return getLocation() ;
    else
      return shell.getLocation();
  }

  protected SourceLocation getLocation()
  {
    // TODO Auto-generated method stub
    return null;
  }

  protected boolean hasLocation()
  {
    // TODO Auto-generated method stub
    return false;
  }

  public void usage(Shell shell)
  {
    usage(shell);
  }

  public void setName(String name)
  {
  	mName = name;
  }

  public String getName()
  {
  	return Util.isBlank(mName) ? "<anon>" : mName ;
  }

  protected XValue getFirstArg(List<XValue> args) throws InvalidArgumentException
  {
  	requires( ! args.isEmpty() , "Excpected arugment missing");
  	return args.get(0);
  }

}