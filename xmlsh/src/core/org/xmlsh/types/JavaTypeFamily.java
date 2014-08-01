package org.xmlsh.types;

import org.xmlsh.util.JavaUtils;

import java.util.EnumSet;

public class JavaTypeFamily extends AbstractTypeFamily implements ITypeFamily
{
	static final JavaTypeFamily _instance = new JavaTypeFamily();

	@Override
	protected IType getTypeInstance(XTypeKind kind)
	{
		return JavaType.newInstance(kind);
	}

	@Override
	protected
	XTypeKind inferKind( Class<?> cls ) {
		if( JavaUtils.isNullClass( cls ) ) 
			return XTypeKind.NULL ;
		if( JavaUtils.isContainerClass( cls ) )
			return XTypeKind.NULL ;
		if( JavaUtils.isAtomicClass( cls ) )
			return XTypeKind.ATOMIC ;
		if( JavaUtils.isClassClass( cls ) )
			return XTypeKind.CLASS ;
		else
			return XTypeKind.OBJECT ;
	}


	@Override
	public boolean isClassOfFamily(Class<?> cls) {
		return true ;
	}

	@Override
	public boolean isInstanceOfFamily(Object obj) {
		return true ;
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
	public TypeFamily typeFamily()
	{
		return TypeFamily.JAVA;
	}


}
