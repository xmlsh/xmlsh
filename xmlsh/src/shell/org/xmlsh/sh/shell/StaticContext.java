package org.xmlsh.sh.shell;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.core.Namespaces;
import org.xmlsh.core.XVariable;
import org.xmlsh.core.XVariable.XVarFlag;
import org.xmlsh.sh.module.IModule;
import org.xmlsh.util.NameValueMap;

// The 'Static Context' (similar to XQuery/XSLT)
// Composed of Functions, Modules, Namespaces anything which is determined
// at parse/compile time inherited by the shell
// All components of ModuleContext are immutable and sharable at any one one
// time,
// but the static context itself is not imutable - it is affected by parse
// and runtime declarations
public class StaticContext implements Cloneable {
  private static Logger mLogger = LogManager.getLogger();
  private FunctionDefinitions mFunctions = null;
  private Modules mModules;                    // Imported modules visible to
                                               // this module
  private int id = _id++;
  private static int _id = 0;
  private Namespaces mNamespaces = null;
  private EnumSet<XVarFlag> mVarFlags = XVariable.standardFlags();
  private NameValueMap<XVariable> mStaticLocals = null;

  public NameValueMap<XVariable> getStaticLocals() {
    return mStaticLocals;
  }

  // log debugging
  @Override
  public String toString() {
    return "CTX: " + id;
  }

  public StaticContext() {
    mLogger.entry();
  }

  @Override
  public StaticContext clone() {
    mLogger.entry(this);
    return mLogger.exit(new StaticContext(this));
  }

  // Clone this context
  protected StaticContext(StaticContext that) {
    mLogger.entry(that);

    if(that.mFunctions != null)
      mFunctions = that.mFunctions.clone();
    if(that.mModules != null)
      mModules = that.mModules.clone();
    if(that.mNamespaces != null)
      mNamespaces = new Namespaces(that.mNamespaces);

    if(that.mStaticLocals != null)
      mStaticLocals = that.mStaticLocals.clone();
    if(that.mVarFlags != null)
      mVarFlags = that.mVarFlags;
    mLogger.exit();

  }

  public void declareFunction(IFunctionDefiniton func) {
    mLogger.entry(func);

    if(mFunctions == null)
      mFunctions = new FunctionDefinitions();
    mFunctions.put(func.getName(), func);
  }

  public IFunctionDefiniton getFunction(String name) {

    if(mFunctions == null)
      return null;
    return mFunctions.get(name);
  }

  public FunctionDefinitions getFunctions() {

    if(mFunctions == null)
      mFunctions = new FunctionDefinitions();
    return mFunctions;
  }

  public Modules getModules() {

    if(mModules == null)
      mModules = new Modules();
    return mModules;
  }

  public EnumSet<XVarFlag> getVarFlags() {
    return mVarFlags;
  }

  public Namespaces getNamespaces() {
    if(mNamespaces == null)
      mNamespaces = new Namespaces();
    return mNamespaces;
  }

  public Iterable<IModule> getDefaultModules() {

    List<IModule> all = new ArrayList<>();
    for(IModule mh : mModules) {

      if(!mModules.hasAnyPrefixes(mh))
        all.add(mh);
    }
    return mLogger.exit(all);

  }

  public StaticContext export(NameValueMap<XVariable> localVars) {
    StaticContext ctx = clone();
    if(localVars != null && !localVars.isEmpty()) {
      if(ctx.mStaticLocals == null)
        ctx.mStaticLocals = localVars.clone();
      else
        ctx.mStaticLocals.putAll(localVars);
    }
    return ctx;
  }

}
