/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.core;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.xmlsh.sh.core.SourceLocation;
import org.xmlsh.sh.shell.Module;
import org.xmlsh.sh.shell.Shell;

public class ScriptFunctionCommand extends BuiltinFunctionCommand {
	
	
	private InputStream mScript;
	private Module mModule;
	private SourceLocation mLocation;
	
	
	
	
	public ScriptFunctionCommand(String name , InputStream is, Module module ) {
		super(name);
		mScript = is;
		mModule = module ;
		
	}
	
	private void close()
	{
		try {
			mScript.close();
		} catch (IOException e) {
			;
		}
	}

	@Override
	public XValue run(Shell shell, List<XValue> args ) throws Exception {
		
		
		try {
			Shell sh = shell.clone();
			try {
					if( args != null )
						sh.setArgs(args);
					sh.setArg0(getName());
					int iret = shell.runScript(mScript, getName(),false);
					
					return shell.getReturnValue(true);
				} finally {
					// Close shell - even if exception is thrown through sh.runScript and up
					sh.close();

				}

		}
			
		finally {
			close();
		}
		
		
	}

}



//
//
//Copyright (C) 2008,2009,2010,2011 David A. Lee.
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
