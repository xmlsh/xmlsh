/**
 * $Id: $
 * $DateTime: $
 *
 */

package org.xmlsh.commands;

import java.io.OutputStream;
import java.util.List;

import javax.xml.transform.sax.TransformerHandler;

import org.xmlsh.core.XCommand;
import org.xmlsh.core.XEnvironment;
import org.xmlsh.core.XValue;
import org.xmlsh.types.XFile;
import org.xmlsh.util.Util;


public class xpwd extends XCommand
{
	
	
	public static void main( String args[] ) throws Exception
	{
		xpwd cmd = new xpwd();

		cmd.run( args );
		
		
		
	}

	
	
	public int run(  List<XValue> args , XEnvironment env )	throws Exception
	{
		

		XFile file = new XFile(env.getShell().getCurdir());
		

		OutputStream stdout = env.getStdout();
	      
		TransformerHandler hd = Util.getTransformerHander(stdout);

		
		hd.startDocument();
		
		
		file.serialize(hd);
		
		hd.endDocument();
		
		
		
		
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
