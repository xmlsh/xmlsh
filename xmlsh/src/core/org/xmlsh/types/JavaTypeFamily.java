package org.xmlsh.types;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.XValue;
import org.xmlsh.core.XValueList;
import org.xmlsh.core.XValueSequence;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.util.JavaUtils;
import org.xmlsh.util.Util;

public class JavaTypeFamily extends AbstractTypeFamily implements ITypeFamily
{
  private static final JavaTypeFamily _instance = new JavaTypeFamily();
  private static final Object _nullValue = null ;
private List<XValue> values;


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
    @SuppressWarnings("unchecked")
	@Override
    public List<XValue> getXValues(Object obj) throws InvalidArgumentException
    {
      if(obj == null)
        return Collections.emptyList();
      if( JavaUtils.isContainerOf( obj , XValue.class ) )  
    		return JavaUtils.<XValue>getValues( obj );

      if( JavaUtils.isCollectionOf(obj ,XValue.class) )
		 return JavaUtils.<XValue>getValues( obj );

      if( obj instanceof XValue )
    	  return ((XValue)obj).getXValues();
      
      if(JavaUtils.isContainer(obj)){
          List<?> list = JavaUtils.getValues(obj);

          List<XValue> xlist = new ArrayList<>(list.size());
         for( Object o : list ){
             xlist.add( XValue.newXValue(o));
         }
         return xlist ;
          
         
      }
      
      
      return Collections.singletonList(getXValue(obj));
    }

    @Override
    public XValue getXValue(Object obj) throws InvalidArgumentException
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
      try {
		return XValue.newXValue(this , _nullValue , false);
	} catch (InvalidArgumentException e) {
		throw new IllegalArgumentException(e);
	}
    }

    public static JavaTypeFamily getInstance()
    {
      return _instance;
    }


	@Override
	public boolean hasKey(Object obj, String key) {
	   return JavaUtils.hasKey( obj , key );
		
	}

	@Override
	public XValue append(Object value, XValue v)
			throws InvalidArgumentException {
	
	
		if( isContainer(value)){
			/*
			Class common = JavaUtils.getCommonAncestor( JavaUtils.getContainedType(value) , v.asObject() );
			Object newContainer = JavaUtils.newConatainerOf( value.getClass() , common );
			JavaUtils.addAll(  newContainer , value );
			JavaUtils.add( newContainer , v.asObject() );
			*/
	
			List<Object> ret = JavaUtils.getValues(value );
			ret.add((Object) v.asObject());
			return XValue.newXValue( TypeFamily.JAVA , ret );
			
		}
		
		return XValue.newXValue( TypeFamily.JAVA , Arrays.asList( value  , v.asObject()) );
		
		
	}

	@Override
	public boolean isContainer(Object obj) {
		assert( obj != null );
		return JavaUtils.isContainerClass(obj.getClass());
		
		
		
	}


}
