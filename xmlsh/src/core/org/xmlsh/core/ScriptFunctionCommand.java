/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.core;

import java.io.Closeable;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.sh.core.SourceLocator;
import org.xmlsh.sh.module.IModule;
import org.xmlsh.sh.shell.Shell;

public class ScriptFunctionCommand extends AbstractBuiltinFunction implements Closeable {


	private URL mScriptURL;
	private IModule mModule;
	private SourceLocator mLocation;
 
	static Logger mLogger = LogManager.getLogger();

	public ScriptFunctionCommand(String name , URL input, IModule module ) {
		super(name);
		mScriptURL = input;
		mModule = module ;

	}

	@Override
	public void close() throws IOException
	{
		
		mLogger.entry();
	}

	@Override
	public XValue run(Shell shell, List<XValue> args ) throws Exception {


		try ( Shell sh = shell.clone() ){
				if( args != null )
					sh.setArgs(args);
				sh.setArg0(getName());
				return mLogger.exit( shell.runScript(mScriptURL , getName(),false).mReturnValue);

		}

		finally {
			close();
		}


	}

}



//
//
//Copyright (C) 2008-2014 David A. Lee.
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
