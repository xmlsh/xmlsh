/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.util;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * A Name/Value list that stores lists of pairs String to templated type and has iterators
 * for keys and works like a List
 * 
 * Suports clone method to manage use in the shell stack
 *  
 */


@SuppressWarnings("serial")
public class NameValueList<T> extends ArrayList< INameValue<T> > {

	/*
	 * Default Constructor
	 */
	public	NameValueList()
	{}

	/*
	 * Copy constructor 
	 */
	public	NameValueList( NameValueList<T> that)
	{
		addAll(that);
	}

	public INameValue<T> findName( String name ){
		for(INameValue<T> nv : this )
			if( nv.getName().equals(name) )
				return nv;
		return null;
	}


	public  INameValue<T>   removeName( String name ){
		for (Iterator<INameValue<T>> I = iterator(); I.hasNext();) {
			INameValue<T> e = I.next();
			if( e.getName().equals(name)){
				I.remove();
				return e;
			}
		}
		return null;
	}

	public   INameValue<T>  findValue( T value ){
		for(INameValue<T> nv : this )
			if( nv.getValue().equals(value) )
				return nv;
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
