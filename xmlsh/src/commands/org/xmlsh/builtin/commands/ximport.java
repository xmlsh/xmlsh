/**
 * $Id: colon.java 21 2008-07-04 08:33:47Z daldei $
 * $Date: 2008-07-04 04:33:47 -0400 (Fri, 04 Jul 2008) $
 * 
 */

package org.xmlsh.builtin.commands;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.annotations.Command;
import org.xmlsh.core.BuiltinCommand;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.XEnvironment;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.module.Module;
import org.xmlsh.util.StringPair;
import org.xmlsh.util.Util;

@Command(name="import")
public class ximport extends BuiltinCommand
{
  private static Logger mLogger = LogManager.getLogger();

  @Override
  public int run(List<XValue> args) throws Exception
  {
    int ret = 0;
    if(args.size() < 1) {
      usage();
      return 1;
    }

    XValue what = args.remove(0);

    try {
      if(what.toString().equals("module"))
        return importModule(args);
      else if(what.toString().equals("package")) {
        if(args.isEmpty())
          usage();
        else for (XValue arg : args)
          if( ! importPackage(new StringPair(arg,'='))) {
            mShell.printErr("package: " + arg.toString() + " not found");
            ret++;
          }
        return ret;
      }
      else if(what.toString().equals("commands")) {
        if(args.isEmpty())
          usage();
        else for (XValue arg : args) {
          if( ! importCommands(new StringPair(arg,'='))){
            mShell.printErr("xmlsh command extension: " + arg.toString() + " not found");
            ret++;
          }
        }
        return ret;
      }
      else if(what.toString().equals("java"))
        return importJava(args);
      else 
        return importScript(new StringPair(what,'='),args);
    }

    catch (InvalidArgumentException e) {
      mLogger.info("invalid argument exception importing: " + what.toString(), e);
      if(mShell.isInCommandConndition())
        return -1;
      // mShell.printErr("invalid argument exception importing: "+ what.toString() ,e );
      throw e;

    }

    catch (Exception e) {
      mLogger.warn("Uncaught exception: " + e);
      throw e;
    }

  }

  /*
   * Implements
   * import module 
   * import foo=script
   * import foo=a.b.c at jar-file
   */
  private int importScript(StringPair qname, List<XValue> args) throws Exception
  {
    
    List<URL> at = getAt(args);
    
   // ScriptCommand icmd = CommandFactory.getInstance().getScript(mShell, at.toString() ,SourceMode.IMPORT,getLocation());

    mShell.importScript(qname.getLeft(),qname.getRight(), at, args ); 
    
    
    return 0;
    
  }

  private int importModule(List<XValue> args) throws Exception
  {
    if(args.size() == 0)
    {
    	usage();
    	return 2;
    }

    String mod = args.remove(0).toString();
    List<URL> at = getAt(args);
    StringPair pair = new StringPair(mod, '=');
    

    mShell.importModule(pair.getLeft(),pair.getRight(), at, args);

    return 0;
  }

  private List<URL>  getAt(List<XValue> args) throws CoreException
  {
    List<URL> at = null;
    if(args.size() > 1 && args.get(0).isAtomic() && args.get(0).toString().equals("at")) {
      args.remove(0);
      at = mShell.toUrls(args);
    }
    return at;
  }


  
  
  
  
  /*
   * Implements
   * import java
   * import java a.jar b.jar c.jar
   * import java at a.jar
   */

  private int importJava(List<XValue> args) throws CoreException
  {
    if(args.size() == 0)
      return listClasspaths();

   List<URL> at = getAt(args);
   if( at == null )
	   at = mShell.toUrls(args);

    mShell.importJava( at  );

    return 0;
  }

  /*
   * import package name foo.bar.spam
   * import package foo.bar.spam
   */

  private int listClasspaths() throws CoreException
  {
    ClassLoader cl = mShell.getClassLoader(null);
    while (cl != null) {
      if(cl instanceof URLClassLoader) {
        for (URL url : ((URLClassLoader) cl).getURLs())
          mShell.printOut(url.toString());

      }
      cl = cl.getParent();
    }
    return 0;

  }

  private boolean importPackage(StringPair qname) throws Exception
  {

    return mShell.importPackage(qname.getLeft(),qname.getRight(), Collections.singletonList(qname.getRight()));
  }

  private boolean importCommands(StringPair qname) throws Exception
  {


    return mShell.importPackage(qname.getLeft(),qname.getRight(),internalPackages(qname.getRight())) ;
  }

  private List<String> internalPackages(String fullname)
  {
    return Arrays.asList("org.xmlsh." + fullname + ".commands", "org.xmlsh." + fullname + ".functions");

  }


}
//
//
// Copyright (C) 2008-2014 David A. Lee.
//
// The contents of this file are subject to the "Simplified BSD License" (the "License");
// you may not use this file except in compliance with the License. You may obtain a copy of the
// License at http://www.opensource.org/licenses/bsd-license.php
//
// Software distributed under the License is distributed on an "AS IS" basis,
// WITHOUT WARRANTY OF ANY KIND, either express or implied.
// See the License for the specific language governing rights and limitations under the License.
//
// The Original Code is: all this file.
//
// The Initial Developer of the Original Code is David A. Lee
//
// Portions created by (your name) are Copyright (C) (your legal entity). All Rights Reserved.
//
// Contributor(s): none.
//
