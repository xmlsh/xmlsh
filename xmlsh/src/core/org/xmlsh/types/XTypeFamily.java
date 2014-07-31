package org.xmlsh.types;

import java.io.IOException;
import java.io.OutputStream;
import java.util.EnumSet;

import com.fasterxml.jackson.databind.JsonNode;

import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.XValue;
import org.xmlsh.core.XValueArray;
import org.xmlsh.core.XValueContainer;
import org.xmlsh.core.XValueList;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.util.JavaUtils;
import org.xmlsh.util.Util;

public class XTypeFamily implements ITypeFamily
{
    static final XTypeFamily instance = new XTypeFamily();
	private static XValue _nullValue = new XValue( TypeFamily.XTYPE );

    private XTypeFamily() {}
    @Override
    public TypeFamily typeFamily() {
        return TypeFamily.XTYPE;
    }

    @Override
    public EnumSet<TypeFamily> subTypeFamilies() {
        return EnumSet.noneOf(TypeFamily.class);
    }

    @Override
    public EnumSet<TypeFamily> superTypeFamilies() {
        return EnumSet.of( TypeFamily.JAVA );
    }
    @Override
    public IType getType(Object obj) {
        return XType.getType( obj );
    }
    @Override
    public boolean isInstanceOfFamily(Object obj) {
        return obj instanceof XValue || 
               obj instanceof XValueContainer ; 
    }
    @Override
    public boolean isClassOfFamily(Class<?> cls) {
        return XValue.class.isAssignableFrom( cls ) ||
               XValueContainer.class.isAssignableFrom( cls );
    }
    @Override
    public String asString(Object value) {
        return value.toString();
    }
	@Override
    public int getSize(Object obj)
    {

		if( ( obj instanceof XValueContainer ) )
			return ((XValueContainer<?>)obj).size();

		return 0;
			

    }
	@Override
    public XValue getValue(XValue xvalue, String ind)
    {
		if( xvalue  == null || xvalue.isNull() )
			return _nullValue ;
		
		if( Util.isBlank(ind) )
			return xvalue ;
			
		Object obj = xvalue.asObject();

		if( ( obj instanceof XValueContainer ) )
			return ((XValueContainer<?>)obj).get(ind);
		else
		if( ( obj instanceof XValue ) ) 
			return ((XValue) obj );
		
		else
			return xvalue ;
			
		
		
    }
	@Override
    public void serialize(Object value, OutputStream out, SerializeOpts opts) throws IOException
    {
		if( value == null )
			return ;

		if( ( value instanceof XValueContainer ) )
			((XValueContainer<?>)value).serialize( out , opts );
		else
		if( ( value instanceof XValue ) )
	        try {
	            ((XValue)value).serialize(out, opts);
            } catch (InvalidArgumentException e) {
	            Util.wrapIOException(e);
            }
        else
			out.write(JavaUtils.toBytes(value, opts));
	    
    }


}
