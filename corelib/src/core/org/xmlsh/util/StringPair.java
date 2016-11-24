/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.util;

import org.xmlsh.core.XValue;
import org.xmlsh.sh.core.CharAttributeBuffer;

/**
 * A String which can optionally be a pair of strings delimited by a seperator
 */

public class StringPair {
  private CharAttributeBuffer mString;
  private CharAttributeBuffer mLeft = null;
  private CharAttributeBuffer mRight;
  private char escape;
  private char delim;
  

  public StringPair(String string, char delim) {
    this(string, delim, true,'\\');
  }
  public StringPair(String string, char delim, char escape ) {
    this(string, delim, true,escape);
  }
  public StringPair(String string, char delim, boolean bFirst) {
    this(string, delim, bFirst,'\\');
  }
  public StringPair(String string, char delim, boolean bFirst, char escape ) {
    this.delim = delim;
    this.escape = escape ;
    CharAttributeBuffer cb = CharAttributeBuffer.encodeString(string,escape);
    mString = cb ;

    int cpos = bFirst ? cb.indexOf(delim) : cb.lastIndexOf(delim);
    if(cpos >= 0) {
      mLeft = cb.subsequence(0, cpos);
      mRight = cb.subsequence(cpos + 1);

    }
    else
      mRight = cb;
  }

  public StringPair(CharAttributeBuffer left, CharAttributeBuffer right, char delim) {
    mLeft = left;
    mRight = right;
    if(left != null)
      mString = left.append(delim).append(right);
    else
      mString = right;
  }

  public StringPair(String left, String right, char delim) {
    this( CharAttributeBuffer.encodeString(left), CharAttributeBuffer.encodeString(right), delim);
  }

  public StringPair(XValue arg, char delim) {

    this(arg, delim, true);
  }

  public StringPair(XValue arg, char delim, boolean bFirst) {
    this(arg.toString(), delim, bFirst, '\\');
  }

  /**
   * @return the string
   */
  public String getString() {
    return mString.decodeString(escape);
  }

  /**
   * @return the string
   */
  public CharAttributeBuffer toStringA() {
    return mString;
  }

  /**
   * @return the left
   */
  public String getLeft() {
    
    return mLeft == null ? null : mLeft.decodeString(escape);
  }
  public CharAttributeBuffer getLeftA() {
    return mLeft;
  }
  /**
   * @return the right
   */
  public String getRight() {
    return mRight.decodeString(escape);
  }
  public CharAttributeBuffer getRightA() {
    return mRight;
  }
  /*
   * Return true if the pair had a left side.
   * Note: Returns true even if the left side is blank
   */

  public boolean hasLeft() {
    return mLeft != null;
  }

  public boolean hasRight() {
    return mRight != null && !mRight.isEmpty();
  }

  public boolean hasDelim() {
    return mLeft != null;
  }
  @Override
  public String toString() {
    return (mLeft==null ?"" : getLeft() + delim)  + getRight();
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
