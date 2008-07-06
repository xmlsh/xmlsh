/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.sh.core;

import org.xmlsh.sh.shell.Shell;


public class AndOr extends BinaryOpCommand {

	public AndOr(Command left, String op, Command right) {
		super(left, op, right);
	
	}
	/* (non-Javadoc)
	 * @see org.xmlsh.sh.core.Command#exec(org.xmlsh.sh.shell.Shell)
	 */
	@Override
	public int exec(Shell shell) throws Exception {
		int leftRet = shell.exec(mLeft);
		if( ! shell.keepRunning() )
			return leftRet ;
		
		if( mOp.equals("||") ){
			if( Shell.toBool(leftRet))
				return leftRet;
			return shell.exec(mRight);
			
		} else
		if( mOp.equals("&&")){
			if( ! Shell.toBool(leftRet))
				return leftRet ; 	
			
			int rightRet = shell.exec(mRight);
			return shell.fromBool(Shell.toBool( leftRet ) && 
					Shell.toBool(rightRet) );

		} else
			return leftRet;
		
		
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
