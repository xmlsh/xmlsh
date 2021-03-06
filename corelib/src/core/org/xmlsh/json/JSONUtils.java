/**
 * $Id: $
 * $Date: $
 * 
 */

package org.xmlsh.json;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.types.TypeFamily;
import org.xmlsh.types.xtypes.XValueList;
import org.xmlsh.util.Util;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.ContainerNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.POJONode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import com.fasterxml.jackson.databind.type.SimpleType;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;

public class JSONUtils {

    
    /*
     * NOTE TO DAL: 
     * DO NOT try to extend XmlFatory to do renaming - its too invasive and complicated,
     * all the right places to override are final methods and its very tricky
     * Instead use the XMLRewritingStreamXXX classes
     * Or if you must fork the xml dataformat project and change the source
     *      
     */
    private static volatile ObjectMapper _theObjectMapper = null;

    private static volatile XmlFactory _theXmlFactory = null;
    private static volatile JacksonXmlModule _theXmlModule = null;
    private static volatile XmlMapper _theXmlMapper = null;
    private static Logger mLogger = LogManager.getLogger();

    private final static JavaType JSON_NODE_TYPE = SimpleType
            .constructUnsafe(JsonNode.class);
    
    static {
      com.jayway.jsonpath.Configuration.setDefaults(new com.jayway.jsonpath.Configuration.Defaults() {
            {
               
            }

        
            private final JsonProvider jsonProvider = new JacksonJsonNodeJsonProvider();
            private final MappingProvider mappingProvider = new JacksonMappingProvider();

            @Override
            public JsonProvider jsonProvider() {
                return jsonProvider;
            }

            @Override
            public MappingProvider mappingProvider() {
                return mappingProvider;
            }

            public Set<Option> options() {
                return EnumSet.noneOf(Option.class);
            }
        });
    }

    /*
     * TEST CODE ... needs to go into AWS
     * abstract class IgnoreVolumeTypeEnum
     * {
     * 
     * @JsonSetter public abstract void setVolumeType(String vt);
     * 
     * @JsonGetter public abstract String getVolumeType(String vt);
     * 
     * 
     * 
     * }
     */

    // Get a copy of the object mapper for configuring
    public static ObjectMapper newJsonObjectMapper() {
        return getJsonObjectMapper().copy();
    }

    public static ObjectMapper getJsonObjectMapper()
    {
        return getJsonObjectMapper(null);
    }
    
    public static ObjectMapper getJsonObjectMapper(SerializeOpts opts)
    {

        // lets play and avoid syncronization
        // on the off chance this is concurrent 2 mappers are created and one gets GC'd
        if (_theObjectMapper == null) {

            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JaxbAnnotationModule());

            mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS,
                    true);
            // mapper.configure(DeserializationFeature. x , on );
            mapper.configure(Feature.ALLOW_SINGLE_QUOTES, true);
            mapper.configure(Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
            mapper.configure(Feature.ALLOW_COMMENTS, true);
            mapper.configure(Feature.ALLOW_NON_NUMERIC_NUMBERS, true);
            mapper.configure(Feature.ALLOW_NUMERIC_LEADING_ZEROS, true);
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                    false);

            /*
             * Test code needs to go on AWS
             * mapper.addMixInAnnotations(com.amazonaws.services.ec2.model.EbsBlockDevice.class, IgnoreVolumeTypeEnum.class);
             */

            // other completely global configurations

            if (_theObjectMapper == null)
                _theObjectMapper = mapper;

        }
        
        if( opts != null && _theObjectMapper.getSerializationConfig().isEnabled(SerializationFeature.INDENT_OUTPUT) 
                   != opts.getIndentJson() )
            return
                    _theObjectMapper.configure(SerializationFeature.INDENT_OUTPUT, opts.getIndentJson());

        return _theObjectMapper;

    }
    


    public static JacksonXmlModule getXmlModule()
    {

        // lets play and avoid syncronization
        // on the off chance this is concurrent 2 mappers are created and one gets GC'd
        if (_theXmlModule == null) {
            JacksonXmlModule module = new RenamingXmlModule();
            if (_theXmlModule == null)
                _theXmlModule = module;
        }
        return _theXmlModule;
    }

    public static XmlFactory getXmlFactory()
    {
        // lets play and avoid syncronization
        // on the off chance this is concurrent 2 mappers are created and one gets GC'd
        if (_theXmlFactory == null) {
            XmlFactory factory = new XmlFactory();
            if (_theXmlFactory == null)
                _theXmlFactory = factory;
        }
        return _theXmlFactory;
    }

    public static XmlMapper getXmlMapper() {
        // lets play and avoid syncronization
        // on the off chance this is concurrent 2 mappers are created and one gets GC'd
        if (_theXmlMapper == null) {
            XmlMapper mapper = newXmlMapper();
            if (_theXmlMapper == null)
                _theXmlMapper = mapper;
        }
        return _theXmlMapper;
    }

    public static XmlMapper newXmlMapper() {
        XmlMapper mapper = new XmlMapper(getXmlFactory(), getXmlModule());
        mapper.registerModule(new JaxbAnnotationModule());
        return mapper;
    }

    public static ObjectWriter getObjectWriter() {
        return getJsonObjectMapper().writer();
    }

    public static ObjectReader getObjectReader() {
        return getJsonObjectMapper().reader();
    }

    public static JsonFactory getJsonFactory()
    {

        // Need to use ObjectMapper factory to get type mapping 
        return getJsonObjectMapper().getFactory();

    }

    public static JsonNode toJsonNode(String json)
            throws InvalidArgumentException {
        try {
            ObjectMapper mapper = getJsonObjectMapper();
            JsonNode actualObj = mapper.readTree(json);
            return actualObj;
        } catch (Exception e) {
            Util.wrapException("Exception converting json value", e,
                    InvalidArgumentException.class);
            return null; // SNH

        }
    }

    public static JsonNode toJsonType(XValue value)
            throws InvalidArgumentException
    {
        if (value.isNull())
            return null;

        try {
            if (value.isJson())
                return value.asJson();

            ObjectMapper mapper = getJsonObjectMapper();
            Object obj = value.getJavaNative();

            if (obj instanceof Map)
                return mapper.convertValue(obj, ObjectNode.class);

            if (obj instanceof List)
                return mapper.convertValue(obj, ArrayNode.class);;
            if (obj instanceof Array)
                return mapper.convertValue(obj, ArrayNode.class);;

            if (obj instanceof Integer)
                return JsonNodeFactory.instance.numberNode((Integer) obj);
            if (obj instanceof Long)
                return JsonNodeFactory.instance.numberNode((Long) obj);
            if (obj instanceof Double)
                return JsonNodeFactory.instance.numberNode((Double) obj);

            if (obj instanceof Boolean)
                return JsonNodeFactory.instance.booleanNode((Boolean) obj);

            return mapper.convertValue(obj, jsonNodeClass());

        } catch (Exception e) {
            Util.wrapException("Exception converting JSON value", e,
                    InvalidArgumentException.class);
            return null; // SNH
        }

    }

    public static Class<JsonNode> jsonNodeClass()
    {
        return JsonNode.class;
    }

    public static JavaType jsonNodeType()
    {
        return JSON_NODE_TYPE;

    }

    public static NumericNode toJsonNumber(XValue arg)
            throws InvalidArgumentException {
        String str = null;
        if (arg.isJson()) {
            JsonNode j = arg.asJson();
            if (j.isNumber())
                return (NumericNode) j;
            else
                str = j.asText();
        }

        else
            str = arg.toString();

        try {
            ObjectMapper mapper = getJsonObjectMapper();
            return mapper.readValue(str, NumericNode.class);
        } catch (Exception e) {
            Util.wrapException("Exception converting JSON  value", e,
                    InvalidArgumentException.class);
            return null; // SNH

        }

    }

    public static BooleanNode toJsonBoolean(XValue arg)
            throws InvalidArgumentException {

        try {
            boolean b = false;
            if (arg != null && !arg.isNull()) {
                if (arg.isJson())
                    b = arg.asJson().asBoolean();
                else if (arg.isString()) {
                    String s = arg.toString();
                    b = (s.equalsIgnoreCase("true") || s.equals("1"));
                }
                else

                    b = arg.toBoolean();
            }
            return JsonNodeFactory.instance.booleanNode(b);
        } catch (Exception e) {
            Util.wrapException("Exception converting JSON value", e,
                    InvalidArgumentException.class);

            return null; // SNH

        }

    }

    public static String jsonToString(JsonNode value, boolean serialized)
            throws JsonProcessingException
    {
      
      if( value == null ) return "";
      if( serialized ){
        ObjectMapper mapper = getJsonObjectMapper();
        return mapper.writeValueAsString(value);
      } else
       if( value.isValueNode() )
          return value.asText("");
       else
         return value.toString();
    }

    /*
     * Read a json node from an input stream
     */
    public static JsonNode readJsonNode(InputStream is)
            throws JsonProcessingException, IOException
    {
        ObjectMapper mapper = getJsonObjectMapper();
        return mapper.readTree(is);
    }

    /*
     * Read an object from Json
     */
    public static <T> T readJsonValue(InputStream is, Class<T> cls)
            throws JsonProcessingException, IOException
    {
        ObjectMapper mapper = getJsonObjectMapper();
        return mapper.readValue(is, cls);
    }

    public static void writeJsonNode(JsonNode result, PrintStream os)
            throws JsonGenerationException, JsonMappingException, IOException
    {
        ObjectMapper mapper = getJsonObjectMapper();
        mapper.writeValue(os, result);

    }

    public static NullNode nullValue()
    {
        return JsonNodeFactory.instance.nullNode();
    }

    public static TextNode toJsonString(String string)
    {
        return JsonNodeFactory.instance.textNode(string);
    }

    public static TextNode toJsonString(XValue xv)
    {
        if (xv == null || xv.isNull())
            return toJsonString((String) null);

        if (xv.isString())
            return toJsonString(xv.toString());

        if (xv.isAtomic())
            return toJsonString(xv.toString());

        ObjectMapper mapper = getJsonObjectMapper();
        return mapper.convertValue(xv.asObject(), TextNode.class);

    }

    public static Object asJavaNative(JsonNode node)
    {
        if (node.isValueNode()) {
            ValueNode value = (ValueNode) node;
            if (value.isNumber())
                return ((NumericNode) value).numberValue();
            if (value.isBoolean())
                return value.asBoolean();
            if (value.isTextual())
                return value.asText().toString();
            if (value.isNull())
                return null;
        }

        if (node.isArray()) {
            ArrayNode a = (ArrayNode) node;
            ArrayList<Object> al = new ArrayList<Object>(a.size());
            for (JsonNode an : a) {
                al.add(asJavaNative(an));
            }
            return al;
        }
        ObjectMapper mapper = getJsonObjectMapper();

        if (node.isObject()) {
            return mapper.convertValue(node, Map.class);
        }
        return node.toString(); // WTF
    }

    public static void writeJsonNode(JsonNode value, OutputStream os,
            SerializeOpts opt) throws JsonGenerationException,
            JsonMappingException, IOException
    {
        ObjectMapper mapper = getJsonObjectMapper(opt);
        mapper.writeValue(os, value);
    }

    public static InputStream asInputStream(JsonNode value, SerializeOpts opt)
            throws JsonGenerationException, JsonMappingException, IOException
    {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        writeJsonNode(value, bos, opt);
        bos.flush();
        bos.close();
        return new ByteArrayInputStream(bos.toByteArray());

    }

    public static class JsonNodeBuilder extends  TokenBuffer {
    	public JsonNodeBuilder() { 
    		super(getJsonObjectMapper() , false);
    	}
    	
    	public JsonNode build() throws JsonProcessingException, IOException { 
            try ( JsonParser jp = asParser() ) {
            	return getJsonObjectMapper().readTree(jp);
            } 
        } 
    };
    
    public static JsonNodeBuilder createJsonNodeBuilder( ){
    	return new JsonNodeBuilder();
    };
    
    
    public static JsonGenerator createGenerator(OutputStream os,
            SerializeOpts jopts) throws IOException
    {
        JsonGenerator gen = getJsonFactory().createGenerator(os);
        if (jopts.getIndentJson() )
           return gen.useDefaultPrettyPrinter();
        return gen;
    }

    public static void safeClose(JsonGenerator generator)
    {
        if (generator != null) {
            try {
                generator.close();
            } catch (IOException e) {
                mLogger.info("Exception closing JsonGenerator", e);
            }
        }
    }

    public static void safeClose(JsonParser parser)
    {
        if (parser != null) {
            try {
                parser.close();
            } catch (IOException e) {
                mLogger.info("Exception closing JsonParser", e);
            }
        }

    }

    public static byte[] toByteArray(JsonNode value, SerializeOpts opt)
            throws JsonGenerationException, JsonMappingException, IOException {
        return getJsonObjectMapper().writeValueAsBytes(value);
    }

    public static JavaType getJavaType(Object obj) {

        return getJsonObjectMapper().constructType(obj.getClass());
    }

    public static boolean isNullClass(Class<?> cls)
    {
        return cls == null || NullNode.class.isAssignableFrom(cls);
    }

    public static boolean isContainerClass(Class<?> cls)
    {
        return ContainerNode.class.isAssignableFrom(cls);
    }

    public static boolean isObjectClass(Class<?> cls)
    {
        return ObjectNode.class.isAssignableFrom(cls);
    }

    public static boolean isArrayClass(Class<?> cls)
    {
        return ArrayNode.class.isAssignableFrom(cls);
    }

    public static boolean isAtomicClass(Class<?> cls)
    {
        return ValueNode.class.isAssignableFrom(cls) &&
                !(MissingNode.class.isAssignableFrom(cls) ||
                POJONode.class.isAssignableFrom(cls));
    }

    public static boolean isClassClass(Class<?> cls)
    {
        return JsonNodeType.class.isAssignableFrom(cls);

    }

    public static boolean isEmpty(JsonNode value) {
        return value.size() == 0;
    }

    public static ObjectNode newJsonObject() {
        return getJsonObjectMapper().createObjectNode();
    }

    public static ArrayNode newJsonArray() {
        return getJsonObjectMapper().createArrayNode();
    }

    public static List<XValue> asXList(Iterator<JsonNode> nodes)
            throws InvalidArgumentException
    {
        XValueList list = new XValueList();

        while (nodes.hasNext())
            list.add(XValue.newXValue(TypeFamily.JSON, nodes.next()));

        return list;
    }

    public static boolean isAtomic(Object value)
    {

        return isAtomicClass(value.getClass());
    }

	public static void writeJsonNode(JsonGenerator gen, JsonNode result) throws JsonGenerationException, JsonMappingException, IOException {
		getObjectWriter().writeValue(gen,result);		
	}
    
    

}

/*
 * Copyright (C) 2008-2014 David A. Lee.
 * 
 * The contents of this file are subject to the "Simplified BSD License" (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.opensource.org/licenses/bsd-license.php
 * 
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
 */