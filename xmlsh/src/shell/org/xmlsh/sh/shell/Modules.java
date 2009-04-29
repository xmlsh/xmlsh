/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.sh.shell;

import java.util.ArrayList;

import org.xmlsh.util.StringPair;
import org.xmlsh.util.Util;

/*
 * Modules are like namespaces.
 * They map a prefix to a package (instead of a URI)
 * 
 * Modules also have a list of default prefixes, (e.g. "xmlsh")
 */


@SuppressWarnings("serial")
public class Modules extends  ArrayList<Module>
{
	

	/**
	 * Declare/Import a module
	 * If prefix is null or "" then just add the uri to the defaults list
	 * 
	 */
	public void declare( String prefix , String uri )
	{
		Module module = new Module( prefix , uri );
		
		// IF module exists by this prefix then redeclare
		Module exists = getModule( prefix );
		if( exists != null )
			remove( exists );
		
		this.add(module);
	
	
	}
	
	Modules() {}
	
	
	public Module	getModule(String prefix)
	{
		for( Module m : this )
			if( Util.isEqual(m.getPrefix(), prefix ) )
				return m ;
		return null;
		
	}
	Modules( Modules that){
		this.addAll(that);
	}

	/*
	 * Import a module by string value
	 * 
	 * prefix=class
	 * class
	 * 
	 */
	public void declare(String m) {
		StringPair 	pair = new StringPair(m,'=');
		if( pair.hasLeft() )
			declare( pair.getLeft(), pair.getRight() );
		else
			declare( null , m  );
		
	}
	
}



//
//
//Copyright (C) 2008,2009 , David A. Lee.
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
