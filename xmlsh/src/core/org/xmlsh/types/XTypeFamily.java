package org.xmlsh.types;

import java.util.EnumSet;

import org.xmlsh.core.XValue;
import org.xmlsh.core.XValueArray;
import org.xmlsh.core.XValueContainer;
import org.xmlsh.core.XValueList;

public class XTypeFamily implements ITypeFamily
{
    static final XTypeFamily instance = new XTypeFamily();

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


}
