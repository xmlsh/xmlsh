/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.util;

import java.util.HashMap;

/*
 * A Name/Value map that maps String to templated type and has iterators
 * for keys and works like a HashMap
 * 
 * Suports clone method to manage use in the shell stack
 *  
 */


@SuppressWarnings("serial")
public class NameValueMap<T> extends HashMap<String,T> {
	
	/*
	 * Default Constructor
	 */
	public	NameValueMap()
	{}
	
	
	
	/*
	 * Copy constructor 
	 */
	public	NameValueMap(NameValueMap<T> that)
	{
		putAll(that);
	}



	

}



//
//
//Copyright (C) 2008-2012  David A. Lee.
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
