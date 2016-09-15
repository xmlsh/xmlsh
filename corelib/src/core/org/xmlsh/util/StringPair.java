/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.util;

import org.xmlsh.core.XValue;

/**
 * A String which can optionally be a pair of strings delimited by a seperator
 */

public class StringPair {
	private		String		mString;
	private		String 		mLeft  = null;
	private		String		mRight;

    public StringPair( String string , char delim )
    {
        this( string , delim , true );
    }

	
	public StringPair( String string , char delim , boolean bFirst )
	{
		mString = string ;
		
		int cpos = bFirst ? string.indexOf(delim) : string.lastIndexOf(delim);
		if( cpos >= 0 ){
			mLeft = string.substring(0,cpos);
			mRight = string.substring(cpos+1);

		} else
			mRight = string ;
	}
	
	public StringPair( String left , String right , char delim ){
		mLeft = left ; 
		mRight = right ;
		if( left != null )
			mString = left + delim + right ;
		else
			mString = right ;
	}

	
	 
	
	public StringPair(XValue arg,char delim) {

	    this( arg , delim , true );
	}
	public StringPair(XValue arg,char delim, boolean bFirst) {
		this(arg.toString(),delim, bFirst);
	}


	/**
	 * @return the string
	 */
	public String getString() {
		return mString;
	}

	/**
	 * @return the string
	 */
	@Override
	public String toString() {
		return mString;
	}

	/**
	 * @return the left
	 */
	public String getLeft() {
		return mLeft;
	}


	/**
	 * @return the right
	 */
	public String getRight() {
		return mRight;
	}

	/*
	 * Return true if the pair had a left side. 
	 * Note: Returns true even if the left side is blank
	 */

	public boolean hasLeft()
	{
		return mLeft != null;
	}


	public boolean hasRight()
	{
		return !Util.isEmpty(mRight);
	}

	public boolean	hasDelim()
	{
		return mLeft != null ;
	}


}



//
//
//Copyright (C) 2008-2014    David A. Lee.
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
