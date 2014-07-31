package org.xmlsh.types;

import org.xmlsh.core.CoreException;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.util.JavaUtils;
import org.xmlsh.util.Util;

import java.io.IOException;
import java.io.OutputStream;
import java.util.EnumSet;

import com.fasterxml.jackson.databind.JsonNode;

public class JavaTypeFamily implements ITypeFamily
{
    static final JavaTypeFamily instance = new JavaTypeFamily();
	private XValue _nullValue = new XValue( TypeFamily.JAVA);

    private JavaTypeFamily() {}

    @Override
    public TypeFamily typeFamily() {
      return TypeFamily.JAVA;
    }

    @Override
    public EnumSet<TypeFamily> subTypeFamilies() {
        return EnumSet.allOf(TypeFamily.class);
    }

    @Override
    public EnumSet<TypeFamily> superTypeFamilies() {
        return EnumSet.noneOf(TypeFamily.class);
    }

    @Override
    public IType getType(Object obj) {
        return JavaType.getType(obj);
    }

  
    @Override
    public boolean isInstanceOfFamily(Object obj) {
        return true ;
    }

    @Override
    public boolean isClassOfFamily(Class<?> cls) {
        return true ;
    }

    @Override
    public String asString(Object value) {
       return value.toString();
    }

	@Override
    public int getSize(Object obj)
    {
      if( obj == null)
    	  return 0 ;
	  return JavaUtils.getSize( obj );
		
    }

	@Override
    public XValue getValue(XValue xvalue, String ind) throws CoreException 
    {
		 if( xvalue == null || xvalue.isNull() )
		    	return _nullValue;
	    if( Util.isBlank(ind) )
	    	return xvalue;
	    
	    Object obj = xvalue.asObject();
	    try {
	        return JavaUtils.getField( obj.getClass() , obj , ind , null );
        } catch (InvalidArgumentException | SecurityException | NoSuchFieldException | IllegalArgumentException
                | IllegalAccessException | ClassNotFoundException e) {
	       Util.wrapCoreException("Exception getting value from java class: " + obj.getClass().getName() , e );
        }
		return xvalue;
    }

	@Override
    public void serialize(Object value, OutputStream out, SerializeOpts opts) throws IOException
    {
		if( value == null )
			return ;
		out.write( JavaUtils.toBytes(value, opts) );
	    
    }	    

}
