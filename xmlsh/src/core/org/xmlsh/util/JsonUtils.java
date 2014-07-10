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
import java.util.ArrayList;
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
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.node.ValueNode;

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

	public static NumericNode toJsonNumber(XValue arg) throws XPathException, JsonProcessingException, IOException {
		String str = null;
		if( arg.isJson()) {
			JsonNode j  = arg.asJson();
			if( j.isNumber())
				return (NumericNode) j;
			else
				str = j.asText();
		}
			
		else
			str = arg.toString();
		
	    ObjectMapper mapper = new ObjectMapper();
	    return mapper.readValue(str, NumericNode.class );

	}
	
	public static BooleanNode toJsonBoolean(XValue arg) throws UnexpectedException, XPathException, JsonProcessingException, IOException {
		 
		boolean b = false ;
		if( arg != null &&! arg.isNull() ) {
			if( arg.isJson() )
				b = arg.asJson().asBoolean();
			else
			if( arg.isString() ) {
				String s=arg.toString();
				b = ( s.equalsIgnoreCase("true") || s.equals("1")) ;
			} else

			b = arg.toBoolean();
		} 
		return JsonNodeFactory.instance.booleanNode( b );
		
		
		
	}
	public static String jsonToString(JsonNode value) throws JsonProcessingException
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
	public static NullNode jsonNull()
    {
	   return JsonNodeFactory.instance.nullNode();
    }
	public static TextNode toJsonString(String string)
    {
		return JsonNodeFactory.instance.textNode(string);
    }

	public static TextNode toJsonString(XValue xv )
    {
		if( xv == null || xv.isNull())
			return toJsonString( (String) null );
	
		if( xv.isString() )
			return toJsonString( xv.toString());
		
		if( xv.isAtomic() )
			return toJsonString( xv.toString() );

		ObjectMapper mapper = new ObjectMapper();
		return mapper.convertValue( xv.asObject() , TextNode.class );
    
    }
	public static Object asJavaNative(JsonNode node)
    {
	   if( node.isValueNode() ) {
		   ValueNode value = (ValueNode) node;
		   if( value.isNumber())
			   return ((NumericNode)value).numberValue();
		   if( value.isBoolean() )
			   return value.asBoolean();
		   if( value.isTextual())
			   return value.asText().toString();
		   if( value.isNull())
			   return null;
	   }
		   
		if( node.isArray()) {
			ArrayNode a = (ArrayNode) node;
			ArrayList<Object> al = new ArrayList<Object>( a.size());
			for( JsonNode an : a ) {
				al.add( asJavaNative( an ));
			}
			return al;
		}
		ObjectMapper mapper = new ObjectMapper();

		if( node.isObject() ) {
			return mapper.convertValue( node , Map.class);
		}
		return node.toString(); // WTF
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