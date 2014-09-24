/**
 * $Id: xpwd.java 21 2008-07-04 08:33:47Z daldei $
 * $Date: 2008-07-04 04:33:47 -0400 (Fri, 04 Jul 2008) $
 * 
 */

package org.xmlsh.java.commands;

import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.Permission;
import java.util.List;

import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.util.Util;

/*
 * 
 * Command to call into any java class's main method in the same JVM
 * 
 * jcall class args
 */

public class jcall extends XCommand
{
  @SuppressWarnings("serial")
  private static class ExitException extends SecurityException
  {
    int mExitCode;

    ExitException(int code)
    {
      mExitCode = code;
    }

  }

  private static class NoExitSecurityManager extends SecurityManager
  {
    SecurityManager mParent;

    @Override
    public void checkPermission(Permission perm)
    {
      if(mParent != null)
        mParent.checkPermission(perm);

    }

    @Override
    public void checkPermission(Permission perm, Object context)
    {

      if(mParent != null)
        mParent.checkPermission(perm, context);

    }

    NoExitSecurityManager(SecurityManager parent)
    {
      mParent = parent;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.SecurityManager#checkExit(int)
     */
    @Override
    public void checkExit(int status)
    {
      throw new ExitException(status);
    }

  }

  /*
   * jcall invoke native java main class
   * 
   * @see org.xmlsh.core.XCommand#run(java.util.List)
   */

  @Override
  public synchronized int run(List<XValue> args) throws Exception
  {

    SerializeOpts serializeOpts = getSerializeOpts();
    /*
     * Do NOT use Options because we need to split out the jcall options differently then the invoked options
     */

    ClassLoader classloader = null;
    if(args.size() > 1) {
      String arg1 = args.get(0).toString();
      if(arg1.equals("-cp") || arg1.equals("-classpath")) {
        args.remove(0);
        XValue classpath = args.remove(0);
        classloader = getClassLoader(classpath);
      }

    }
    if(classloader == null)
      classloader = getClassLoader();

    SecurityManager oldManager = null;
    if(args.size() < 1)
      throw new InvalidArgumentException("usage: jcall [-cp classpath] [class|object] [args]");

    PrintStream stdout = System.out;

    // DAL: Resesting stderr causes stderr to be lost after this call
    // dont know why. Dont reset stderr and all is well.
    // PrintStream stderr = System.err;
    InputStream stdin = System.in;

    PrintStream newStdout = null;
    // PrintStream newStderr = null;

    try {
      oldManager = System.getSecurityManager();
      System.setSecurityManager(new NoExitSecurityManager(oldManager));

      System.setOut(newStdout = getStdout().asPrintStream(serializeOpts));

      System.setIn(getStdin().asInputStream(serializeOpts));

      String className = args.remove(0).toString();
      Class<?> cls = Class.forName(className, true, classloader);

      Method method = cls.getMethod("main", String[].class);
      method.invoke(null, new Object[] { Util.toStringArray(args) });

    } catch (InvocationTargetException e) {
      Throwable e2 = e.getTargetException();
      if(e2 instanceof ExitException)
        return ((ExitException) e2).mExitCode;
      else if(e2 != null && e2 instanceof Exception)
        throw (Exception) e2;

      else throw e;
    } catch (ExitException e) {

      return e.mExitCode;
    } catch (Exception e) {
      e.printStackTrace();
    }

    finally {
      System.setSecurityManager(oldManager);

      System.setOut(stdout);
      // System.setErr(stderr);
      System.setIn(stdin);

      newStdout.flush();
      // newStderr.flush();

    }

    return 0;

  }

  /*
   * Test for calling jcall or exiting
   */
  public static void main(String[] args)
  {
    if(args.length > 0)
      System.out.println(args[0]);
    if(args.length == 1 && args[0].equals("exit"))
      System.exit(1);

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
