/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.core;

import java.util.List;
import org.xmlsh.annotations.AnnotationUtils;
import org.xmlsh.sh.core.SourceLocation;
import org.xmlsh.sh.module.Module;
import org.xmlsh.sh.module.RootModule;
import org.xmlsh.sh.shell.Shell;

public abstract class AbstractBuiltinFunction extends XFunction {

  protected AbstractBuiltinFunction() {
    super((String) null);
    // defer to post super construction to resolve class
    AnnotationUtils.getFunctionNames(getClass());
  }

  @Override
  public Module getModule() {
    return RootModule.getInstance();
  }

  public abstract XValue run(Shell shell, List<XValue> args) throws Exception;

  protected AbstractBuiltinFunction(String name) {
    super(name);
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
