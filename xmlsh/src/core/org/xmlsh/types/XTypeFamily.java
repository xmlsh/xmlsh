package org.xmlsh.types;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.types.xtypes.IXValue;
import org.xmlsh.types.xtypes.IXValueContainer;
import org.xmlsh.types.xtypes.IXValueList;
import org.xmlsh.types.xtypes.IXValueMap;
import org.xmlsh.types.xtypes.XValueList;
import org.xmlsh.util.JavaUtils;
import org.xmlsh.util.Util;

public class XTypeFamily extends AbstractTypeFamily implements ITypeFamily
{
  private static final XTypeFamily _instance = new XTypeFamily();
  private static final Object _nullValue = null;
  static Logger mLogger = LogManager.getLogger();

  @Override
  public boolean isClassOfFamily(Class<?> cls)
  {
    return XValue.class.isAssignableFrom(cls) || IXValue.class.isAssignableFrom(cls);
  }

  @Override
  public boolean isInstanceOfFamily(Object obj)
  {
    if( obj == null )
      return true ; // OK for null objects
    return obj instanceof XValue || obj instanceof IXValue;
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
        return nullXValue();
      IXValue<?> ic = asXType(obj);

      if(Util.isBlank(ind))
        return  XValue.newXValue( this , obj , false );
      
      // map first
      if( ic.isMap() )
        return ic.asXMap().get(ind);      
      if( ic.isList() ) 
        return ic.asXList().get(Util.parseInt(ind, 1)-1);

       return nullXValue() ;
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
    public boolean isEmpty(Object obj) 
    {
      if(obj == null)
        return true;
      IXValue<?> ic;
	try {
		ic = asXType(obj);
		  return ic.isEmpty();
	} catch (InvalidArgumentException e) {
		throw new IllegalArgumentException(e);
	}
    

    }

    @Override
    public XValue setXValue(XValue xobj, String ind, XValue value) throws CoreException
    {

      assert (xobj != null && !xobj.isNull());
      if(xobj == null || xobj.isNull())
        throw new CoreException("Cannot set indexed value to null object");
      
      IXValue<?> ic = asXType(xobj.asObject());
      Object o = null;
      // map first
      if( ic.isMap() ){
    	  IXValueMap<? extends IXValueMap<?>> map = ic.asXMap();
    	  map.put(ind,value);
    	  if( map != ic )
    		  xobj = map.asXValue();
      }
      else
      if( ic.isList() ) {
    	  IXValueList<? extends IXValueList<?>> list = ic.asXList();
    	  list.setAt(Util.parseInt(ind, 0),value);
    	  if( ic != list )
    		  xobj = list.asXValue();
    	  
      }
      else
      throw new CoreException("Cannot set non-indexed object");
      
      return xobj ;

    }

    @Override
    public List<XValue> getXValues(Object obj) throws InvalidArgumentException
    {
      if(obj == null)
        return Collections.emptyList();
      if((obj instanceof IXValueContainer)) {
        if(obj instanceof IXValueList)
          return ((IXValueList<?>) obj).asList();
        XValueList list = new XValueList();
        list.addAll(((IXValueContainer<?>) obj).values());
        return list;
      }
      else if((obj instanceof XValue))
        return Collections.singletonList((XValue) obj);
      else return Collections.singletonList(XValue.newXValue(null, obj)); // may conatain any type - SNH

    }

    @Override
    public XValue getXValue(Object obj) throws InvalidArgumentException
    {

      if(obj instanceof XValue)
        return (XValue) obj;
      if( isInstanceOfFamily( obj ) ) 
        return XValue.newXValue( this , obj , false );
      return XValue.newInstance(obj);

    }

    @Override
    public boolean isAtomic(Object obj) 
    {
      if((obj instanceof IXValue)) {
        return ((IXValue<?>) obj).isAtomic();
      }
      mLogger.warn("Unexpected type:{} " , describeClass(obj));
      return false;
    }

    @Override
    public XValue setXValue(XValue xobj, int index, XValue value) throws CoreException
    {

      assert (xobj != null && !xobj.isNull());

      IXValue<?> ic = asXType(xobj.asObject());

      if(xobj == null || xobj.isNull())
        throw new CoreException("Cannot set indexed value to null object");
      
      if( ic.isList() ) {
        IXValueList<? extends IXValueList<?>> list = ic.asXList();
		list.setAt(index,value);
		if( ic != list )
			xobj = list.asXValue();
      }
      else
      if( ic.isMap() ){
        IXValueMap<? extends IXValueMap<?>> map = ic.asXMap();
		map.put(String.valueOf(index),value);  
		if( map != ic )
			xobj = map.asXValue();
      }
      else
      
      throw new CoreException("Cannot set non indexed value");
      return xobj;

    }

  protected static IXValue<?> asXType(Object obj) throws InvalidArgumentException
  {
    if((obj instanceof IXValue)) {
      return ((IXValue<?>) obj);
    }
    throw new InvalidArgumentException("Unexpected type: " + describeClass(obj));

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
		return XValue.newXValue(this , _nullValue, false );
	} catch (InvalidArgumentException e) {
		throw new IllegalArgumentException(e);
	}
  }

  public static XTypeFamily getInstance()
  {
    return _instance;
  }


@Override
public boolean hasKey(Object obj, String key) {
	if( obj instanceof IXValueContainer ){
		IXValueMap<?> c = (IXValueMap<?>) obj ;
		return 	c.containsKey(key);
	}
	return false;
	
	
}

@Override
public boolean isContainer(Object obj) {

	return obj instanceof IXValueContainer ;
	
}


}
