/**
 * $Id: colon.java 21 2008-07-04 08:33:47Z daldei $
 * $Date: 2008-07-04 04:33:47 -0400 (Fri, 04 Jul 2008) $
 *
 */

package org.xmlsh.builtin.commands;

import java.util.List;
import org.xmlsh.core.BuiltinCommand;
import org.xmlsh.core.Options;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.ShellThread;
import org.xmlsh.util.Util;

public class wait extends BuiltinCommand {

  @Override
  public int run(List<XValue> args) throws Exception {

    Options opts = new Options("m=atmost:");
    opts.parse(args);

    args = opts.getRemainingArgs();

    if(opts.hasOpt("m"))
      return atmost(Util.parseInt(opts.getOptStringRequired("m"), 0));

    if(args.size() > 0) {
      for(XValue arg : args) {
        if(!arg.isAtomic()) {
          mShell.printErr("Arg is not a job ID");
          continue;
        }
        long id = arg.toLong();
        if(id < 0) {
          mShell.printErr("Arg is not a job ID");
          continue;
        }

        List<ShellThread> children = mShell.getChildren(false);
        if(children != null) {
          ShellThread toJoin = null;
          synchronized(children) {
            for(ShellThread thread : children) {
              if(thread.getId() == id) {
                toJoin = thread;
                break;
              }
            }
          }
          if(toJoin != null)
            toJoin.join();
        }
      }

    }
    else {
      ShellThread child;

      // while there are children jobs
      while((child = mShell.getFirstThreadChild()) != null)
        child.join();

    }
    return 0;

  }

  /*
   * Wait until at most n threads are in the background
   */
  private int atmost(int n) {
    mShell.waitAtMostChildren(n, 0);
    return 0;
  }

}

// Copyright (C) 2008-2014 David A. Lee.

// The contents of this file are subject to the "Simplified BSD License" (the
// "License");
// you may not use this file except in compliance with the License. You may
// obtain a copy of the
// License at http://www.opensource.org/licenses/bsd-license.php

// Software distributed under the License is distributed on an "AS IS" basis,
// WITHOUT WARRANTY OF ANY KIND, either express or implied.
// See the License for the specific language governing rights and limitations
// under the License.

// The Original Code is: all this file.

// The Initial Developer of the Original Code is David A. Lee

// Portions created by (your name) are Copyright (C) (your legal entity). All
// Rights Reserved.

// Contributor(s): none.
