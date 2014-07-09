/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.util;

import net.sf.saxon.trans.XPathException;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonUtils {


	public static JsonNode toJsonNode( String json ) throws JsonProcessingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode actualObj = mapper.readTree(json);
		return actualObj;
	}
	public static JsonNode toJsonType( XValue value ) throws XPathException, JsonProcessingException, IOException
	{
		if( value.isNull() )
			return null ;
		
		if( value.isJson())
			return value.asJson();
			
	    ObjectMapper mapper = new ObjectMapper();
		Object obj = value.getJavaNative();

	
		if( obj instanceof Map )
			return mapper.convertValue(obj, ObjectNode.class);
		
		if( obj instanceof List )
			return  mapper.convertValue(obj, ArrayNode.class); ;
		if( obj instanceof Array )
			return  mapper.convertValue(obj, ArrayNode.class); ;
		
			
		if( obj instanceof Integer )
			return JsonNodeFactory.instance.numberNode((Integer)obj) ;
		if( obj instanceof Long )
			return JsonNodeFactory.instance.numberNode((Long)obj) ;
		if( obj instanceof Double )
			return JsonNodeFactory.instance.numberNode((Double)obj) ;
		
		if( obj instanceof Boolean )
			return JsonNodeFactory.instance.booleanNode((Boolean)obj) ;
		
		return mapper.convertValue(obj, JsonNode.class);
		
		
		
		
		
	}

	public static Object toNumber(XValue arg) throws XPathException, JsonProcessingException, IOException {
		Object obj = null;
		if( arg.isJson())
			obj = arg.asJson().asDouble();
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
		
		if( arg == null || arg.isNull() )
			return false ;
		if( arg.isString() ){
			String sv = arg.toString();
			return sv.equalsIgnoreCase("true") || sv.equals("1") ;
		}
		return new Boolean( arg.toBoolean() );
		
		
		
	}
	public static String toString(JsonNode value) throws JsonProcessingException
    {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure( SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS , true);
		return mapper.writeValueAsString(value);
    }
	public static JsonNode toJsonNode(InputStream is) throws JsonProcessingException, IOException
    {
		ObjectMapper mapper = new ObjectMapper();
	    return mapper.readTree(is);
    }
	public static void writeJsonNode(JsonNode result, PrintStream os) throws JsonGenerationException, JsonMappingException, IOException
    {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS , true).writeValue(os, result);
	    
    }

}



/*
 * Copyright (C) 2008-2014   David A. Lee.
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