package org.xmlsh.types;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

import org.xmlsh.core.CoreException;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.util.JavaUtils;
import org.xmlsh.util.Util;

public class JavaTypeFamily extends AbstractTypeFamily implements ITypeFamily
{
  private static final JavaTypeFamily _instance = new JavaTypeFamily();
  private static final Object _nullValue = null ;

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
        return nullXValue();
      assert (!Util.isBlank(ind));

      Object res = null;
      boolean isInt = Util.isInt(ind, false);
      if(isInt && JavaUtils.isArrayClass(obj.getClass()))
        res = JavaUtils.getIndexValue(obj, Util.parseInt(ind, 0));
      else 
      if( ! isAtomic(obj) )
    	  res = JavaUtils.getNameIndexValue( obj , ind );
   
      return getXValue(res);

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
      return Collections.singletonList(getXValue(obj));
    }

    @Override
    public XValue getXValue(Object obj)
    {
       if( obj == null  )
         return nullXValue() ;
       if( obj instanceof XValue )
         return( (XValue) obj );
       
       
       return XValue.newXValue( this, obj , false );
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
      return getXValue(r);
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

    @Override
    public Object nullValue()
    {
      return _nullValue;
    }

    @Override
    public XValue nullXValue()
    {
      return XValue.newXValue(this , _nullValue , false);
    }

    public static JavaTypeFamily getInstance()
    {
      return _instance;
    }


	@Override
	public boolean hasKey(Object obj, String key) {
	   return JavaUtils.hasKey( obj , key );
		
	}


}
