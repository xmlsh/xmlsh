/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.util;

import java.util.HashMap;
import java.util.TreeMap;

/*
 * A Name/Value map that maps String to templated type and has iterators
 * for keys and works like a HashMap
 * 
 * Suports clone method to manage use in the shell stack
 *  
 */


@SuppressWarnings("serial")
public class NameValueMap<T> extends TreeMap<String,T> implements Cloneable {

	/*
	 * Default Constructor
	 */
	public	NameValueMap()
	{}


	/*
	 * Copy constructor 
	 */
	protected	NameValueMap(NameValueMap<T> that)
	{
		putAll(that);
	}
	
	@Override
	public NameValueMap<T> clone() {
		return new NameValueMap<>( this );
	}
	
	public T getByValue( T v ){
		if( super.containsValue( v))
	       return v;
		return null;
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
