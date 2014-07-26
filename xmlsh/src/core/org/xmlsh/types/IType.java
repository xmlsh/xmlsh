package org.xmlsh.types;

import org.xmlsh.core.XValue;


/*
 * Root of all describable types 
 */
public interface IType
{
    TypeFamily family();
    XTypeKind  kind();         // General kind of type 
    String     simpleName();   // simple type name
    String     typeName();     // specific type name 
    XValue getIndexedValue(Object obj , String ind );

}
