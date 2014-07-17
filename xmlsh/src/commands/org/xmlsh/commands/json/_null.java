/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.commands.json;

import net.sf.saxon.trans.XPathException;
import org.xmlsh.core.BuiltinFunctionCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.JSONUtils;

import java.util.List;

public class _null extends BuiltinFunctionCommand {

	public _null()
	{
		super("null");
	}
	
	@Override
	public XValue run(Shell shell, List<XValue> args) throws XPathException {
		
		return new XValue( JSONUtils.jsonNull() );
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
