package org.xmlsh.types;

import java.util.EnumSet;


/*
 * Interface all type families must provide - 
 * if not then the XTYPE type family will be used
 * 
 */
public interface ITypeFamily
{
	IMethods getMethods( Class<?> cls );
	IMethods getMethods( XTypeKind kind  );
	IType  getNullType();


	/* Type inspection */
	IType  getType( Class<?> cls );
	IType  getType( XTypeKind kind );
	public boolean isClassOfFamily(Class<?> cls);
	public boolean isInstanceOfFamily(Object obj) ;
	EnumSet<TypeFamily>        subTypeFamilies();    // Directly derived type families


	EnumSet<TypeFamily>        superTypeFamilies();   // All supertypes
	TypeFamily                 typeFamily();         // Returns the specific type family


}
