/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.builtin;

import java.util.List;

import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.XdmNode;
import org.xmlsh.core.BuiltinCommand;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.XValue;
import org.xmlsh.core.XVariable;
import org.xmlsh.sh.shell.Shell;

public class xread extends BuiltinCommand {

	/*
	 *  Read a line of text from stdin and assign to variables
	 *  
	 */


	public int run( List<XValue> args ) throws Exception {

		if( args.size() != 1 )
			throw new InvalidArgumentException("requires 1 argument");
		
		mShell.getEnv().unsetVar(args.get(0).toString());

		
		Processor proc = Shell.getProcessor();
		DocumentBuilder builder = proc.newDocumentBuilder();

		XdmNode node = builder.build(mShell.getEnv().getStdin().asSource());
		mShell.getEnv().setVar(
				new XVariable(args.get(0).toString(), new XValue(node)));


		return 0;
	}
}


//Copyright (C) 2008, David A. Lee.

//The contents of this file are subject to the "Simplified BSD License" (the "License");
//you may not use this file except in compliance with the License. You may obtain a copy of the
//License at http://www.opensource.org/licenses/bsd-license.php 

//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied.
//See the License for the specific language governing rights and limitations under the License.

//The Original Code is: all this file.

//The Initial Developer of the Original Code is David A. Lee

//Portions created by (your name) are Copyright (C) (your legal entity). All Rights Reserved.

//Contributor(s): none.

