/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.sh.core;

import java.io.PrintWriter;

public abstract class BinaryOpCommand extends CommandExpr {

	protected 		CommandExpr		mLeft;
	protected		String	 	mOp;
	protected		CommandExpr		mRight;
	@Override
	public	boolean		isSimple() { return false ; }


	public BinaryOpCommand( CommandExpr left, String op , CommandExpr right )
	{
		mLeft = left;
		mOp = op ;
		mRight = right;
		if( mLeft != null )
			setLocation( mLeft );
		else
			setLocation( mRight );
	}

	@Override
	public void print( PrintWriter out, boolean bExec ){
		if( bExec )
			return ;
		if( mLeft != null )
			mLeft.print(out, bExec);
		out.println(mOp);
		if( mRight != null )
			mRight.print(out, bExec);
	}


	@Override
	public String getName() {
		return mLeft != null ? mLeft.getName() : 
			mRight != null ? mRight.getName() : mOp ;
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

