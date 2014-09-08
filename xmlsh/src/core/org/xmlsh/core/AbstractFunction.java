package org.xmlsh.core;

import org.xmlsh.sh.core.AbstractExpr;

public abstract class AbstractFunction extends AbstractExpr implements IFunction
{

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

}