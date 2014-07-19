/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.util;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.XValue;
import org.xmlsh.json.JSONSerializeOpts;
import org.xmlsh.sh.shell.SerializeOpts;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;

public class JSONUtils {

	/*
	 * Shared Object Mapper - uses default configuration so needs to be copied if custom configs are used
	 * 
	 */

	private static volatile  ObjectMapper _theObjectMapper = null ;
	private static volatile  JsonFactory  _theJsonFactory = null ;
	
	private static volatile  XmlFactory  _theXmlFactory = null ;
	private static volatile  JacksonXmlModule _theXmlModule = null;
	private static volatile  XmlMapper _theXmlMapper = null;
	private static Logger mLogger = LogManager.getLogger(JSONUtils.class);


	
	/* TEST CODE ... needs to go into AWS 
	abstract class IgnoreVolumeTypeEnum
	{
      @JsonSetter public abstract void setVolumeType(String vt);
      @JsonGetter public abstract String getVolumeType(String vt);

      

	}
	
	*/

	// Get a copy of the object mapper for configuring
	public static ObjectMapper newJsonObjectMapper() {
		return getJsonObjectMapper().copy();
	}

	public static ObjectMapper getJsonObjectMapper()
	{

		// lets play and avoid syncronization
		// on the off chance this is concurrent 2 mappers are created and one gets GC'd
		if( _theObjectMapper == null ) {

			ObjectMapper mapper = new ObjectMapper();
			mapper.registerModule( new JaxbAnnotationModule());

			mapper.configure( SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS , true);
			// mapper.configure(DeserializationFeature. x , on );
			mapper.configure(Feature.ALLOW_SINGLE_QUOTES, true);
			mapper.configure(Feature.ALLOW_UNQUOTED_FIELD_NAMES,true);
			mapper.configure(Feature.ALLOW_COMMENTS,true);
			mapper.configure(Feature.ALLOW_NON_NUMERIC_NUMBERS,true);
			mapper.configure(Feature.ALLOW_NUMERIC_LEADING_ZEROS,true);
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			
			
			/* Test code needs to go on AWS
		 mapper.addMixInAnnotations(com.amazonaws.services.ec2.model.EbsBlockDevice.class, IgnoreVolumeTypeEnum.class);
          */
			

			
			
			// other completely global configurations

			if( _theObjectMapper == null )
				_theObjectMapper = mapper ;

		}

		return _theObjectMapper ; 

	}
	public static JacksonXmlModule getXmlModule()
	{

		// lets play and avoid syncronization
		// on the off chance this is concurrent 2 mappers are created and one gets GC'd
		if( _theXmlModule == null ) {
			JacksonXmlModule module = new JacksonXmlModule();
			if( _theXmlModule == null ) 
				_theXmlModule = module ;
		}
		return _theXmlModule ;
	}
	
	public static XmlFactory getXmlFactory()
	{
		// lets play and avoid syncronization
		// on the off chance this is concurrent 2 mappers are created and one gets GC'd
		if( _theXmlFactory == null ) {
			XmlFactory factory = new XmlFactory();
			if( _theXmlFactory == null ) 
				_theXmlFactory = factory ;
		}
		return _theXmlFactory ;
	}
	
	
	
	public static XmlMapper getXmlMapper() {
		// lets play and avoid syncronization
		// on the off chance this is concurrent 2 mappers are created and one gets GC'd
		if( _theXmlMapper == null ) {
			XmlMapper mapper = new XmlMapper(getXmlFactory(),getXmlModule());
			mapper.registerModule( new JaxbAnnotationModule());
			if( _theXmlMapper == null ) 
				_theXmlMapper = mapper ;
		}
		return _theXmlMapper ;
	}
	
	public static ObjectWriter getObjectWriter() {
		return getJsonObjectMapper().writer();
	}
	
	public static ObjectReader getObjectReader() {
		return getJsonObjectMapper().reader();
	}


	public static JsonFactory getJsonFactory()
	{
		// lets play and avoid syncronization
		// on the off chance this is concurrent 2 mappers are created and one gets GC'd
		if( _theJsonFactory == null ) {

			JsonFactory factory = new JsonFactory();
			
			// other completely global configurations

			if( _theJsonFactory == null )
				_theJsonFactory = factory ;

		}
		return _theJsonFactory ;


	}


	public static JsonNode toJsonNode( String json ) throws InvalidArgumentException  {
		try {
			ObjectMapper mapper = getJsonObjectMapper();
			JsonNode actualObj = mapper.readTree(json);
			return actualObj;
		}
		catch( Exception e ){
			Util.wrapException("Exception converting json value",e,InvalidArgumentException.class);	
			return null ; // SNH 

		}
	}
	public static JsonNode toJsonType( XValue value ) throws InvalidArgumentException
	{
		if( value.isNull() )
			return null ;

		try {
			if( value.isJson())
				return value.asJson();

			ObjectMapper mapper = getJsonObjectMapper();
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
		catch (Exception e) {
			Util.wrapException("Exception converting JSON value",e,InvalidArgumentException.class);	
			return null ; // SNH 
		}


	}

	public static NumericNode toJsonNumber(XValue arg) throws InvalidArgumentException  {
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

		try {
			ObjectMapper mapper = getJsonObjectMapper();
			return mapper.readValue(str, NumericNode.class );
		}catch( Exception e ){
			Util.wrapException("Exception converting JSON  value",e,InvalidArgumentException.class);	
			return null ; // SNH 

		}

	}

	public static BooleanNode toJsonBoolean(XValue arg) throws InvalidArgumentException  {

		try {
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
		catch( Exception e ){
			Util.wrapException("Exception converting JSON value",e,InvalidArgumentException.class);	

			return null ; // SNH 

		}



	}
	public static String jsonToString(JsonNode value) throws JsonProcessingException
	{
		ObjectMapper mapper = getJsonObjectMapper();
		return mapper.writeValueAsString(value);
	}
	
	/*
	 * Read a json node from an input stream
	 */
	public static JsonNode readJsonNode(InputStream is) throws JsonProcessingException, IOException
	{
		ObjectMapper mapper = getJsonObjectMapper();
		return mapper.readTree(is);
	}
	
	
	/*
	 * Read an object from Json 
	 */
	public static <T> T   readJsonValue(InputStream is,Class<T> cls) throws JsonProcessingException, IOException
	{
		ObjectMapper mapper = getJsonObjectMapper();
		return mapper.readValue(is, cls);
	}
	
	public static void writeJsonNode(JsonNode result, PrintStream os) throws JsonGenerationException, JsonMappingException, IOException
	{
		ObjectMapper mapper = getJsonObjectMapper(); 
		mapper.writeValue(os, result);

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

		ObjectMapper mapper = getJsonObjectMapper();
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
		ObjectMapper mapper = getJsonObjectMapper();

		if( node.isObject() ) {
			return mapper.convertValue( node , Map.class);
		}
		return node.toString(); // WTF
	}

	public static void writeJsonNode(JsonNode value, OutputStream os, SerializeOpts opt) throws JsonGenerationException, JsonMappingException, IOException
	{

		ObjectMapper mapper = getJsonObjectMapper();
		mapper.writeValue(os, value);
	}

	public static InputStream asInputStream(JsonNode value, SerializeOpts opt) throws JsonGenerationException, JsonMappingException, IOException
	{

		/* TODO - figure out a streaming method */
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		writeJsonNode( value ,bos , opt );
		bos.flush();
		bos.close();
		return new ByteArrayInputStream( bos.toByteArray());

	}

	public static JsonGenerator createGenerator(OutputStream os, JSONSerializeOpts jopts) throws IOException
    {
		JsonGenerator gen = getJsonFactory().createGenerator(os);
		if( jopts.getPretyPrint())
			gen.useDefaultPrettyPrinter();
		return gen;
    }

	public static void safeClose(JsonGenerator generator) 
    {
		if( generator != null ) {
			try {
	            generator.close();
            } catch (IOException e) {
	          mLogger.info( "Exception closing JsonGenerator", e );
            }
		}
    }

	public static void safeClose(JsonParser parser)
    {
		if( parser != null ) {
			try {
				parser.close();
            } catch (IOException e) {
	          mLogger.info( "Exception closing JsonParser", e );
            }
		}
	    
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