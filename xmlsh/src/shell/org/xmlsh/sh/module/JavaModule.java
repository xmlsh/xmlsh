/**
 * $Id: $
 * $Date: $
 * 
 */

package org.xmlsh.sh.module;

import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.EvalEnv;
import org.xmlsh.core.EvalFlag;
import org.xmlsh.core.FunctionExpr;
import org.xmlsh.core.ICommand;
import org.xmlsh.core.IFunctionExpr;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.XClassLoader;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.types.TypeFamily;
import org.xmlsh.util.JavaUtils;
import org.xmlsh.util.Util;

public class JavaModule extends Module
{

  static class JavaModuleFunction extends  FunctionExpr
  {
    private JavaModule mModule;
    private Class<?> mClass;

    public JavaModuleFunction(JavaModule javaModule, String func, Class<?> cls, ClassLoader cl)
    {
      super(func);
      mModule = javaModule;
      mClass = cls;
    }

    @Override
    public EvalEnv argumentEnv(EvalEnv parent)
    {
      return parent.withFlagsOff(EvalFlag.EXPAND_WILD, EvalFlag.SPLIT_WORDS);
    }

    @Override
	public Module getModule() {
		return mModule;
	}

    @Override
    public EvalEnv returnEnv(EvalEnv parent)
    {
      return EvalEnv.evalNone();
    }

	@Override
    public XValue run(Shell shell, List<XValue> args) throws Exception
    {
      XValue retVal = null;

      if(Util.isEqual("new", getName())) {  // Constructor
        retVal = JavaUtils.newXValue(mClass, args);
      }
      else
      // return class as an object
      if(Util.isEqual("class", getName())) {

        retVal = XValue.newXValue(TypeFamily.JAVA, mClass);

      }
      else {

        Object thisObj = null;
        // Static first
        Method m = JavaUtils.getBestMatch(mClass, getName(), args, true);
        if(m == null && args.size() > 0) {

          thisObj = args.remove(0).asObject();
          if(mClass.isInstance(thisObj))
            m = JavaUtils.getBestMatch(mClass, getName(), args, false);

        }
        if(m == null)
          throw new InvalidArgumentException("Cannot find matching method: " + getName());

        retVal = thisObj != null ? JavaUtils.callMethod(m, thisObj, args) : JavaUtils.callStaticMethod(m, args);

      }
      return retVal;
    }

  }

  private Class<?> mJavaClass;
  
  
  static Logger mLogger = LogManager.getLogger();
  JavaModule( ModuleConfig config, XClassLoader loader) throws CoreException
  {
    super(config, loader );

    mJavaClass = findClass(config.getModuleClass());
    if(mJavaClass == null)
      throw new InvalidArgumentException("Class not found:" + config.getModuleClass());

  }

   static ModuleConfig getConfiguration(Shell shell, String clsname,
		List<URL> at) {
	ModuleConfig config = new ModuleConfig("java" , "java:" + clsname ,  at , shell.getSerializeOpts());
	config.setModuleClass(clsname);
	return config ;
   }

  @Override
  public String describe()
  {
    return "java [ class=" + mJavaClass.getName() + " ]";
  }

  @Override
  public ICommand getCommand(String name)
  {
    return null;
  }

  @Override
  public IFunctionExpr getFunction(final String name)
  {

    return new JavaModuleFunction( this , name, mJavaClass, getClassLoader());

  }


  @Override
  public boolean hasHelp(String name)
  {
    // TODO Auto-generated method stub
    return false;
  }

@Override
public URL findResource(String res) {
	mLogger.trace("TODO: maybe look in class's package for resource {}",res);
	return(null);
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