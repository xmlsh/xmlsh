/**
 * $Id: $
 * $DateTime: $
 *
 */

package org.xmlsh.builtin;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.xmlsh.core.BuiltinCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.core.XVariable;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.Util;

public class read extends BuiltinCommand {
	
	/*
	 *  Read a line of text from stdin and assign to variables
	 *  
	 */
	
	
	public int run( Shell shell,String cmd,  List<XValue> args ) throws Exception {
			InputStream is = shell.getEnv().getStdin();
			String line = Util.readLine( is );
			
			XValue xifs = shell.getEnv().getVarValue("IFS");
			String ifs = xifs == null ? " \t" : xifs.toString();
			
			ArrayList<String> 	list = new ArrayList<String>();
			StringTokenizer	tok = new StringTokenizer( line , ifs );
			
			int arg = 0;
			while( tok.hasMoreTokens()){
				String s=tok.nextToken();
				if( arg < args.size() )
					shell.getParentEnv().setVar(
							new XVariable(args.get(arg++).toString(), new XValue(s)));
				
			}
			
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
