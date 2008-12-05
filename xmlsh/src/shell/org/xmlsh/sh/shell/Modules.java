/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.sh.shell;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xmlsh.util.Util;

/*
 * Modules are like namespaces.
 * They map a prefix to a package (instead of a URI)
 * 
 * Modules also have a list of default prefixes, (e.g. "xmlsh")
 */


public class Modules extends Namespaces
{
	private		List<String>		mDefaults = new ArrayList<String>();
	
	
	/*
	 * Add a prefix to the list of default prefixes
	 * Resolve this to the actual package name and store that
	 */
	void 	addDefault( String def ){
		String pkg = this.get(def);
		if( pkg != null )
			mDefaults.add( pkg );
	}
	
	public Iterable<String> defaultPackages()
	{
		return 	mDefaults;
	}
	
	/*
	 * Override the Namespaces declare
	 * If prefix is null or "" then just add the uri to the defaults list
	 * 
	 */
	public void declare( String prefix , String uri )
	{
		if( Util.isEmpty(prefix))
			mDefaults.add(uri);
		else
		if( Util.isEmpty(uri))
			remove(prefix);
		else
			put( prefix , uri );
	}
	
	Modules() {}
	
	
	Modules( Modules that){
		super( that );
		mDefaults.addAll(that.mDefaults);
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
