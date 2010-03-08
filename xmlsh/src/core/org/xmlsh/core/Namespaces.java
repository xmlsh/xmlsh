/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.core;

import org.xmlsh.util.NameValueMap;
import org.xmlsh.util.StringPair;
import org.xmlsh.util.Util;

@SuppressWarnings("serial")
public class Namespaces extends NameValueMap<String>
{

	public Namespaces() {}
	public Namespaces(Namespaces that) 
	{
		super(that);
	}
	
	public void declare( String prefix , String uri )
	{
		if( Util.isEmpty(prefix))
			prefix = "";
		
		if( Util.isEmpty(uri))
			remove(prefix);
		else
			put( prefix , uri );
	}
	
	/**
	 * Declare a prefix/uri pair
	 * 
	 * prefix=uri			// prefix + uri
	 * =uri					// default namespace uri
	 * prefix=				// delete prefix mapping
	 * uri					// equivilent to =uri
	 * 
	 */
	
	public void declare( String ns ){
		StringPair 	pair = new StringPair(ns,'=');
		declare( pair.getLeft(), pair.getRight() );
		
	}
	public void declare(XValue v) {
		declare( v.toString() );
		
	}
	

}



//
//
//Copyright (C) 2008,2009,2010 , David A. Lee.
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
