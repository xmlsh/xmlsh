/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.builtin.commands;

import org.xmlsh.core.BuiltinCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.core.XVariable;

import java.io.File;
import java.util.List;

public class xcd extends BuiltinCommand {




	@Override
	public int run(  List<XValue> args ) throws Exception {
		String sdir = null;
		if( args.size() < 1 ){
			XVariable xhome = mShell.getEnv().getVar("HOME");

			String home = xhome == null ? null : xhome.getValue().toString();
			if( home == null )
				home = System.getProperty("user.home");

			if( home == null ){
				mShell.printErr("cd: Cannot cd to HOME");
				return 1;
			}
			sdir = home;
		}
		else
			sdir = args.get(0).toString();

		File newDir = mShell.getFile( sdir);
		if( newDir != null && newDir.exists() && newDir.isDirectory() && newDir.canRead() ){
			mShell.setCurdir(newDir);
			return 0;
		}
		else {
			mShell.printErr("cd: Cannot cd to: " + sdir);
			return 1;
		}

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
