/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.sh.shell;

import org.xmlsh.core.CoreException;
import org.xmlsh.core.XValue;
import org.xmlsh.util.StringPair;
import org.xmlsh.util.Util;

import java.util.ArrayList;
import java.util.List;

/*
 * Modules are like namespaces.
 * They map a prefix to a package (instead of a URI)
 * 
 * Modules also have a list of default prefixes, (e.g. "xmlsh")
 */


@SuppressWarnings("serial")
public class Modules extends  ArrayList<Module>
{
	public IModule declare(Shell shell, String prefix , String name, List<XValue> init ) throws CoreException
	{
		/*
		 * Dont redeclare a module under the same prefix
		 */

		for( IModule m : this )
			if( Util.isEqual(m.getName(),name) && Util.isEqual(m.getPrefix(),prefix))
				return m;


		Module module = Module.createModule(shell, prefix , name , init  );
		return declare(module);
	}


	/**
	 * Declare/Import a module
	 * If prefix is not null and already used then re-declare the module
	 * @param init 
	 * @throws CoreException 
	 * 
	 */
	public IModule declare(Module module) throws CoreException
	{

		if( ! Util.isEmpty(module.getPrefix())){
			// IF module exists by this prefix then redeclare
			IModule exists = getModule( module.getPrefix() );
			if( exists != null )
				remove( exists );
		}
		else {
			// Non prefixed modules dont import the same package
			IModule exists = getExistingModule( module );
			if( exists != null )
				return exists ;
		}

		// Dont duplicate exact object
		if( this.contains(module))
			return module;

		this.add(module);

		return module ;

	}

	Modules() {}


	public IModule	getModule(String prefix)
	{
		for( IModule m : this )
			if( Util.isEqual(m.getPrefix(), prefix ) )
				return m ;
		return null;

	}

	public IModule	getExistingModule(IModule mod)
	{
	  
		for( IModule m : this )
			if( m.definesSameModule(mod ) )
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
	public IModule declare(Shell shell, String m, List<XValue> init) throws CoreException {
		StringPair 	pair = new StringPair(m,'=');
		return declare(shell, pair.getLeft(), pair.getRight() ,  init  );

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
