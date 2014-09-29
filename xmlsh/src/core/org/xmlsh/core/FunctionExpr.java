package org.xmlsh.core;


import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.sh.core.AbstractExpr;

public abstract class FunctionExpr extends AbstractExpr implements IFunctionExpr
{



  private static Logger mLogger = LogManager.getLogger();
  protected FunctionExpr(String name)
  {
    super(name);
  }
  protected FunctionExpr(List<String> names){
    super(names);
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

}