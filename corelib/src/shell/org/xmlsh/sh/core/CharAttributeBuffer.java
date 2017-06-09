/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.sh.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import org.xmlsh.util.CharAttr;
import lombok.val;

/*
 * Like StringBuilder but stores extended attributes with each charactor.
 * 
 */
public class CharAttributeBuffer {

  private static final int DEFAULT_CAPACITY = 256;
  private int capacity;
  private int length;
  private char[] charArray;
  private byte[] attrArray;
  

  @FunctionalInterface
  public interface CharAttributeDecoder {
    void decode(StringBuilder sb, char ch, char esc, byte attrs);
  }


  @FunctionalInterface
  public interface CharAttributeEncoder {
    byte encode(CharAttributeBuffer sb, char ch, char esc , byte attrs);
  }


  public CharAttributeBuffer() {
    this(DEFAULT_CAPACITY);
  }

  public CharAttributeBuffer(int capacity) {
    this.capacity = capacity;
    charArray = new char[capacity];
    attrArray = new byte[capacity];
  }

  public CharAttributeBuffer clone() {
    return new CharAttributeBuffer(this);
  }

  public CharAttributeBuffer(CharAttributeBuffer that) {
    this(that == null ? 0 : that.capacity);
    if(that != null)
      append(that);
  }

  public CharAttributeBuffer(String s, CharAttr attr) {
    this(s, (byte) attr.toBit());
  }

  public CharAttributeBuffer(String s, CharAttrs attrs) {
    this(s, (byte) attrs.toBits());
  }

  public CharAttributeBuffer(String s, EnumSet<CharAttr> attrs) {
    this(s, (byte) CharAttrs.toBits(attrs));
  }

  public CharAttributeBuffer(String s, byte attr) {
    int size = s.length();
    capacity = size + DEFAULT_CAPACITY - (size % DEFAULT_CAPACITY);
    charArray = new char[capacity];
    attrArray = new byte[capacity];
    append(s, attr);

  }

  private CharAttributeBuffer(char[] ca, byte[] aa, int len, int c) {
    charArray = ca;
    attrArray = aa;
    length = len;
    capacity = c;
  }

  public CharAttributeBuffer(String s) {
    this(s, (byte) 0);
  }

  public CharAttributeBuffer  append(char c) {
    return append(c, (byte) 0);
  }

  public CharAttributeBuffer append(char c, CharAttrs attr) {
    return append(c, attr.toBits());
  }

  public CharAttributeBuffer append(char c, byte attr) {
    ensure(length + 1);
    charArray[length] = c;
    attrArray[length++] = attr;
    return this;
  }

  private void ensure(int size) {
    if(size <= capacity)
      return;

    capacity = size + DEFAULT_CAPACITY - (size % DEFAULT_CAPACITY);
    charArray = Arrays.copyOf(charArray, capacity);
    attrArray = Arrays.copyOf(attrArray, capacity);

  }

  public int attrAt(int pos) {
    return attrArray[pos];
  }

  public char charAt(int pos) {
    return charArray[pos];
  }

  @Override
  public String toString() {
    //return decodeString();
    return new String( this.charArray , 0, this.length);
  }

  public static CharAttributeBuffer encodeString(String s,  CharAttributeEncoder encoder, char esc, CharAttrs attrs ) {
    if( s == null ) return null ; // preserve null
    byte ca = attrs.toBits();
    CharAttributeBuffer b = new CharAttributeBuffer(s.length());
    for( char c : s.toCharArray() )
      ca = encoder.encode( b , c , esc , ca);
    return b;
  }
  
  public static String decodeString(CharAttributeBuffer cb , CharAttributeDecoder decoder, char esc ) {
    if( cb == null ) return null;
    StringBuilder sb = new StringBuilder(cb.length);
    for(int i = 0; i < cb.length; i++)
      decoder.decode(sb, cb.charArray[i], esc , cb.attrArray[i]);
    return sb.toString();
  }

  public static CharAttributeDecoder defaultDecoder = 
    (StringBuilder sb, char ch, char esc , byte attrs) -> {
        if((attrs & CharAttr.ESCAPED) != 0)
          sb.append(esc);
        sb.append(ch);
      }   ;
    public static CharAttributeEncoder defaultEncoder = 
        (CharAttributeBuffer cb, char ch, char esc, byte attrs) -> {
          
          // Hard quoted 'a\\b'   -- esc ignored except \' 
          // Soft quoted  "a\\b"  -- esc added literally , sets esc bit
          // currently escaped adds literally clears bit
          // not quoted sets escbit only 
          byte retattrs = attrs;
          if( (attrs & CharAttr.ESCAPED) != 0 ){
            retattrs &= ~CharAttr.ESCAPED;
          }
          else
          if( (attrs & CharAttr.HARD_QUOTE ) != 0  ){
            if( ch == esc )
              retattrs |= CharAttr.ESCAPED;
          }
          else
          if( (attrs & CharAttr.SOFT_QUOTE ) != 0  ){
             if( ch == esc  )
               retattrs |= CharAttr.ESCAPED;
             else
             if( ch == '"')
               attrs |= CharAttr.ESCAPED ;
          }
          else
          if( ch == esc ){
            retattrs |= CharAttr.ESCAPED;
            return retattrs;
          }
          cb.append(ch,attrs);
          return (retattrs);
        }   ;
      

  public static String decodeString( CharAttributeBuffer sb, char esc) {
    return decodeString(sb , defaultDecoder, esc);
  }
  public String decodeString() {
    return decodeString( this , '\\');
  }
  public String decodeString(char esc) {
    return decodeString( this , esc);
  }  
  public static CharAttributeBuffer encodeString(String s, char esc, CharAttrs attrs) {
    return encodeString( s,defaultEncoder, esc, attrs);
  }
  public static CharAttributeBuffer encodeString(String s, char esc) {
    return encodeString(s,esc,CharAttrs.NONE);
  }

  public static CharAttributeBuffer encodeString(String s) {
    return encodeString( s,defaultEncoder, '\\',CharAttrs.NONE);
  }

  public char[] getCharArray() {
    return Arrays.copyOf(charArray, length);
  }

  public byte[] getAttrArray() {
    return Arrays.copyOf(attrArray, length);
  }

  public int size() {
    return length;
  }

  public boolean isEmpty() {
    return length == 0;
  }

  public void clear() {
    length = 0;
  }

  public void append(String s) {
    append(s, (byte) 0);
  }

  public void append(String s, byte attr) {
    int len = s.length();
    ensure(length + len);
    s.getChars(0, len, charArray, length);
    Arrays.fill(attrArray, length, length + len, attr);
    length += len;
  }

  public int indexOf(int start, char c, byte attr) {
    for(int i = start; i < length; i++) {
      if(charArray[i] == c && attrArray[i] == attr)
        return i;
    }
    return -1;
  }
  public int indexOf(char delim) {
    return indexOf( 0,delim , CharAttr.NONE );
  }

  public int lastIndexOf(char c) {
      return lastIndexOf( 0 , c, CharAttr.NONE );
  }
  public int lastIndexOf(int first , char c, byte attr) {
    for(int i = length-1; i >=first ; i--) {
      if(charArray[i] == c && attrArray[i] == attr)
        return i;
    }
    return -1;
  }
  public void delete(int start, int len) {
    if(start + len > length)
      len = (length - start);
    if(len <= 0)
      return;
    System.arraycopy(charArray, start + len, charArray, start, length - len);
    System.arraycopy(attrArray, start + len, attrArray, start, length - len);

    length -= len;

  }
  
  // split using c as an optionally repeatable seperator e.g. a/b//c == [a,b,c]
  public CharAttributeBuffer[] split(char c,boolean repeatable) {
    ArrayList<CharAttributeBuffer> list = new ArrayList<CharAttributeBuffer>();

    int start = 0;
    for(int i = 0; i < length; i++) {
      if(charArray[i] == c) {
        list.add(subsequence(start, i));
        if( repeatable)
          while(i < length && charArray[i + 1] == c)
            i++;
        start = i + 1;
      }
    }
    if(start < length)
      list.add(subsequence(start, length));

    return list.toArray(new CharAttributeBuffer[list.size()]);

  }
  public CharAttributeBuffer subsequence(int start) {
    return subsequence(start,length);
  }
  public CharAttributeBuffer subsequence(int start, int i) {
    return new CharAttributeBuffer(
        Arrays.copyOfRange(charArray, start, i),
        Arrays.copyOfRange(attrArray, start, i),
        i - start,
        capacity);

  }

  @Override
  public boolean equals(Object that) {
    if(this == that)
      return true;
    if(that instanceof CharAttributeBuffer) {
      CharAttributeBuffer cb = (CharAttributeBuffer) that;
      if(length == cb.length) {
        for(int i = 0; i < length; i++) {
          if(charArray[i] != cb.charArray[i] ||
              attrArray[i] != cb.attrArray[i])
            return false;
        }
        return true;
      }
    }
    return false;
  }

  public boolean stringEquals(String s) {
    int len = s.length();
    if(length != len)
      return false;
    for(int i = 0; i < len; i++)
      if(charArray[i] != s.charAt(i))
        return false;
    return true;
  }

  public CharAttributeBuffer append(CharAttributeBuffer achars) {
    if(achars != null) {
      int len = achars.length;
      ensure(length + len);
      System.arraycopy(achars.charArray, 0, charArray, 0, len);
      System.arraycopy(achars.attrArray, 0, attrArray, 0, len);
      length += len;
    }
    return this ;
  }

  public void append(String s, CharAttrs attr) {
    append(s, attr.toBits());

  }

  public void logString(StringBuilder sb) {

    sb.append("[");
    for(int i = 0; i < 10 && i < length; i++) {
      if(i > 0)
        sb.append(',');
      sb.append(charArray[i]).append(':')
          .append(CharAttrs.fromBits(attrArray[i]));
    }
  }

  public String[] splitString(char cs) {
    return splitString(cs,'\\',false);
  }
  
  public String[] splitString(char cs,char esc, boolean repeatable) {
    val cv = split(cs,repeatable);
    val sa = new String[cv.length];
    int i =0 ;
    for( val v : cv ){
      sa[i++] = v.decodeString(esc) ;
    }
    return sa;
  }


}

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
