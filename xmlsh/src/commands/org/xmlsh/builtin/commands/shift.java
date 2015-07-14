/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.builtin.commands;

import java.util.List;

import org.xmlsh.core.BuiltinCommand;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.XValue;
import org.xmlsh.core.XVariable;
import org.xmlsh.util.Util;

public class shift  extends BuiltinCommand {


	@Override
	public int run( List<XValue> args ) throws Exception {

		if( args.size() == 0 ){
			return mShell.shift(1) ? 0 : 1;

		}

		if( args.size() == 1 ){
			String a1 = args.get(0).toString();
			if( Util.isInt(a1, false))
				return mShell.shift(Util.parseInt(a1, 1)) ? 0 : 1;
			else
				return shiftvar( a1 ,1  ) ? 0 : 1 ;
		}

		if( args.size() == 2 )
			return shiftvar( args.get(0).toString() , Util.parseInt(args.get(1), 1)) ? 0 : 1 ;
	    usage();
	    return -1 ;

	}

	private boolean  shiftvar(String name, int n) throws InvalidArgumentException {
		XVariable var = mShell.getEnv().getVar(name);
		if( var != null )
			return var.shift(n);
		else
		{
			// Try positional params 
			// shift 1 2 
			if( Util.isInt(name, false)){
				int np = Util.parseInt(name,-1);
				// Only shift params 1..
				List<XValue> args = mShell.getArgs();
				if( np > 1 && np < args.size()  ){
					XValue val = args.get(np-1);
					val = val.shift(n);
					args.set(np-1, val);
					return true ;
				}

			} 
			return false ;
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
