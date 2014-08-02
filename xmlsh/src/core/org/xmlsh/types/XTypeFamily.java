package org.xmlsh.types;

import org.xmlsh.core.IXValueContainer;
import org.xmlsh.core.XValue;
import org.xmlsh.core.XValueArray;
import org.xmlsh.core.XValueList;
import org.xmlsh.core.XValueMap;

import java.util.EnumSet;

public class XTypeFamily extends AbstractTypeFamily implements ITypeFamily
{
	static final XTypeFamily _instance = new XTypeFamily();


	@Override
	protected IType getTypeInstance(XTypeKind kind)
	{
		return XType.newInstance(kind);
	}

	@Override
	protected XTypeKind inferKind( Class<?> cls ) {
		if( cls == null ) 
			return XTypeKind.NULL;
		if( XValueArray.class.isAssignableFrom(cls) ||
		    XValueList.class.isAssignableFrom(cls) )
			  return XTypeKind.ARRAY ;
		if( XValueMap.class.isAssignableFrom(cls) )
			return XTypeKind.MAP ;
		if(  IXValueContainer.class.isAssignableFrom(cls) )
			return XTypeKind.CONTAINER ;
		return XTypeKind.ATOMIC;

	}
	@Override
	public boolean isClassOfFamily(Class<?> cls) {
		return XValue.class.isAssignableFrom( cls ) ||
				IXValueContainer.class.isAssignableFrom( cls );
	}
	@Override
	public boolean isInstanceOfFamily(Object obj) {
		return obj instanceof XValue || 
				obj instanceof IXValueContainer ; 
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
	public TypeFamily typeFamily()
	{
		return TypeFamily.XTYPE;
	}
}
