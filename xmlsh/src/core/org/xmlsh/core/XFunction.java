package org.xmlsh.core;

import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.sh.core.AbstractExpr;
import org.xmlsh.sh.core.SourceLocation;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.Util;

public abstract class XFunction implements IXFunction {

  private String mName;
  protected static Logger mLogger = LogManager.getLogger();

  protected XFunction(String name) {
    mName = name;
  }

  @Override
  public EvalEnv argumentEnv(EvalEnv parent) {
    return parent.withFlagsOff(EvalFlag.EXPAND_WILD, EvalFlag.SPLIT_WORDS);
  }

  @Override
  public EvalEnv returnEnv(EvalEnv parent) {
    return EvalEnv.evalNone();
  }

  public String getName() {
    return mName;
  }

  protected void requires(boolean condition, String message)
      throws InvalidArgumentException {
    if(!condition)
      throw new InvalidArgumentException(getName() + ":" + message);

  }

  protected XValue getFirstArg(List<XValue> args)
      throws InvalidArgumentException {
    requires(!args.isEmpty(), "Excpected arugment missing");
    return args.get(0);
  }

  /**
   * Note that Command usage() only prints a message ,
   * Function usage throws an exception
   * 
   * @throws Exception
   */

  protected void error(Shell shell, Exception e) throws Exception {
    shell.printErr(getName(), e);
    printUsage(shell, e.toString());
    throw e;
  }

  protected void error(Shell shell, String s, Exception e) throws Exception {
    shell.printErr(getName() + " " + s, e);
    printUsage(shell, s + " " + e.toString());
    throw e;
  }

  public void printUsage(Shell shell, String message) {
    String cmdName = getName();
    SourceLocation sloc = shell.getLocation();

    if(!Util.isBlank(message))
      shell.printErr(cmdName + ": " + message, sloc);
    else
      shell.printErr(cmdName + ":", sloc);
  }

  public void usage(Shell shell, String message)
      throws InvalidArgumentException {
    printUsage(shell, message);
    throw new InvalidArgumentException(message);
  }

}
