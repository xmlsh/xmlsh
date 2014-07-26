/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.sh.core;

import java.util.ArrayList;
import java.util.Arrays;

/*
 * Like StringBuilder but stores extended attributes with each charactor.
 * 
 */
public class CharAttributeBuffer
{

	private static final int DEFAULT_CAPACITY = 256;
	private int capacity;
	private int length;
	private char[] charArray;
	private byte[] attrArray;
	
	
	public CharAttributeBuffer()
	{
		this( DEFAULT_CAPACITY );
	}
	
	
	public CharAttributeBuffer(int capacity)
	{
		this.capacity = capacity;
		charArray = new char[capacity];
		attrArray = new byte[capacity];
	}
	
	public CharAttributeBuffer(String s, byte attr)
    {
		int size = s.length();
		capacity = size + DEFAULT_CAPACITY - ( size % DEFAULT_CAPACITY) ; 
		charArray = new char[capacity];
		attrArray = new byte[capacity];
		append( s , attr );
 		
    }
	
	private CharAttributeBuffer(char[] ca , byte[] aa , int len , int c ) {
		charArray = ca;
		attrArray = aa ;
		length = len ;
		capacity = c;
	}

	


	public CharAttributeBuffer(String s)
    {
	    this(s,(byte)0);
    }


	public void append( char c ) {
		append(c,(byte) 0);
	}
	
	public void append( char c , byte attr ) {
		ensure( length+1 );
		charArray[length] = c ;
		attrArray[length++] = attr;
	}

	private void ensure(int size)
    {
		if( size <= capacity )
			return ;
				
		capacity = size + DEFAULT_CAPACITY - ( size % DEFAULT_CAPACITY) ;
		charArray = Arrays.copyOf(charArray, capacity);
		attrArray = Arrays.copyOf(attrArray, capacity);
		 
    }
	
	public int attrAt( int pos ) {
		return attrArray[pos];
	}

	public char charAt( int pos ) {
		return charArray[pos];
	}

	public String toString( ) {
		return  new String( charArray , 0 , length );
	}
	
	
	public char[] getCharArray() { 
		return Arrays.copyOf(charArray, length);
	}
	
	public byte[] getAttrArray() {
		return Arrays.copyOf(attrArray, length);
	}
	
	public int size() {
		return length ;
	}

	public boolean isEmpty() {
		return length == 0 ;
	} 
	
	public void clear() {
		length = 0;
	}
	public void append(String s) {
		append( s , (byte)0 );
	}


	public void append(String s, byte attr)
    {
		int len = s.length();
		ensure( length + len );
        s.getChars(0,len,charArray,length);
        Arrays.fill(attrArray, length , length+len , attr );
        length += len ;
    }


	public int indexOf(int start , char c, int attr)
    {
	    for(int i = start ; i < length ; i++ ) {
	    	if( charArray[i] == c && attrArray[i] == attr )
	    		return i;
	    }
	    return -1;
    }


	public void delete(int start, int len)
    {
		if( start + len > length)
			len = (length-start);
		if(len <= 0)
			return;
		System.arraycopy(charArray, start+len, charArray, start, length-len);
		System.arraycopy(attrArray, start+len, attrArray, start, length-len);

		length -= len ;
	    
    }


	public CharAttributeBuffer[] split(char c)
    {
	   ArrayList<CharAttributeBuffer> list = new ArrayList<CharAttributeBuffer>();
	   
	   int start = 0 ;
	   for( int i = 0 ; i < length ; i++ ) {
         if( charArray[i] == c ) {
        	 list.add( subsequence( start , i ) );
        	 start = i+1;
         }
	   }
	   if( start < length )
		   list.add( subsequence( start , length ) );
		   
		 return list.toArray(new CharAttributeBuffer[ list.size() ]);
	   
	   
	   
    }


	public CharAttributeBuffer subsequence(int start, int i)
    {
		return new CharAttributeBuffer(
				Arrays.copyOfRange(charArray , start , i ) ,
				Arrays.copyOfRange(attrArray , start , i ) ,
				i-start , 
				capacity );

		
    }
	public boolean equals( Object that ) {
		if( this == that )
			return true ;
		if( that instanceof CharAttributeBuffer ) {
			CharAttributeBuffer cb = (CharAttributeBuffer) that;
			if( length == cb.length ) {
				for( int i = 0 ; i < length ; i++ ) {
					if( charArray[i] != cb.charArray[i] ||
						attrArray[i] != cb.attrArray[i] )
						  return false ;
				}
				return true ;
			}
		}
		return false ;
	}
	
	public boolean stringEquals( String s ) {
		int len = s.length();
		if( length != len )
			return false ;
		for( int i = 0 ; i < len ; i++ )
			if( charArray[i] != s.charAt(i))
				return false ;
		return true ;
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