/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.util;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Map;

import net.sf.saxon.trans.XPathException;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;

import com.jayway.jsonpath.JsonModel;
import com.sun.xml.internal.bind.v2.schemagen.xmlschema.List;

public class JsonUtils {


	public static Object toJsonType( XValue value ) throws XPathException
	{
		if( value.isNull() )
			return null ;
		
		if( value.isJson()){
			JsonModel model = value.asJson();
			return model.getJsonObject();
			
			
		}
		
		Object obj = value.getJavaNative();

	
		if( obj instanceof Map )
			return obj ;
		
		if( obj instanceof List )
			return obj ;
		if( obj instanceof Array )
			return Arrays.asList(obj);
		
		if( obj instanceof Integer )
			return obj ;
		if( obj instanceof Long )
			return obj ;
		if( obj instanceof Double )
			return obj ;
		
		if( obj instanceof Number )
          	return obj ;
		
		if( obj instanceof Boolean )
			return obj ;
		
		return obj.toString() ;
		
		
		
		
		
		
	}

	public static Object toNumber(XValue arg) throws XPathException {
		Object obj = null;
		if( arg.isJson())
			obj = arg.asJson().getJsonObject();
		else
		
			obj = arg.getJavaNative();
		
		
		if( obj instanceof String ){

			String sobj = arg.toString() ;
			if( sobj.contains("." ))
				return JavaUtils.convert( obj ,  Double.class );
			else
				return JavaUtils.convert( obj , Long.class );
			
		}
		
		if( JavaUtils.isIntClass( obj.getClass() ))
			return 	JavaUtils.convert(obj, Long.class);
		else
			return JavaUtils.convert( obj ,  Double.class );
	}

	public static Boolean toBoolean(XValue arg) throws UnexpectedException, XPathException {
		
		return new Boolean( arg.toBoolean() );
		
		
		
	}

	public static JsonModel getModel(XValue v) {
		Object o = v.asObject() ;
		if( o instanceof JsonModel )
			return (JsonModel)o;
		return JsonModel.model(o);
	}
}



/*
 * Copyright (C) 2008-2012 David A. Lee.
 * 
 * The contents of this file are subject to the "Simplified BSD License" (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.opensource.org/licenses/bsd-license.php 

 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied.
 * See the License for the specific language governing rights and limitations under the License.
 *
 * The Original Code is: all this file.
 *
 * The Initial Developer of the Original Code is David A. Lee
 *
 * Portions created by (your name) are Copyright (C) (your legal entity). All Rights Reserved.
 *
 * Contributor(s): David A. Lee
 * 
 */