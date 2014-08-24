/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.builtin.commands;

import org.xmlsh.core.BuiltinCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.core.XVariable;
import org.xmlsh.util.Util;

import java.util.List;

public class shift  extends BuiltinCommand {


	@Override
	public int run( List<XValue> args ) throws Exception {

		if( args.size() == 0 ){
			mShell.shift(1);
			return 0;

		}

		if( args.size() == 1 ){
			String a1 = args.get(0).toString();
			if( Util.isInt(a1, false))
				mShell.shift(Util.parseInt(a1, 1));
			else
				shiftvar( a1 ,1 );

			return 0;
		}

		if( args.size() == 2 ){

			shiftvar( args.get(0).toString() , Util.parseInt(args.get(1), 1));


		}

		return 0;

	}

	private void shiftvar(String name, int n) {
		XVariable var = mShell.getEnv().getVar(name);
		if( var != null )
			var.shift(n);
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
				}

			}
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
