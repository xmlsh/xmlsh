/**
 * $Id: $
 * $DateTime: $
 *
 */

package org.xmlsh.builtin;

import org.xmlsh.core.BuiltinCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.Util;

public class shift  extends BuiltinCommand {

	
	public int run( Shell shell,String cmd , XValue[] args) throws Exception {
		
		int num = 1;
		if( args.length > 0 )
			num = Util.parseInt( args[0] , 1  );
		
		
		shell.shift( num );
		return 0;
				
	}



}
//
//
//Copyright (C) 2008, David A. Lee.
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
