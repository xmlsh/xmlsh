/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.core;
import org.xmlsh.sh.core.SourceLocation;
import org.xmlsh.sh.core.CommandExpr;
import org.xmlsh.sh.core.ICommandExpr;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.HelpUsage;
import org.xmlsh.util.Util;

import java.io.PrintWriter;
import java.util.List;

public abstract class AbstractBuiltinFunction extends AbstractFunction implements IFunctionExpr {

  public abstract XValue  run( Shell shell , List<XValue> args ) throws Exception;
  protected AbstractBuiltinFunction( String name )
  { 
    super(name);
  }

  

  @Override
  public IFunction getFunction()
  {
    final AbstractBuiltinFunction func = this ;
    return new IFunction() {
      

      @Override
      public XValue run(Shell shell, SourceLocation loc, List<XValue> args) throws Exception
      {
        return func.run(shell, args);
        
      }

      @Override
      public String getName()
      {
        return func.getName();
      }} ;

  
  }
}



//
//
//Copyright (C) 2008-2014 David A. Lee.
//
//The contents of this file are subject to the "Simplified BSD License" (the "License");
//you may not use this file except in compliance with the License. You may obtain a copy of the
//License at http://www.opensource.org/licenses/bsd-license.php 
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied.
//See the License for the specific language governing rights and limitations under the License.
//
//The Original Code is: all this file.
//
//The Initial Developer of the Original Code is David A. Lee
//
//Portions created by (your name) are Copyright (C) (your legal entity). All Rights Reserved.
//
//Contributor(s): none.
//
