package org.xmlsh.commands.builtin;
import java.io.PrintWriter;
import java.util.List;

import org.xmlsh.core.BuiltinCommand;
import org.xmlsh.core.CommandFactory;
import org.xmlsh.core.ICommand;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.XValue;
import org.xmlsh.core.usage;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.util.Util;

/**
 * $Id: $
 * $Date: $
 *
 */

@usage("This is a usage string")
public class help extends BuiltinCommand {

	
	public int run(List<XValue> args) throws Exception {
		
		Options opts = new Options( "h=help" , SerializeOpts.getOptionDefs());
		opts.parse(args);
		
		
		
		List<XValue> xvargs = Util.expandSequences(opts.getRemainingArgs());
		
		if( xvargs.size() < 1  ){
			usage();
		}
		
		OutputPort stdout = mShell.getEnv().getStdout();
		PrintWriter out = stdout.asPrintWriter(this.getSerializeOpts(opts));
		


		for( XValue xname : xvargs ){
			
			String name = xname.toString();
			ICommand command = CommandFactory.getInstance().getCommand(mShell , name );
			if( command != null ){
				String u = command.getUsage();
				out.println("Usage: " + u );
				command.close();
			} 
			else
				out.println("Unknown command: " + name );
			
				
				
				
		
		}
		out.close();
		
		return 0;
		
		
		
	}

	private void usage() {
		// TODO Auto-generated method stub
		
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
