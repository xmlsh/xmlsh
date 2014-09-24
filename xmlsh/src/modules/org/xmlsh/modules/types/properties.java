/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.modules.types;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.xmlsh.core.AbstractBuiltinFunction;
import org.xmlsh.core.InputPort;
import org.xmlsh.core.XValue;
import org.xmlsh.core.XValueProperties;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.JavaUtils;

public class properties extends AbstractBuiltinFunction
{


  private SerializeOpts sopts;

  public properties()
  {
    super("properties");
  }

  @Override
  public XValue run(Shell shell, List<XValue> args) throws Exception
  {
    XValueProperties props = null ;
    for( XValue arg : args ) {
      if( props == null ) {
        if( arg.isInstanceOf( XValueProperties.class ) )
          props = arg.asInstanceOf(XValueProperties.class);
        else
        if( arg.isInstanceOf( Map.class ) )
          props = XValueProperties.fromMap( arg.asInstanceOf(Map.class));
        else
          props = JavaUtils.convert(arg, XValueProperties.class );
      }
      else {
        if( arg.isInstanceOf( XValueProperties.class ) )
          props = props.merge(arg.asInstanceOf(XValueProperties.class) );
        else
          if( arg.isInstanceOf( Map.class ) )
            props = props.merge( XValueProperties.fromMap( arg.asInstanceOf(Map.class)));
        
        
      }
    } 
      return props == null ? new XValueProperties().asXValue() : props.asXValue();
    
   }

  private void loadFromJSON(Properties props, InputPort in)
  {
   return ;
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