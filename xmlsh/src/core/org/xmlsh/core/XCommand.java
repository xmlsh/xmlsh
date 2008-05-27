/**
 * $Id: $
 * $DateTime: $
 *
 */

package org.xmlsh.core;


import org.xmlsh.sh.shell.Shell;

public abstract class XCommand implements ICommand {
	
	
	
	
	protected static void throwInvalidArg(XEnvironment env, String string)
			throws InvalidArgumentException {
				env.getShell().printErr(string);
				throw new InvalidArgumentException( string );
				
			}



	/* (non-Javadoc)
	 * @see org.xmlsh.core.ICommand#run(org.xmlsh.sh.shell.Shell, java.lang.String[])
	 */
	public int run(Shell shell, String cmd, XValue[] args) throws Exception 
	{

		
		return run(  args , shell.getEnv() );
		
	}
	


	// Command line run using System in/out/err
	public	int run( String[] args ) throws Exception
	{
		
		XEnvironment env = new XEnvironment(new Shell());
		
		XValue[]	vargs = new XValue[ args.length ];
		for( int i = 0 ; i < args.length ; i++ )
			vargs[i] = new XValue( args[i]);
		
		
		
	    try {
	    	return  run( vargs , env );
	    } finally {
			
	    }
			

		
	}
	
	
	abstract public int run( XValue args[] , XEnvironment env ) throws Exception;
	
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
