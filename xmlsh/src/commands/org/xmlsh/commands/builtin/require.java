/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.commands.builtin;

import java.util.List;

import org.xmlsh.core.BuiltinCommand;
import org.xmlsh.core.ThrowException;
import org.xmlsh.core.XValue;

public class require extends BuiltinCommand {

	@Override
	protected int run(List<XValue> args) throws Exception {
		
		// Require with no args simply requires the require command exists
		// Added in version 1.0.1
		if( args.size() == 0 )
			return 0;
		
		String sreq = args.get(0).toString();
		
		int ret = mShell.requireVersion(null,sreq);
		if( ret == 0 )
			return 0;
		
		if( mShell.isInCommandConndition() )
			return 1;
		
		
		String err = "requires version: " + sreq ;  
		printErr(err);
		
		throw new ThrowException(new XValue(err) );
		
		
		
	}
}



//
//
//Copyright (C) 2008,2009,2010,2011,2012 David A. Lee.
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
