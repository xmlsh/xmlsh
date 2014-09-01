/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.stax.functions;

import org.xmlsh.core.BuiltinFunctionCommand;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.types.TypeFamily;

import java.io.IOException;
import java.util.List;

public class newEventReader extends BuiltinFunctionCommand {



	public newEventReader()
	{
		super("newEventReader");
	}

	@Override
	public XValue run(Shell shell, List<XValue> args) throws CoreException, IOException {
		if( args.size() == 0 )
			return XValue.asXValue( TypeFamily.JAVA, shell.getEnv().getStdin().asXMLEventReader(shell.getSerializeOpts()));
		else
			return XValue.asXValue( TypeFamily.JAVA, shell.getEnv().getInput(args.get(0)).asXMLEventReader(shell.getSerializeOpts()));



	}

}



//
//
//Copyright (C) 2008-2014 David A. Lee.
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
