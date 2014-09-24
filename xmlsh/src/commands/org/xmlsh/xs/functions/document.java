/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.xs.functions;

import java.util.List;

import org.xmlsh.core.AbstractBuiltinFunction;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.S9Util;

public class document extends AbstractBuiltinFunction {



	public document()
	{
		super("document");
	}

	@Override
	public XValue run(Shell shell, List<XValue> args) throws CoreException 
	{
		if( args.size() != 1 )
			throw new InvalidArgumentException("document() requres a single argument");


		return XValue.newXValue(S9Util.wrapDocument(args.get(0).asXdmNode() ));



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
