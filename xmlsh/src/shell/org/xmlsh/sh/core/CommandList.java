/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.sh.core;

import org.xmlsh.sh.shell.Shell;


public class CommandList extends BinaryOpCommand
{

	public CommandList(Command left, String op , Command right) {
		super(left, null , right);
		if( op.equals("&"))
			left.setWait(false);

	}

	/* (non-Javadoc)
	 * @see org.xmlsh.sh.core.Command#exec(org.xmlsh.sh.shell.Shell)
	 */
	@Override
	public int exec(Shell shell) throws Exception {
		int leftRet = shell.exec(mLeft);
		if( mRight == null || ! shell.keepRunning() )
			return leftRet ;
		else
			return shell.exec( mRight );
		
	}
	
	/* 
	 * Override setWait to set the wait flag on the rightmost command if it exists
	 * This fixes the precidence of "&" applied to command lists such that
	 * a & b & 
	 * produces 2 seperate jobs instead of 1 job of "a & b" (which in turn would produce a sub-job of "a")
	 * 
	 */
	@Override
	public void setWait( boolean flag )
	{
		if( mRight != null )
			mRight.setWait(flag);
		else
		if( mLeft != null )
			mLeft.setWait(flag);
		else
			super.setWait(flag);
		
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

