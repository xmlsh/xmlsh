/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.types;

import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.XValue;
import org.xmlsh.util.JavaUtils;

abstract class AbstractTypeFamily implements ITypeFamily
{	

  
  static String describeClass(Object obj)
  {
    return obj == null ? "<null>" : obj.getClass().getName(); 
  }
 
  

  @Override
  public XValue append(Object o , XValue v) throws InvalidArgumentException
  {
    if( o == null )
      return v; 
    return getXValue( JavaUtils.concat( o , v.asObject() ));


  }


  @Override
  public String asString(Object obj)
  {
    return obj.toString();

  }


  @Override
  public String simpleTypeName(Object obj)
  {
    return JavaUtils.simpleTypeName(obj);
  }

  /* (non-Javadoc)
   * @see org.xmlsh.types.IMethods#typeName(java.lang.Object)
   */
  @Override
  public String typeName(Object obj)
  {
    if( obj == null )
      return "null";
    return JavaUtils.getClassName(obj);
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