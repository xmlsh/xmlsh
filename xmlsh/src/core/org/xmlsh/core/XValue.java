/**
 * $Id: $
 * $DateTime: $
 *
 */

package org.xmlsh.core;

import java.util.ArrayList;

import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmValue;

public class XValue {
	
	XdmValue	mValue;			// s9 value
	String		mString;		// string 
	
	
	
	public XValue()
	{
		mValue = null;
		mString = null;
	}
	
	/*
	 *  Create an atomic string (xs:string)
	 */
	
	public XValue(String s)
	{
		mString = s;
	}
	/*
	 * Create an XValue from an XdmValue 
	 */
	public XValue( XdmValue v )
	{
		mValue = v;
	}
	
	/*
	 *  Create an XValue by combining a list of XValue objects into a single XValue
	 */
	public XValue( Iterable<XValue> args) {
		ArrayList<XdmItem> items = new ArrayList<XdmItem>();
		for( XValue arg : args ){
			XdmValue v = arg.toXdmValue();
			for( XdmItem item : v )
				items.add( item );
			
		}

		mValue =  new XdmValue(  items);
		
	}
	
	/*
	 * Return (cast) the variable to an XdmValue
	 * 
	 * The first time a variable is evaluated in an XML context a XdmValue is created and cached
	 * 
	 */
	public XdmValue toXdmValue(){
		if( mValue == null ){
			mValue = new XdmAtomicValue( mString );
			
		}
		
		return mValue ;
		
	}

	
	public XValue(int n) {
		this( Integer.toString(n));
	}

	public XValue(long n) {
		this(Long.toString(n));
	}

	
	public String	toString(){
		if( mString != null )
			return mString;
		if( mValue != null )
			return mValue.toString();
		return "";
	}

	
	/*
	 * Variables are considered pure strings
	 * if the string element is not null
	 */
	public boolean isString() {
		return mString != null ;
	}	

}
//
//
//Copyright (C) 2008, David A. Lee.
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
