package org.xmlsh.types;



/*
 * Root of all describable types 
 */
public interface IType
{
	public TypeFamily family();
	public IMethods   getMethods();
	public boolean   isAtomic();
	public boolean   isContainer();
	public boolean   isNull();
	public XTypeKind  kind();         // General kind of type 

}
