/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.commands.builtin;

import java.util.List;

import org.xmlsh.core.BuiltinCommand;
import org.xmlsh.core.CommandFactory;
import org.xmlsh.core.ICommand;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.core.SourceLocation;

public class source extends BuiltinCommand {


	
	public int run(   List<XValue> args ) throws Exception {
			if( args.size() != 1 )
				throw new UnexpectedException("source expects 1 argument");
			
			XValue port = args.get(0);
			ICommand icmd = CommandFactory.getInstance().getScript(mShell, port.toString() ,true,getLocation());
			if( icmd == null){
				SourceLocation loc = getLocation();
				if( loc != null )
					mShell.printErr(loc.toString());
				mShell.printErr( port + ": not found");
				return 1;
			}
			
			return icmd.run(mShell, port.toString() , null);
			
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
