/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.xs.functions;

import java.util.List;

import net.sf.saxon.s9api.QName;

import org.xmlsh.core.AbstractBuiltinFunction;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.S9Util;

public class attribute extends AbstractBuiltinFunction {

	public attribute()
	{
		super("attribute");
	}

	@Override
	public XValue run(Shell shell, List<XValue> args) throws InvalidArgumentException 
	{
		if( args.size() != 2 )
			throw new InvalidArgumentException("Expected name value");

		QName name = args.get(0).asQName(shell);


		return S9Util.createAttribute(shell, name, args.get(1).toString() );






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
