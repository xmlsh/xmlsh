/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.modules.json;

import java.util.ArrayList;
import java.util.List;

import org.xmlsh.core.AbstractBuiltinFunction;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.XValue;
import org.xmlsh.json.JSONUtils;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.types.TypeFamily;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class array extends AbstractBuiltinFunction {

	public array()
	{
		super("array");
	}

	@Override
	public XValue run(Shell shell, List<XValue> args) throws InvalidArgumentException {

		ArrayList<Object> list = new ArrayList<Object>();


		ObjectMapper mapper = JSONUtils.getJsonObjectMapper();
		ArrayNode node = mapper.createArrayNode();

		for( XValue arg : args ){
			node.add(JSONUtils.toJsonType(arg) );
		}


		return XValue.newXValue( TypeFamily.JSON ,node );
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
