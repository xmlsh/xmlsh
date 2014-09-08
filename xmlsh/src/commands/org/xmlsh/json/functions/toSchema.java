/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.json.functions;

import java.util.List;

import org.xmlsh.core.AbstractBuiltinFunction;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.XValue;
import org.xmlsh.json.JSONUtils;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.types.TypeFamily;
import org.xmlsh.util.JavaUtils;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsonschema.JsonSchema;

public class toSchema extends AbstractBuiltinFunction {

	public toSchema()
	{
		super("to-schema");
	}


	@SuppressWarnings("deprecation")
	@Override
	public XValue run(Shell shell, List<XValue> args) throws ClassNotFoundException, CoreException, JsonMappingException {

		if( args.size() == 0)
			throw new InvalidArgumentException("usage: to-schema( class )");

		XValue arg = args.get(0);

		Class<?> cls = JavaUtils.convertToClass( arg, shell );


		ObjectMapper mapper = JSONUtils.getJsonObjectMapper();

		@SuppressWarnings("deprecation")
		JsonSchema schema = mapper.generateJsonSchema(cls);

		return XValue.newXValue(TypeFamily.JSON,  schema.getSchemaNode());

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
