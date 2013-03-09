/**
 * $Id: echo.java 88 2008-11-27 17:06:00Z daldei $
 * $Date: 2008-11-27 12:06:00 -0500 (Thu, 27 Nov 2008) $
 *
 */

package org.xmlsh.commands.builtin;

import java.util.List;

import org.xmlsh.core.BuiltinCommand;
import org.xmlsh.core.Options;
import org.xmlsh.core.XIOEnvironment;
import org.xmlsh.core.XValue;
import org.xmlsh.util.PipedPort;
import org.xmlsh.util.PipedStreamPort;
import org.xmlsh.util.PipedXDMPort;

public class xmkpipe extends BuiltinCommand {
	
	public int run( List<XValue> args ) throws Exception {

		Options opts = new Options( "x=xml,s=size:,close"  );
		opts.parse(args);
		

		

		
		args = opts.getRemainingArgs();
		if( args.size() != 1 ){
			this.usage("Usage: xmkpipe [-xml] name");
			return 1;
		}
		String name = args.get(0).toString();
		XIOEnvironment ioenv = mShell.getEnv().getSavedIO();
		
		if( opts.hasOpt("close")){
			ioenv.getOutputPort(name).close();
			return 0;
			
		}
		
		

		PipedPort pipe = null ;
		if( opts.hasOpt("x"))
			if( opts.hasOpt("size"))
				pipe = new PipedXDMPort(mShell.getSerializeOpts());
			else
				pipe = new PipedXDMPort(mShell.getSerializeOpts(), opts.getOptInt("size", 100));
		else
			if( opts.hasOpt("size"))
				pipe = new PipedStreamPort(opts.getOptInt("size", 10240));
			else
				pipe = new PipedStreamPort();
		

		
		ioenv.setInput(name, pipe.getInput());
		ioenv.setOutput(name, pipe.getOutput());
		
		
		return 0;
	}
}
//
//
//Copyright (C) 2008-2013    David A. Lee.
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
