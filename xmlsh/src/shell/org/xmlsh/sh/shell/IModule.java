/**
 * $Id: $
 * $Date: $
 * 
 */

package org.xmlsh.sh.shell;

import org.xmlsh.core.ICommand;
import org.xmlsh.core.IFunction;



import org.xmlsh.core.IReleasable;
import org.xmlsh.core.XValue;

import java.io.Closeable;
import java.io.IOException;
import java.net.URL;
import java.util.List;

public interface IModule extends Closeable , IReleasable
{
  
  /*
   * Calls into the module
   */
  
  public void onLoad(Shell shell);
  public void onInit(Shell shell , List<XValue> args);
  public void onAttach(Shell shell )  throws IOException;
  public void onDetach(Shell shell) throws IOException;
  public void onUnload();
  
  /*
   * Calls TO the module
   */
  public String getPrefix();
  public boolean isDefault();
  public ICommand getCommandClass(String name);
  public IFunction getFunctionClass(String name);
  public String getName();
  public boolean hasHelp(String name);
  public URL getHelpURL();
  public boolean definesSameModule(IModule mod);
  public String describe();
  public URL getResource(String res);


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