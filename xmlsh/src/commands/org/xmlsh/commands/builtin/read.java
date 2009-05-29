/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.commands.builtin;

import java.io.InputStream;
import java.util.List;

import org.xmlsh.core.BuiltinCommand;
import org.xmlsh.core.InputPort;
import org.xmlsh.core.XValue;
import org.xmlsh.core.XVariable;
import org.xmlsh.util.Util;

public class read extends BuiltinCommand {
	
	/*
	 *  Read a line of text from stdin and assign to variables
	 *  
	 */


	public int run(  List<XValue> args ) throws Exception {

		// Unset all args
		for( XValue arg : args )
			mShell.getEnv().unsetVar( arg.toString() );


		InputPort stdin = mShell.getEnv().getStdin();
		InputStream is = stdin.asInputStream(getSerializeOpts());
		String line = Util.readLine( is );
		if( line == null )
			return 1; // EOF

		String ifs = getIFSRegex();
		String[] results = line.split(ifs,args.size());
		for( int i = 0 ; i < args.size() ; i++ )
			if( i < results.length )

			mShell.getEnv().setVar(
					new XVariable(args.get(i).toString(), new XValue(results[i])));

		// stdin.close(); // Crashes ... why ?
		is.close();

		return 0;
	}

	private String getIFSRegex() {
		XValue xifs = mShell.getEnv().getVarValue("IFS");
		String ifs = xifs == null ? " \t" : xifs.toString();
		
		return "[" + ifs + "]" ;
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
