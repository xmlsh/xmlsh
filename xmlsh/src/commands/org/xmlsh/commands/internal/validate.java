/**
 * $Id: colon.java 21 2008-07-04 08:33:47Z daldei $
 * $Date: 2008-07-04 04:33:47 -0400 (Fri, 04 Jul 2008) $
 *
 */

package org.xmlsh.commands.internal;

import java.util.List;

import org.xmlsh.core.BuiltinCommand;
import org.xmlsh.core.CommandFactory;
import org.xmlsh.core.ICommand;
import org.xmlsh.core.Options;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.core.Command;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.util.Util;

public class validate extends XCommand  {

	/*
	 * Evaluate all arguments as as string
	 * and parse them as a command 
	 * 
	 */
	
	public int run(  List<XValue> args ) throws Exception {
		


		XValue port = args.get(0);
		@SuppressWarnings("unused")
		ICommand icmd = CommandFactory.getInstance().getScript(mShell, port.toString() ,true,getLocation());

		
			return  0 ;
			
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
