/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.sh.shell;

import java.io.InputStream;

import org.xmlsh.core.CommandFactory;
import org.xmlsh.core.ICommand;
import org.xmlsh.core.ScriptCommand;
import org.xmlsh.util.Util;

public class Module {
	private String 	mPrefix; 	// may be null
	private String mPackage;		// may NOT be null
	public Module(String prefix, String pkg) {
		super();
		mPrefix = prefix;
		mPackage = pkg;
	}
	/**
	 * @return the prefix
	 */
	public String getPrefix() {
		return mPrefix;
	}
	/**
	 * @return the uRI
	 */
	public String getPackage() {
		return mPackage;
	}
	public boolean isDefault() {
		return Util.isEmpty( mPrefix );
	}

	private InputStream getCommandResource( String name )
	{
		String resource = "/" + mPackage.replace('.','/') + "/" + name ;
		InputStream is = CommandFactory.class.getResourceAsStream(resource);
		
		return is;
	}
	
	
	

	public ICommand getCommandClass( String name) {
		try {
			Class<?> cls = Class.forName(mPackage + "." + name);
			ICommand cmd = (ICommand) cls.newInstance();
			return cmd;

		} catch (Exception e) {
			;

		}
		
		
		/*
		 * Try a script 
		 */
		InputStream scriptStream = this.getCommandResource(name + ".xsh");
		if( scriptStream != null )
			return new  ScriptCommand( name , scriptStream , false );
		return null;
		
		
		
	}


}



//
//
//Copyright (C) 2008,2009 David A. Lee.
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
