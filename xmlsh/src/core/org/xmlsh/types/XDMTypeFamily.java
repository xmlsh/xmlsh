package org.xmlsh.types;

import net.sf.saxon.om.ValueRepresentation;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;

import java.util.EnumSet;

public final class XDMTypeFamily extends AbstractTypeFamily implements ITypeFamily
{
	static final XDMTypeFamily _instance = new XDMTypeFamily();


	@Override
	protected IType getTypeInstance(XTypeKind kind)
	{
		return XDMType.newInstance(kind);

	}

	@Override
	protected XTypeKind inferKind( Class<?> cls ) {
		if( cls == null ) 
			return XTypeKind.NULL;

		if( XdmValue.class.isAssignableFrom(cls) ) {
			if( XdmAtomicValue.class.isAssignableFrom(cls ) ) 
				return XTypeKind.ATOMIC ;
			if( XdmNode.class.isAssignableFrom(cls ) ) 
				return XTypeKind.CONTAINER;

		}
		return XTypeKind.UNKNOWN;
	}

	@Override
	public boolean isClassOfFamily(Class<?> cls) {
		return XdmValue.class.isAssignableFrom( cls ) ||
				ValueRepresentation.class.isAssignableFrom(cls ) ||
				QName.class.isAssignableFrom(cls);
	}

	@Override
	public boolean isInstanceOfFamily(Object obj) {
		return 
				obj instanceof XdmValue ||
				obj instanceof ValueRepresentation ||
				obj instanceof QName ;

	}

	@Override
	public EnumSet<TypeFamily> subTypeFamilies() {
		return EnumSet.noneOf(TypeFamily.class);
	}



	@Override
	public EnumSet<TypeFamily> superTypeFamilies() {
		return EnumSet.of(TypeFamily.JAVA);
	}


	@Override
	public TypeFamily typeFamily()
	{
		return TypeFamily.XDM;
	}


}
