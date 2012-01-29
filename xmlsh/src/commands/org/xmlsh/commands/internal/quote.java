/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.commands.internal;

import java.util.List;

import org.xmlsh.core.BuiltinFunctionCommand;
import org.xmlsh.core.Options;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.JavaUtils;

public class quote extends BuiltinFunctionCommand {

	public quote() {
		super("quote");
	
	}

	@Override
	public XValue run(Shell shell, List<XValue> args) throws Exception {
		
		StringBuffer	sb = new StringBuffer("\"");
		for( XValue arg : args )
			sb.append( escape( arg.toString() ) );
		sb.append("\"");
		return new XValue( sb.toString());

		
	}

	private String escape(String string) 
	{
		if( string.indexOf('"') < 0 && string.indexOf('\\') < 0 ) 
				return string ;
		StringBuffer sb = new StringBuffer();
		char c;
		for( int i = 0 ; i < string.length() ; i++ ){
			c = string.charAt(i);
			if( c == '"' || c == '\\' )
				sb.append('\\');
			sb.append(c);
			
				
		}
		return sb.toString();
		
	}

}



//
//
//Copyright (C) 2008,2009,2010,2011,2012 David A. Lee.
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
