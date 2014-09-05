/**
 * $Id: $
 * $Date: $
 * 
 */

package org.xmlsh.sh.shell;

import org.xmlsh.core.CoreException;
import org.xmlsh.core.EvalEnv;
import org.xmlsh.core.EvalFlag;
import org.xmlsh.core.ICommand;
import org.xmlsh.core.IFunctionDecl;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.core.CommandExpr;
import org.xmlsh.sh.core.ICommandExpr;
import org.xmlsh.types.TypeFamily;
import org.xmlsh.util.JavaUtils;
import org.xmlsh.util.Util;

import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JavaModule extends Module
{

  static class JavaModuleFunctionCommand extends CommandExpr
  {
    private IModule mModule;
    private String mFunc;
    private Class<?> mClass;


    public JavaModuleFunctionCommand(IModule mod, String func, Class<?> cls, ClassLoader cl)
    {
      super(func);
      mModule = mod;
      mFunc = func;
      mClass = cls;
    }

    @Override
    public void print(PrintWriter out, boolean bExec)
    {
      out.println(mClass.getName() + mFunc + "()");

    }

    @Override
    public int exec(Shell shell) throws Exception
    {
      List<XValue> args = shell.getArgs();
      XValue retVal = null;

      if(Util.isEqual("new", mFunc)) {  // Constructor
        retVal = JavaUtils.newXValue(mClass, args);
      }
      else
      // return class as an object
      if(Util.isEqual("class", mFunc)) {

        retVal = XValue.newXValue(TypeFamily.JAVA, mClass);

      }
      else {

        Object thisObj = null;
        // Static first
        Method m = JavaUtils.getBestMatch(mClass, mFunc, args, true);
        if(m == null && args.size() > 0) {

          thisObj = args.remove(0).asObject();
          if(mClass.isInstance(thisObj))
            m = JavaUtils.getBestMatch(mClass, mFunc, args, false);

        }
        if(m == null)
          throw new InvalidArgumentException("Cannot find matching method: " + mFunc);

        retVal = thisObj != null ? JavaUtils.callMethod(m, thisObj, args) : JavaUtils.callStaticMethod(m, args);

      }
      shell.exec_return(retVal);
      return 0;

    }

    @Override
    public boolean isSimple()
    {
      // TODO Auto-generated method stub
      return true;
    }

  }

  private Class<?> mJavaClass;

  JavaModule(Shell shell, String prefix, URI nameURI, List<XValue> args) throws CoreException
  {
    super(prefix);
    List<URL> classpath = null;
    if(args.size() > 1 && args.remove(0).toString().equals("at")) {
      classpath = new ArrayList<URL>();
      for (XValue xv : args) {
        URL classurl = shell.getURL(xv.toString());
        classpath.add(classurl);
      }
    }
    mClassLoader = getClassLoader(classpath);
    mHelpURL = null;
    String clsname = nameURI.getRawSchemeSpecificPart();

    int ldot = clsname.lastIndexOf('.');
    mName = clsname.substring(ldot + 1);
    mJavaClass = findClass(clsname );
    if(mJavaClass == null)
      throw new InvalidArgumentException("Class not found:" + clsname);

  }

  @Override
  public IFunctionDecl getFunctionClass(final String name)
  {

    final IModule thisModule = this;
    return new IFunctionDecl()
      {

        @Override
        public String getName()
        {
          return name;
        }

        @Override
        public ICommandExpr getBody()
        {
          return new JavaModuleFunctionCommand(thisModule, name, mJavaClass, mClassLoader);

        }

        @Override
        public EvalEnv argumentEnv(EvalEnv parent)
        {
          return parent.withFlagsOff(EvalFlag.EXPAND_WILD , EvalFlag.SPLIT_WORDS);
        }

        @Override
        public EvalEnv returnEnv(EvalEnv parent)
        {
          return EvalEnv.evalNone();
        }

      };

  }

  @Override
  public ICommand getCommandClass(String name)
  {
    return null;
  }

  @Override
  public boolean hasHelp(String name)
  {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean definesSameModule(IModule mod)
  {
    if( mod == null || ! ( mod instanceof JavaModule) )
        return false ;
    return ((JavaModule)mod).mJavaClass.equals( mJavaClass ) ;
  }

  @Override
  public String describe()
  {
    return "java:" + mJavaClass.getName() ;
  }

}

/*
 * Copyright (C) 2008-2012 David A. Lee.
 * 
 * The contents of this file are subject to the "Simplified BSD License" (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.opensource.org/licenses/bsd-license.php
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied.
 * See the License for the specific language governing rights and limitations under the License.
 * 
 * The Original Code is: all this file.
 * 
 * The Initial Developer of the Original Code is David A. Lee
 * 
 * Portions created by (your name) are Copyright (C) (your legal entity). All Rights Reserved.
 * 
 * Contributor(s): David A. Lee
 */