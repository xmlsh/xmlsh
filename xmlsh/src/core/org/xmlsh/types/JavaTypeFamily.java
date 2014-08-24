package org.xmlsh.types;

import org.xmlsh.core.CoreException;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.util.JavaUtils;
import org.xmlsh.util.Util;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

public class JavaTypeFamily extends AbstractTypeFamily implements ITypeFamily
{
  static final JavaTypeFamily _instance = new JavaTypeFamily();

/*
 * @Override
 * protected
 * XTypeKind inferKind( Class<?> cls ) {
 * if( JavaUtils.isNullClass( cls ) )
 * return XTypeKind.NULL ;
 * if( JavaUtils.isObjectClass( cls ) )
 * return XTypeKind.MAP ;
 * if( JavaUtils.isArrayClass( cls ) )
 * return XTypeKind.ARRAY ;
 * if( JavaUtils.isContainerClass( cls ) )
 * return XTypeKind.CONTAINER ;
 * if( JavaUtils.isAtomicClass( cls ) )
 * return XTypeKind.ATOMIC ;
 * if( JavaUtils.isClassClass( cls ) )
 * return XTypeKind.CLASS ;
 * else
 * return XTypeKind.MAP ;
 * }
 */
  @Override
  public boolean isClassOfFamily(Class<?> cls)
  {
    return true;
  }

  @Override
  public boolean isInstanceOfFamily(Object obj)
  {
    return true;
  }

  @Override
  public TypeFamily typeFamily()
  {
    return TypeFamily.JAVA;
  }
    private final XValue _nullValue = new XValue(TypeFamily.JAVA, null);


    @Override
    public String asString(Object value)
    {
      if( value == null )
        return "";
      return value.toString();
    }

    @Override
    public int getSize(Object obj)
    {
      if(obj == null)
        return 0;
      return JavaUtils.getSize(obj);

    }

    @Override
    public XValue getXValue(Object obj, String ind) throws CoreException
    {
      if(obj == null)
        return _nullValue;
      assert (!Util.isBlank(ind));

      Object res = null;
      boolean isInt = Util.isInt(ind, false);
      if(isInt && JavaUtils.isArrayClass(obj.getClass()))
        res = JavaUtils.getIndexValue(obj, Util.parseInt(ind, 0));
      else try {
        res = JavaUtils.getField(obj.getClass(), obj, ind, null);
      } catch (SecurityException | NoSuchFieldException | IllegalArgumentException
        | IllegalAccessException | ClassNotFoundException e) {
        Util.wrapCoreException("Exception getting value from java class: " + obj.getClass().getName(), e);
      }
      return toXValue(res);

    }

    @Override
    public void serialize(Object value, OutputStream out, SerializeOpts opts) throws IOException
    {
      if(value == null)
        return;
      out.write(JavaUtils.toByteArray(value, opts));

    }

    @Override
    public boolean isEmpty(Object value)
    {
      return JavaUtils.isEmpty(value);

    }

    // Set a string indexed value
    @Override
    public XValue setXValue(XValue xobj, String ind, XValue value) throws CoreException
    {
      assert (xobj != null && !xobj.isNull());
      if(xobj == null || xobj.isNull())
        throw new CoreException("Cannot set indexed value to null object");
      assert (!Util.isBlank(ind));

      try {
        JavaUtils.setNameIndexedValue(xobj.asObject(), ind, value.asObject());
        return xobj;
      } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
        Util.wrapCoreException("Exception setting indexed value", e);
      }
      return xobj;
    }

    // Get all values of a collection or just this value
    @Override
    public List<XValue> getXValues(Object obj)
    {
      if(obj == null)
        return Collections.emptyList();
      return Collections.singletonList(newXValue(obj));
    }

    @Override
    public XValue getXValue(Object obj)
    {
       return toXValue(obj);
      
      
    }

    @Override
    public boolean isAtomic(Object value)
    {
      return JavaUtils.isAtomicClass(value.getClass());
    }

    @Override
    public XValue getXValue(Object obj, int index) throws CoreException
    {
      Object r = JavaUtils.getIndexValue(obj, index);
      return toXValue(r);
    }

     XValue toXValue(Object r)
    {
      if( r == null  )
        return _nullValue ;
      if( r instanceof XValue )
        return( (XValue) r );
      return newXValue( r );
    }

    @Override
    public XValue setXValue(XValue xobj, int index, XValue value) throws CoreException
    {
        assert (xobj != null && !xobj.isNull());
        if(xobj == null || xobj.isNull())
          throw new CoreException("Cannot set indexed value to null object");
        assert (index >= 0 );

        try {
          JavaUtils.setNamePositionalValue(xobj.asObject(), index, value.asObject());
          return xobj;
        } catch (SecurityException | IllegalArgumentException e) {
          Util.wrapCoreException("Exception setting indexed value", e);
        }
        return xobj;
    }

}
