/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.core;

import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;

public class XClassLoader extends URLClassLoader {

  /*
   * Cache an instance (singleton if this class is loaded by the same class
   * loader)
   * used as the root of all new class loaders
   */

  private static volatile XClassLoader _instance = null;

  public static XClassLoader getInstance() {
    if(_instance == null) {
      synchronized(XClassLoader.class) {
        if(_instance == null)
          _instance = newInstance(ClassLoader.getSystemClassLoader());
      }
    }
    return _instance;
  }

  public static XClassLoader newInstance(ClassLoader parent) {

    return newInstance(new URL[] {}, parent);
  }

  private XClassLoader(URL[] urls, ClassLoader parent) {
    super(urls, parent);

  }

  void add(URL... urls) {
    for(URL u : urls)
      super.addURL(u);
  }

  public void add(List<URL> urls) {
    for(URL u : urls)
      super.addURL(u);
  }

  // A new XClass loader derived from the Root instance
  public static XClassLoader newInstance() {

    return new XClassLoader(new URL[] {}, getInstance());
  }

  // A new XClass loader derived from the Root instance
  public static XClassLoader newInstance(URL[] urls) {
    return new XClassLoader(urls, getInstance());
  }

  // A new XClass loader derived from the Root instance
  // See URL ClassLoader for use of PriviledgeAction

  public static XClassLoader newInstance(final URL[] urls,
      final ClassLoader parent) {

    XClassLoader loader = AccessController
        .doPrivileged(new PrivilegedAction<XClassLoader>() {
          @Override
          public XClassLoader run() {
            return new XClassLoader(urls, parent);
          }
        });

    return loader;
  }

  public static XClassLoader newInstance(List<URL> classPath,
      final ClassLoader parent) {
    if(classPath == null || classPath.isEmpty())
      return newInstance(parent);
    return newInstance(classPath.toArray(new URL[classPath.size()]), parent);

  }

  public static XClassLoader newInstance(List<URL> classPath) {
    if(classPath == null || classPath.isEmpty())
      return newInstance(new URL[] {});
    return newInstance(classPath.toArray(new URL[classPath.size()]));
  }
}

//
//
// Copyright (C) 2008-2014 David A. Lee.
//
// The contents of this file are subject to the "Simplified BSD License" (the
// "License");
// you may not use this file except in compliance with the License. You may
// obtain a copy of the
// License at http://www.opensource.org/licenses/bsd-license.php
//
// Software distributed under the License is distributed on an "AS IS" basis,
// WITHOUT WARRANTY OF ANY KIND, either express or implied.
// See the License for the specific language governing rights and limitations
// under the License.
//
// The Original Code is: all this file.
//
// The Initial Developer of the Original Code is David A. Lee
//
// Portions created by (your name) are Copyright (C) (your legal entity). All
// Rights Reserved.
//
// Contributor(s): none.
//
