/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.sh.shell;

import java.util.Properties;

/*
 * The system environment emulates the "system".
 * for XMLSH this means it emulates the "Process Environment" of xmlsh
 * Processes are implemented as threads so the System environment is implemented
 * as thread local variables.
 * 
 * 
 */

@SuppressWarnings("serial")
public class SystemEnvironment extends Properties {

  private static ThreadLocal<SystemEnvironment> sInstance;

  /*
   * When tomcat re-loads a servlet it doesnt always re-initialize static vars
   * !!!
   * 
   */

  private static synchronized ThreadLocal<SystemEnvironment> _this() {
    if(sInstance == null)
      sInstance = new ThreadLocal<SystemEnvironment>() {
        @Override
        protected synchronized SystemEnvironment initialValue() {
          return new SystemEnvironment();
        }
      };

    return sInstance;
  }

  /**
   * Get an instance of the SystemEnvironment
   * This is a thread local copy
   * 
   */

  private SystemEnvironment() {
  }

  static SystemEnvironment getInstance() {
    return _this().get();

  }

  public static void uninitialize() {
    _this().get().clear();

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
