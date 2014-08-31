package org.xmlsh.types;

import org.xmlsh.core.CoreException;
import org.xmlsh.core.IXValue;
import org.xmlsh.core.IXValueContainer;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.XValue;
import org.xmlsh.core.XValueList;
import org.xmlsh.core.XValueSequence;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.util.JavaUtils;
import org.xmlsh.util.Util;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

public class XTypeFamily extends AbstractTypeFamily implements ITypeFamily
{
  static final XTypeFamily _instance = new XTypeFamily();

/*
 * @Override
 * protected XTypeKind inferKind( Class<?> cls ) {
 * if( cls == null )
 * return XTypeKind.NULL;
 * if( XValueArray.class.isAssignableFrom(cls) ||
 * XValueList.class.isAssignableFrom(cls) )
 * return XTypeKind.ARRAY ;
 * if( XValueMap.class.isAssignableFrom(cls) )
 * return XTypeKind.MAP ;
 * if( IXValueContainer.class.isAssignableFrom(cls) )
 * return XTypeKind.CONTAINER ;
 * return XTypeKind.ATOMIC;
 * 
 * }
 */
  @Override
  public boolean isClassOfFamily(Class<?> cls)
  {
    return XValue.class.isAssignableFrom(cls) || IXValueContainer.class.isAssignableFrom(cls);
  }

  @Override
  public boolean isInstanceOfFamily(Object obj)
  {
    return obj instanceof XValue || obj instanceof IXValueContainer;
  }

  @Override
  public TypeFamily typeFamily()
  {
    return TypeFamily.XTYPE;
  }


    @Override
    public XValue append(Object value, XValue v) throws InvalidArgumentException
    {
      return asXType(value).append(v);

    }

    @Override
    public String asString(Object value)
    {
      if( value == null )
        return "";
      return value.toString();
    }

    @Override
    public int getSize(Object obj) throws InvalidArgumentException
    {

      IXValue<?> ic = asXType(obj);
      assert( obj != null );
      if( obj == null )
        return 0;
     
      return ic.isContainer() ? ic.asXContainer().size() : 
         0;
    }

    // Named index access
    @Override
    public XValue getXValue(Object obj, String ind) throws InvalidArgumentException
    {
      if(obj == null)
        return _nullValue;
      IXValue<?> ic = asXType(obj);

      if(Util.isBlank(ind))
        return newXValue(obj);
      
      // map first
      if( ic.isMap() )
        return ic.asXMap().get(ind);      
      if( ic.isList() ) 
        return ic.asXList().get(Util.parseInt(ind, 1)-1);

       return _nullValue ;
    }

    @Override
    public XValue getXValue(Object obj, int index) throws CoreException
    {
      IXValue<?> ic = asXType(obj);
      if(ic.isList())
        return ic.asXList().getAt(index);
      throw new InvalidArgumentException("Not an indexable type: " + describeClass(obj));

    }

    @Override
    public void serialize(Object obj, OutputStream out, SerializeOpts opts) throws IOException,
        InvalidArgumentException
    {
      if(obj == null)
        return;
      IXValue<?> ic = asXType(obj);
      ic.serialize(out, opts);
    }

    @Override
    public String typeName(Object obj)
    {
      if( obj == null )
        return "null";
      
      return JavaUtils.getClassName(obj);

    }

    @Override
    public boolean isEmpty(Object obj) throws InvalidArgumentException
    {
      if(obj == null)
        return true;
      IXValue<?> ic = asXType(obj);
      return ic.isEmpty();

    }

    @Override
    public XValue setXValue(XValue xobj, String ind, XValue value) throws CoreException
    {

      assert (xobj != null && !xobj.isNull());
      if(xobj == null || xobj.isNull())
        throw new CoreException("Cannot set indexed value to null object");
      
      IXValue<?> ic = asXType(xobj.asObject());
      // map first
      if( ic.isMap() )
        return ic.asXMap().put(ind,value);      
      if( ic.isList() ) 
        return ic.asXList().setAt(Util.parseInt(ind, 0),value);

      throw new CoreException("Cannot set non-indexed object");

    }

    @Override
    public List<XValue> getXValues(Object obj)
    {
      if(obj == null)
        return Collections.emptyList();
      if((obj instanceof IXValueContainer)) {
        if(obj instanceof XValueList)
          return (XValueList) obj;
        XValueList list = new XValueList();
        list.addAll(((IXValueContainer<?>) obj).values());
        return list;
      }
      else if((obj instanceof XValue))
        return Collections.singletonList((XValue) obj);
      else return Collections.singletonList(new XValue(null, obj)); // may conatain any type - SNH

    }

    @Override
    public XValue getXValue(Object obj)
    {

      if(obj instanceof XValue)
        return (XValue) obj;
      return newXValue(obj);

    }

    @Override
    public boolean isAtomic(Object obj)
    {
      if((obj instanceof IXValueContainer)) {
        return ((IXValue<?>) obj).isAtomic();
      }
      throw new InvalidArgumentException("Unexpected type: " + describeClass(obj));
    }

    @Override
    public XValue setXValue(XValue xobj, int index, XValue value) throws CoreException
    {

      assert (xobj != null && !xobj.isNull());

      IXValue<?> ic = asXType(xobj.asObject());

      if(xobj == null || xobj.isNull())
        throw new CoreException("Cannot set indexed value to null object");
      
      if( ic.isList() ) 
        return ic.asXList().setAt(index,value);
      if( ic.isMap() )
        return ic.asXMap().put(String.valueOf(index),value);      
      throw new CoreException("Cannot set non indexed value");

    }

  protected static IXValue<?> asXType(Object obj) throws InvalidArgumentException
  {
    if((obj instanceof IXValue)) {
      return ((IXValue<?>) obj);
    }
    throw new InvalidArgumentException("Unexpected type: " + describeClass(obj));

  }

}
