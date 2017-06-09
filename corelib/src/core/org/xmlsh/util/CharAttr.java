/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.util;

public enum CharAttr {

  ATTR_SOFT_QUOTE, 
  ATTR_HARD_QUOTE, 
  ATTR_PRESERVE,   // Do not touch, unquote or
  ATTR_ESCAPED;
  private byte bit;
  
  CharAttr() { 
    bit = (byte) (1<<ordinal());
  }
  public byte toBit() {
    return bit;  
  }
  public static byte NONE = 0;
  public static byte SOFT_QUOTE = ATTR_SOFT_QUOTE.bit ;
  public static byte HARD_QUOTE = ATTR_HARD_QUOTE.bit ;
  public static byte PRESERVE = ATTR_PRESERVE.bit ;
  public static byte ESCAPED = ATTR_ESCAPED.bit ;
  

  public static CharAttr fromBit(byte b) {
    if(b == 0)
      return null;
    for(CharAttr attr : CharAttr.values()) {
      if((b & attr.bit) == 0)
        return attr;
    }
    return null;
  }
};

/*
 * Copyright (C) 2008-2012 David A. Lee.
 * 
 * The contents of this file are subject to the "Simplified BSD License" (the
 * "License");
 * you may not use this file except in compliance with the License. You may
 * obtain a copy of the
 * License at http://www.opensource.org/licenses/bsd-license.php
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied.
 * See the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is: all this file.
 *
 * The Initial Developer of the Original Code is David A. Lee
 *
 * Portions created by (your name) are Copyright (C) (your legal entity). All
 * Rights Reserved.
 *
 * Contributor(s): David A. Lee
 * 
 */
