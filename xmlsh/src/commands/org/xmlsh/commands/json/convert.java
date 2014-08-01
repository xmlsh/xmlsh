/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.commands.json;

import org.xmlsh.core.BuiltinFunctionCommand;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.XValue;
import org.xmlsh.json.JSONUtils;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.types.TypeFamily;
import org.xmlsh.util.JavaUtils;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class convert extends BuiltinFunctionCommand {

	public convert()
	{
		super("convert");
	}

	@Override
	public XValue run(Shell shell, List<XValue> args) throws ClassNotFoundException, CoreException, JsonParseException, JsonMappingException, IOException 
	{
		requires( args.size() == 2 , " two arguments required");

		Class<?> cls = null ;
		Object from = args.get(0).asObject();
		cls = JavaUtils.convertToClass(args.get(1) , shell );
		if( cls == null )
			cls = JsonNode.class ;

		ObjectMapper mapper = JSONUtils.getJsonObjectMapper();
		Object value = mapper.convertValue(from, cls);
		return new XValue( TypeFamily.XTYPE , value );
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
