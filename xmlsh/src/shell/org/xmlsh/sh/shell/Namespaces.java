/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.sh.shell;

import org.xmlsh.core.XValue;
import org.xmlsh.util.NameValueMap;
import org.xmlsh.util.Util;

@SuppressWarnings("serial")
public class Namespaces extends NameValueMap<String>
{

	public Namespaces() {}
	Namespaces(Namespaces that) 
	{
		super(that);
		
	}
	
	public void declareNamespace( String prefix , String uri )
	{
		if( Util.isEmpty(uri))
			remove(prefix);
		else
			put( prefix , uri );
	}
	
	public void declareNamespace( String ns ){
		

		String prefix = null;
		String url = null;
		int eqpos = ns.indexOf('=');
		if( eqpos >= 0 ){
			prefix = ns.substring(0,eqpos);
			url = ns.substring(eqpos+1);
		}
		
		declareNamespace( prefix , url );
		
	}
	public void declareNamespace(XValue v) {
		declareNamespace( v.toString() );
		
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
