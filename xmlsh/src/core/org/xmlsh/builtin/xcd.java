/**
 * $Id: $
 * $DateTime: $
 *
 */

package org.xmlsh.builtin;

import java.io.File;

import org.xmlsh.core.BuiltinCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.core.XVariable;
import org.xmlsh.sh.shell.Shell;

public class xcd extends BuiltinCommand {
	
	
	
	
	public int run(Shell shell, String cmd, XValue[] args) throws Exception {
		String sdir = null;
		if( args.length < 1 ){
			XVariable xhome = shell.getEnv().getVar("HOME");
			
			String home = xhome == null ? null : xhome.getValue().toString();
			if( home == null )
				home = System.getProperty("user.home");
			
			if( home == null ){
				shell.printErr("Cannot cd to HOME");
				return 1;
			}
			sdir = home;
		}
		else
			sdir = args[0].toString();
		
		File newDir = shell.getFile( sdir);
		if( newDir.exists() && newDir.isDirectory() && newDir.canRead() ){
			shell.setCurdir(newDir);
			return 0;
		}
		else {
			shell.printErr("Cannot cd to: " + sdir);
			return 1;
		}
		
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
