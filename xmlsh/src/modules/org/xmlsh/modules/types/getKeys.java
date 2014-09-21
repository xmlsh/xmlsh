/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.modules.types;

import java.util.List;

import org.xmlsh.core.AbstractBuiltinFunction;
import org.xmlsh.core.IXValueMap;
import org.xmlsh.core.XValue;
import org.xmlsh.core.XValueSequence;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.Util;

public class getKeys extends AbstractBuiltinFunction
{

  public getKeys()
  {
    super("get-key");
  }

  @Override
  public XValue run(Shell shell, List<XValue> args) throws Exception
  {
    XValueSequence list = new XValueSequence();
    for( XValue x : args ) { 
      if( x.isXType() ) {
        Object o = x.asObject();
        if( o instanceof org.xmlsh.core.IXValueMap ) {
          IXValueMap<?> m = (IXValueMap<?>) o ;
          for( String keys : Util.toList(  m.keySet().iterator()) ) {
            list.addValue( XValue.newXValue(keys) );
          }
        }
      }
    }
    return list.asXValue();
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