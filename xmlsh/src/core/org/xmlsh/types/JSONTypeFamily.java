package org.xmlsh.types;

import org.xmlsh.json.JSONUtils;

import java.util.EnumSet;

import com.fasterxml.jackson.databind.JsonNode;

public class JSONTypeFamily extends AbstractTypeFamily implements ITypeFamily
{
	static final JSONTypeFamily _instance = new JSONTypeFamily();

	@Override
	protected IType getTypeInstance(XTypeKind kind)
	{
		return JSONType.newInstance(kind);
	}


	@Override
	protected XTypeKind inferKind( Class<?> cls ) {
		if( cls == null ) 
			return XTypeKind.NULL;
		if( JSONUtils.isNullClass( cls ) ) 
			return XTypeKind.NULL ;
		if( JSONUtils.isContainerClass( cls ) )
			return XTypeKind.NULL ;
		if( JSONUtils.isAtomicClass( cls ) )
			return XTypeKind.ATOMIC ;
		if( JSONUtils.isClassClass( cls ) )
			return XTypeKind.CLASS ;
		else
			return XTypeKind.UNKNOWN;

	}

	@Override
	public boolean isClassOfFamily(Class<?> cls) {
		return  JsonNode.class.isAssignableFrom(cls);
	}

	@Override
	public boolean isInstanceOfFamily(Object obj) {
		return obj instanceof JsonNode ;
	}


	@Override
	public EnumSet<TypeFamily> subTypeFamilies() {
		return EnumSet.noneOf(TypeFamily.class);
	}


	@Override
	public EnumSet<TypeFamily> superTypeFamilies() {
		return EnumSet.of( TypeFamily.JAVA);
	}


	@Override
	public TypeFamily typeFamily()
	{
		return TypeFamily.JSON;
	}
}
