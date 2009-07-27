/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.commands.builtin;

import java.util.List;

import org.xmlsh.core.BuiltinCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.core.XVariable;
import org.xmlsh.util.Util;

public class shift  extends BuiltinCommand {

	
	public int run( List<XValue> args ) throws Exception {
		
		int num = 1;
		
		if( args.size() == 0 ){
			mShell.shift(1);
			return 0;
			
		}
		
		if( args.size() == 1 ){
			String a1 = args.get(0).toString();
			if( Util.isInt(a1, false))
				mShell.shift(Util.parseInt(a1, 1));
			else
				shift( a1 ,1 );
			
			return 0;
		}
		
		if( args.size() == 2 ){
			
			shift( args.get(0).toString() , Util.parseInt(args.get(1), 1));
			
			
		}
		
		return 0;
				
	}

	private void shift(String name, int n) {
		XVariable var = mShell.getEnv().getVar(name);
		if( var != null )
			var.shift(n);
		
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
