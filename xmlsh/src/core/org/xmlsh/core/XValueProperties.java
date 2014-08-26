/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.core;

import org.xmlsh.types.TypeFamily;

import java.util.Map;


/*
 * Generic Properties 
 * A set of Name/Value pairs to any object 
 */
public class XValueProperties extends XValueMap {

	public XValueProperties(XValueProperties that) {
		super(that);
	}

	public XValueProperties( Map<?,?> map ) {
	  super();
	  for( java.util.Map.Entry<?, ?> entries : map.entrySet() ) 
	    put( XValue.asXValue(entries.getKey()).toString() ,  XValue.asXValue( entries.getValue())  );
	  
	  
	}
	

  public XValueProperties() {
    super();
	}

  public static XValueProperties fromMap( Map<?,?> map ) {
    return new XValueProperties( map );
   
  };
  
  @Override
  public
  XValue asXValue() {
    return new XValue( TypeFamily.XTYPE , this );
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