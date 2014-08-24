package org.xmlsh.types;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import org.xmlsh.core.CoreException;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.XValue;
import org.xmlsh.json.JSONUtils;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.util.JavaUtils;
import org.xmlsh.util.Util;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.POJONode;

public class JSONTypeFamily extends AbstractTypeFamily implements ITypeFamily
{
  static final JSONTypeFamily _instance = new JSONTypeFamily();
  private static Logger mLogger = LogManager.getLogger(JSONTypeFamily.class);

  private final static XValue _nullValue = new XValue(TypeFamily.JSON , null);

/*
 * @Override
 * protected XTypeKind inferKind( Class<?> cls ) {
 * if( cls == null )
 * return XTypeKind.NULL;
 * if( JSONUtils.isNullClass( cls ) )
 * return XTypeKind.NULL ;
 * if( JSONUtils.isObjectClass( cls ) )
 * return XTypeKind.MAP ;
 * if( JSONUtils.isArrayClass( cls ) )
 * return XTypeKind.ARRAY ;
 * if( JSONUtils.isContainerClass( cls ) )
 * return XTypeKind.CONTAINER ;
 * if( JSONUtils.isAtomicClass( cls ) )
 * return XTypeKind.ATOMIC ;
 * if( JSONUtils.isClassClass( cls ) )
 * return XTypeKind.CLASS ;
 * else
 * return XTypeKind.UNKNOWN;
 * 
 * }
 */

  @Override
  public boolean isClassOfFamily(Class<?> cls)
  {
    return JsonNode.class.isAssignableFrom(cls);
  }

  @Override
  public boolean isInstanceOfFamily(Object obj)
  {
    return obj instanceof JsonNode;
  }

  @Override
  public TypeFamily typeFamily()
  {
    return TypeFamily.JSON;
  }

    private final class JsonToXValueConverter implements ITypeConverter<JsonNode, XValue>
    {
      @Override
      public XValue convert(JsonNode node)
      {
        return newXValue(node);
      }
    }

    @Override
    public String asString(Object value)
    {
      try {
        return JSONUtils.jsonToString((JsonNode) value);
      } catch (JsonProcessingException e1) {
        mLogger.warn("Exception serializing Json value", e1);
      }
      return "";
    }

    @Override
    public int getSize(Object obj)
    {
      assert (obj instanceof JsonNode);
      if( obj == null )
        return 0;
      return ((JsonNode) obj).size();
    }

    @Override
    public XValue getXValue(Object obj, String ind) throws CoreException
    {
      assert (!Util.isBlank(ind));

      if(obj == null)
        return _nullValue;
      assert (obj instanceof JsonNode);

      JsonNode node = (JsonNode) obj;
      switch (node.getNodeType()) {
      case ARRAY:
        return newXValue(node.get(Util.parseInt(ind, 0)));
      case OBJECT:
        return newXValue(node.get(ind));
      case POJO:
        return new XValue(null, node.get(ind));  // not JSON type
      default:
        return newXValue(obj);
      }

    }

    @Override
    public void serialize(Object value, OutputStream out, SerializeOpts opts) throws IOException
    {
      if(value == null)
        return;

      assert (value instanceof JsonNode);
      JsonNode node = (JsonNode) value;

      try {
        JSONUtils.writeJsonNode(node, out, opts);
      } catch (JsonGenerationException | JsonMappingException e) {
        Util.wrapIOException(e);
      }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmlsh.types.AbstractMethods#simpleTypeName(java.lang.Object)
     */
    @Override
    public String simpleTypeName(Object obj)
    {
      if( obj == null )
        return "null";

      if(obj instanceof JsonNode) {
        JsonNodeType nt = ((JsonNode) obj).getNodeType();
        return nt.toString().toLowerCase();
      }
      return JSONUtils.getJavaType(obj).getClass().getSimpleName();
    }

    @Override
    public boolean isEmpty(Object value)
    {
      assert (value != null);
      if(value instanceof JsonNode)
        return JSONUtils.isEmpty((JsonNode) value);

      return false;
    }

    // Set a named index
    @Override
    public XValue setXValue(XValue xobj, String ind, XValue value) throws CoreException
    {
      assert (xobj != null && !xobj.isNull());
      if(xobj == null || xobj.isNull())
        throw new CoreException("Cannot set indexed value to null object");
      assert (!Util.isBlank(ind));
      Object obj = xobj.asObject();
      assert (obj instanceof JsonNode);
      JsonNode node = (JsonNode) obj;
      switch (node.getNodeType()) {
      case ARRAY:
        ((ArrayNode) node).set(Util.parseInt(ind, 0), JSONUtils.toJsonType(value));
        break;
      case OBJECT:
        ((ObjectNode) node).set(ind, JSONUtils.toJsonType(value));
        break;
      case POJO:
        try {
          JavaUtils.setNameIndexedValue(((POJONode) node).getPojo(), ind, JSONUtils.toJsonType(value));
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
          Util.wrapCoreException("Exception setting indexed value", e);
        }
        break;
      default:
        assert (false);
      }
      return xobj;
    }

    @Override
    public List<XValue> getXValues(Object obj)
    {
      if(obj == null)
        return Collections.emptyList();
      assert (obj instanceof JsonNode);
      JsonNode node = (JsonNode) obj;
      switch (node.getNodeType()) {
      case ARRAY:
      case OBJECT:
        return JSONUtils.asXList(node.elements());
      case POJO:
        return Collections.singletonList(new XValue(TypeFamily.JAVA, ((POJONode) node).getPojo()));
      default:
        return Collections.singletonList(newXValue(obj));
      }

    }

    @Override
    public XValue getXValue(Object obj, int index) throws CoreException
    {
      assert( obj != null );
      assert (obj instanceof JsonNode);
      JsonNode node = (JsonNode) obj;
      Object ret = null ;
      switch (node.getNodeType()) {
      case ARRAY:
        ret = ((ArrayNode) node).get(index);
        break;
      case POJO:
        try {
          ret = JavaUtils.getIndexValue(  ((POJONode) node).getPojo(), index);
        } catch (SecurityException | IllegalArgumentException e) {
          Util.wrapCoreException("Exception setting indexed value", e);
        }
        break;
      default:
        assert (false);
      }
      return toXValue(ret);
    }
    

    XValue toXValue(Object r)
    {
      if( r == null  )
        return _nullValue ;
      if( r instanceof XValue )
        return( (XValue) r );
      return newXValue( r );
    }
    

    // Set a positional index value
    @Override
    public XValue setXValue(XValue xobj, int index, XValue value) throws CoreException
    {
      assert (xobj != null && !xobj.isNull());
      if(xobj == null || xobj.isNull())
        throw new CoreException("Cannot set indexed value to null object");
      assert (index >= 0);

      JsonNode node = getJsonNode(xobj);

      switch (node.getNodeType()) {
      case OBJECT:
        ((ArrayNode) node).set(index, JSONUtils.toJsonType(value));
        break;
      case POJO:
        JavaUtils.setNamePositionalValue(((POJONode) node).getPojo(), index, JSONUtils.toJsonType(value));
        break;
      default:
        throw new InvalidArgumentException("Cannot set indexed value to non Array class: " + describeClass(node));
      }
      return xobj;
    }

    @Override
    public XValue getXValue(Object obj) throws InvalidArgumentException
    {
      return toXValue(getJsonNode(obj));
    }



    @Override
    public boolean isAtomic(Object value)
    {
      return JSONUtils.isAtomic(value);
    }

  private static JsonNode getJsonNode(XValue xobj) throws InvalidArgumentException
  {
    if(xobj == null)
      return JSONUtils.jsonNull();
    Object obj = xobj.asObject();
    return getJsonNode(obj);
  }

  private static JsonNode getJsonNode(Object obj) throws InvalidArgumentException
  {
    if(obj == null)
      return JSONUtils.jsonNull();

    assert (obj instanceof JsonNode);
    if(!(obj instanceof JsonNode))
      throw new InvalidArgumentException("Not a Json Node type: " + describeClass(obj));

    return (JsonNode) obj;
  }

  public static XValue nullValue()
  {
   return _nullValue;
  }

}
