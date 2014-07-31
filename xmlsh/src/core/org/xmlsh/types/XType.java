package org.xmlsh.types;

import org.xmlsh.core.XValue;
import org.xmlsh.core.XValueContainer;
import org.xmlsh.util.Util;

public class XType extends TypeBase implements IType
{
    private XType(Class<?> cls ) {
       super(cls);
    }

    @Override
    public TypeFamily family() {
       return TypeFamily.XTYPE;
    }

    @Override
    public XTypeKind kind() {
    	if( mClass == null ) 
    		return XTypeKind.NULL;
        if(  XValueContainer.class.isAssignableFrom(mClass) )
            return XTypeKind.CONTAINER ;
        return XTypeKind.ATOMIC;
    }

    public static IType getType(Object obj) {
        return new XType( obj == null ? null : obj.getClass() );
    }

    @Override
    public XValue getIndexedValue(Object obj, String ind) {
        if( ind == null )
            return null ;
        
        if( obj instanceof XValue )
            return (XValue) obj ;
        
		if( Util.isBlank(ind) || ind.equals("*") )

			
        if( obj instanceof XValueContainer ) {
            return ((XValueContainer)obj).get(ind);
        }

        return null ;
        
    }

}
