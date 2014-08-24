package org.xmlsh.types;



/*
 * Interface all type families must provide - 
 * if not then the XTYPE type family will be used
 * 
 */
public interface ITypeFamily extends IMethods
{
	/* Type inspection */
	public boolean isClassOfFamily(Class<?> cls);
	public boolean isInstanceOfFamily(Object obj) ;
	TypeFamily                 typeFamily();         // Returns the specific type family

}
