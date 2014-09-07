/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.sh.shell;


import org.xmlsh.core.ICommand;
import org.xmlsh.core.IFunction;
import org.xmlsh.core.IFunctionDecl;
import org.xmlsh.core.IFunctionExpr;
import org.xmlsh.core.ScriptCommand;
import org.xmlsh.core.ScriptFunctionCommand;
import org.xmlsh.core.XCommand;
import org.xmlsh.util.JavaUtils;
import org.xmlsh.util.Util;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.List;

public class PackageModule extends Module
{
  protected List<String> mPackages;


  /*
   * Constructor for internal modules like xlmsh
   * These dont get their own thread group
   */
  protected PackageModule(String prefix, String name, List<String> packages , String helpURL)
  {
    super(name,prefix);
    mPackages = packages;
    mClassLoader = getClassLoader(null);
    // Undocumented - if you use a class loader to find a resource dont start it with "/"
    mHelpURL = mClassLoader.getResource(helpURL.replaceFirst("^/", ""));
  }
  
  protected PackageModule( String prefix, String name)
  {
    super(name,prefix);
  }


  public PackageModule(String prefix)
  {
    super(prefix);
  }

  private InputStream getCommandResource(String name)
  {
    /*
     * Undocumented: When using a classloader to get a resource, then the
     * name should NOT begin with a "/"
     */

    /*
     * Get cached indication of if there is a resource by this name
     */

    Boolean hasResource = mScriptCache.get(name);
    if(hasResource != null && !hasResource.booleanValue())
      return null;

    for( String pkg :getCommandPackages() ) {
      InputStream is = mClassLoader.getResourceAsStream( toResourceName(name,pkg));
       if( is != null ) {
          mScriptCache.put(name, true );
          return is ;
       }
    }
    mScriptCache.put(name, false );

    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.xmlsh.sh.shell.IModule#getFunctionClass(java.lang.String)
   */
  @Override
  public IFunctionExpr getFunctionClass(String name)
  {

    String origName = name;
    /*
     * Convert from camelCase to hypen-case
     */

    name = convertCamelCase(name);
    name = fromReserved(name);

    try {

      Class<?> cls = findClass( name,getFunctionPackages());
      if(cls != null) {
        Constructor<?> constructor = cls.getConstructor();
        if(constructor != null) {
          Object obj = constructor.newInstance();
          if( obj instanceof IFunctionExpr )
            return (IFunctionExpr) obj ;
          
          if(obj instanceof IFunctionDecl) {
            IFunctionDecl cmd = (IFunctionDecl) obj;
            return cmd.getFuntionExpr();
          }
        }
      }

    } catch (Exception e) {
      ;

    }

    /*
     * Try a script
     */
    InputStream scriptStream = getCommandResource(origName + ".xsh");
    if(scriptStream != null)
      return new ScriptFunctionCommand(name, scriptStream, this);
    return null;

  }
 

  /*
   * (non-Javadoc)
   * 
   * @see org.xmlsh.sh.shell.IModule#hasCommand(java.lang.String)
   */
  @Override
  public boolean hasHelp(String name)
  {

    try {

      Class<?> cls = findClass( name , getCommandPackages() );

      if(cls != null)
        return true;

    } catch (Exception e) {
      ;

    }

    return hasCommandResource(name + ".xsh");

  }


  private List<String> getCommandPackages()
  {
    return mPackages;
  }
  private List<String> getFunctionPackages()
  {
    return mPackages;

  }

  protected boolean hasCommandResource(String name)
  {
    for( String pkg : getCommandPackages() ) {
      if(mClassLoader.getResource(toResourceName(name,pkg)) != null)
        return true ;
    }
    return false ;

  }
  /*
   * (non-Javadoc)
   * 
   * @see org.xmlsh.sh.shell.IModule#getCommandClass(java.lang.String)
   */
  @Override
  public ICommand getCommandClass(String name)
  {

    /*
     * Convert from hyphen-case to camelCase
     */

    name = convertCamelCase(name);
    name = fromReserved(name);

    // Store the camel name not the hyphen name
    String origName = name;

    /*
     * First try to find a class that matches name
     */

    try {

      Class<?> cls = findClass(name,getCommandPackages());
      if(cls != null) {
        Constructor<?> constructor = cls.getConstructor();
        if(constructor != null) {
          Object obj = constructor.newInstance();
          if(obj instanceof XCommand) {
            XCommand cmd = (XCommand) obj;
            cmd.setModule(this);
            return cmd;
          }
          else
            mLogger.warn("Command class found [ {} ] but is not instance of XCommand." , cls.getName() );
        }
      }

    } catch (Exception e) {
      mLogger.debug("Exception calling constructor for:" + name, e);

    }

    /*
     * Second
     * Try a script stored as a resource
     */

    // mScriptCache caches a Boolean indicating whether the resource is found or not
    // No entry in cache means it has not been tested yet

    // Failures are cached with a null command
    String scriptName = origName + ".xsh";

    InputStream scriptStream = getCommandResource(scriptName);
    if(scriptStream != null)

      return new ScriptCommand(name, scriptStream, false, this);

    return null;

  }
  
  
  /*
   * Conversts hypen-case to camelCase, also converts from any reserved word
   */


  
  private String convertCamelCase(String name)
  {
    if(name.indexOf('-') < 0)
      return name;

    String parts[] = name.split("-");
    if(parts.length == 1)
      return name;

    StringBuffer result = new StringBuffer(name.length());

    for (String p : parts) {
      if(p.length() == 0)
        continue;

      if(result.length() == 0)
        result.append(p);
      else {
        result.append(Character.toUpperCase(p.charAt(0)));
        result.append(p.substring(1));
      }

    }

    return result.toString();

  }

  @Override
  public boolean definesSameModule(IModule mod)
  {
    if( mod == null || ! ( mod instanceof PackageModule) )
        return false ;
    PackageModule pmod =  ((PackageModule)mod);
    // Equivilence is if this module contains all packages as that
    return mPackages.containsAll(pmod.mPackages);
    
  }
  private String fromReserved(String name)
  {
    if(JavaUtils.isReserved(name))
      return "_" + name;
    else return name;
  }

  @Override
  public String describe()
  {
    return mName + "[" + Util.join( mPackages , ",");
  }


}



/*
 * Copyright (C) 2008-2012 David A. Lee.
 * 
 * The contents of this file are subject to the "Simplified BSD License" (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.opensource.org/licenses/bsd-license.php 

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
 * 
 */