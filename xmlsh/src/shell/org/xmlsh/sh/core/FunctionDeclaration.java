/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.sh.core;

import org.xmlsh.core.EvalEnv;
import org.xmlsh.core.IFunctionDecl;
import org.xmlsh.sh.shell.Shell;

import java.io.PrintWriter;

public class FunctionDeclaration extends CommandExpr implements IFunctionDecl {
	private ICommandExpr mBody;

	@Override
	public	boolean		isSimple() { return false ; }

	public FunctionDeclaration( String name , ICommandExpr body )
	{
		super(name);
		mBody = body;
	}


	/**
	 * @return the body
	 */
	@Override
	public ICommandExpr getBody() {
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

	@Override
	public EvalEnv argumentEnv(EvalEnv parent)
	{
		// Add normal expansions 
		return parent.withFlagsSet( EvalEnv.commandArgsFlags() );
	}

	@Override
	public EvalEnv returnEnv(EvalEnv parent)
	{ 
		return parent.withFlagsMasked( EvalEnv.returnValueMask() ); 
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
