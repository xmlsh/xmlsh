/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.json.functions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.xmlsh.core.AbstractBuiltinFunction;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.XValue;
import org.xmlsh.json.JSONUtils;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.types.TypeFamily;
import org.xmlsh.types.XTypeUtils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class json extends AbstractBuiltinFunction {

	public json()
	{
		super("json");
	}

	@Override
	public XValue run(Shell shell, List<XValue> args) throws ClassNotFoundException, CoreException, JsonParseException, JsonMappingException, IOException 
	{
		List<JsonNode> nodes = new ArrayList<JsonNode>(args.size());
		ObjectMapper mapper = JSONUtils.getJsonObjectMapper();
		for( XValue arg : args ) {
			Object o = arg.asObject();
			nodes.add( mapper.valueToTree(o) );
		}
		if( nodes.isEmpty())
			return XTypeUtils.getInstance(TypeFamily.JSON).nullXValue();

		else
			if( nodes.size() > 1 ) 
				return XValue.newXValue(TypeFamily.JSON, mapper.createArrayNode().addAll(nodes));
			else
				return XValue.newXValue( TypeFamily.JSON, nodes.get(0 ) );

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
