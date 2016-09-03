package org.xmlsh.sh.core;

import java.util.Collections;
import java.util.List;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.Util;

public abstract class AbstractExpr implements IExpression {

  @Override
  public SourceLocation getSourceLocation() {
    return null;
  }

  @Override
  public boolean hasLocation() {
    return false;
  }

  @Override
  public String describe(boolean execMode) {
    return toString();
  }

  @Override
  public String toString() {
    return "Expr: " + getName();
  }

  protected String mName = null;
  protected List<String> mNames = null;

  public AbstractExpr(String name) {
    mName = name;
  }

  public AbstractExpr(List<String> names) {
    setNames(names);
  }

  public List<String> getNames() {
    if(mNames == null)
      return Collections.singletonList(getName());
    return mNames;
  }

  public void setNames(List<String> names) {
    if(names == null)
      return;
    mName = names.get(0);
    mNames = names;
  }

  protected AbstractExpr() {
  }

  protected void requires(boolean condition, String message)
      throws InvalidArgumentException {
    if(!condition)
      throw new InvalidArgumentException(getName() + ":" + message);

  }

  public void setName(String name) {
    mName = name;
  }

  public String getName() {
    return Util.isBlank(mName) ? "<anon>" : mName;
  }

}
