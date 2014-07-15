/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.sh.core;

import org.xmlsh.core.IFunction;
import org.xmlsh.sh.shell.Shell;

import java.io.PrintWriter;

public class FunctionDeclaration extends Command implements IFunction {
	private Command mBody;
	
	public	boolean		isSimple() { return false ; }

	public FunctionDeclaration( String name , Command body )
	{
		super(name);
		mBody = body;
	}
	
	
	/**
	 * @return the body
	 */
	public Command getBody() {
		return mBody;
	}





	/* (non-Javadoc)
	 * @see org.xmlsh.sh.core.Command#print(java.io.PrintWriter)
	 */
	@Override
	public void print(PrintWriter out, boolean bExec) {
		out.println( getName() + " ()");
		if( ! bExec )
			mBody.print( out , bExec);
		
	}
	@Override
	public int exec(Shell shell) throws Exception {
		
		shell.declareFunction( this );	
		return 0;
		
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
