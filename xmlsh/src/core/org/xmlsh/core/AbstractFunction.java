package org.xmlsh.core;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.sh.core.AbstractExpr;
import org.xmlsh.sh.shell.IModule;
import org.xmlsh.sh.shell.StaticContext;

public abstract class AbstractFunction extends AbstractExpr implements IFunction
{

  @Override
	public StaticContext getStaticContext() {
		IModule m = getModule(); 
				return m == null ? null : m.getStaticContext();
	}
private static Logger mLogger = LogManager.getLogger();
  protected AbstractFunction(String name)
  {
    super(name);
  }
  protected AbstractFunction(){
    super("<anon>");
  }

  @Override
  public EvalEnv argumentEnv(EvalEnv parent)
  {
    return parent.withFlagsOff(EvalFlag.EXPAND_WILD , EvalFlag.SPLIT_WORDS) ;
  }

  @Override
  public EvalEnv returnEnv(EvalEnv parent)
  {
    return EvalEnv.evalNone() ;
  }
  @Override
	public IModule getModule() {
	  mLogger.warn("getModule called on AbstractFunction with no impl"); 
	  return null ;
  
  }

}