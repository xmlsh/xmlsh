package org.xmlsh.sh.core;

import java.util.List;

import org.xmlsh.core.EvalEnv;
import org.xmlsh.core.IFunction;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.Shell;

final class ScriptFunction implements IFunction
{
  private String mName ;
  private ICommandExpr mBody;
  public ScriptFunction(String name, ICommandExpr body)
  {
    mName = name ;
    mBody = body ;
  }

  @Override
  public XValue run(Shell shell, SourceLocation loc, List<XValue> args) throws Exception
  {

    return shell.runCommandFunction(mName, mBody, loc, args);

  }
  @Override
  public String getName()
  {
    // TODO Auto-generated method stub
    return mName;
  }

  @Override
  public EvalEnv argumentEnv(EvalEnv parent)
  {
    return parent.withFlagsSet(EvalEnv.commandArgsFlags());
  }

  @Override
  public EvalEnv returnEnv(EvalEnv parent)
  {
    return parent.withFlagsMasked(EvalEnv.returnValueMask());
  }
}