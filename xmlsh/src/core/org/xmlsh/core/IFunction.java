/**
 * $Id: $
 * $Date: $
 * 
 */

package org.xmlsh.core;

import java.util.List;

import org.xmlsh.sh.shell.IModule;
import org.xmlsh.sh.shell.Module;
import org.xmlsh.sh.shell.Shell;

public interface IFunction
{

  public XValue run(Shell shell, List<XValue> args) throws Exception;
  public String getName();
  public EvalEnv argumentEnv(EvalEnv env);
  public EvalEnv returnEnv(EvalEnv env);
  public IModule	getModule();


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