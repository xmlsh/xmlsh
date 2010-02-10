/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.commands.posix;

import java.util.List;

import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;

public class sleep extends XCommand {


	@Override
	public int run(List<XValue> args) throws Exception {
		
		/*
		Options opts = new Options( "r=reference:" , args );
		opts.parse();
		
		args = opts.getRemainingArgs();
		*/
		
		if( args.size() != 1 ){
			usage();
			return -1;
		}
		
		String arg = args.get(0).toString();
		double darg = Double.parseDouble(arg);
		long millis = (long) (darg * 1000.);
		
		Thread.sleep(millis);
		
		return 0;
	}

	private void usage() {
		printErr("usage: sleep delay");
		
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
