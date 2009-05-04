/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.sh.shell;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.xmlsh.core.CoreException;
import org.xmlsh.core.XValue;
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
	 * If prefix is not null and already used then re-declare the module
	 * @param init 
	 * @throws CoreException 
	 * 
	 */
	public Module declare(Shell shell, String prefix , String name, String pkg,  List<XValue> init ) throws CoreException
	{
		Module module = new Module( prefix , name , pkg );
		
		module.init(shell, init);
		
		if( ! Util.isEmpty(prefix)){
		// IF module exists by this prefix then redeclare
			Module exists = getModule( prefix );
			if( exists != null )
				remove( exists );
		}
		
		this.add(module);
	
		return module ;
	
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
	public void declare(Shell shell, String m, List<XValue> init) throws CoreException {
		StringPair 	pair = new StringPair(m,'=');
		if( pair.hasLeft() )
			declare(shell, pair.getLeft(), pair.getRight() ,  null,init  );
		else
			declare( shell, null , m  , null, init );
		
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
