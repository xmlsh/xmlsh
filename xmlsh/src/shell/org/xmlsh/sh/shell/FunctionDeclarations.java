/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.sh.shell;

import java.util.Hashtable;

import org.xmlsh.sh.core.Command;

class FunctionDeclarations {
	
	// Functions are rare - dont allocate a table unless its usd
	private Hashtable<String,Command>	mFuncs = null;
	
	// Parent/Derived class
	// Used for efficient push/pop of shell context
	private FunctionDeclarations mParent = null ; 
	
	// Default constructor 
	FunctionDeclarations()
	{
	}
	
	// copy constructor
	FunctionDeclarations(FunctionDeclarations parent)
	{
		mParent = parent ;
	}
	
	
	/*
	 * Add a function declaration.
	 * Only adds to the current list, will not modify derived lists
	 * 
	 */
	void	add(String name, Command command)
	{
		
		if( mFuncs == null ){
			mFuncs = new Hashtable<String,Command>();

		}
		mFuncs.put(name, command);

	}
	
	/*
	 * Get a declaration if it exists
	 * First check this list then derived lists
	 */
	
	Command	get(String name)
	{
		
		if( mFuncs != null ){
			Command c = mFuncs.get(name);
			if( c != null )
				return c;
			
		}
		
		if( mParent != null )
			return mParent.get(name);
		return null;
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
