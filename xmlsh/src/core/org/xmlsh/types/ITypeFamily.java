package org.xmlsh.types;

import java.io.UnsupportedEncodingException;
import java.util.EnumSet;

import net.sf.saxon.s9api.SaxonApiException;


/*
 * Interface all type families must provide - 
 * if not then the XTYPE type family will be used
 * 
 */
public interface ITypeFamily
{
    TypeFamily                 typeFamily();         // Returns the specific type family
    EnumSet<TypeFamily>        subTypeFamilies();    // Directly derived type families
    EnumSet<TypeFamily>        superTypeFamilies();   // All supertypes
    
    
    /* Type inspection */
    IType  getType( Object obj );
    
    public boolean isInstanceOfFamily(Object obj) ;
    public boolean isClassOfFamily(Class<?> cls);
    public String asString(Object value) throws Exception ;
    
    
}
