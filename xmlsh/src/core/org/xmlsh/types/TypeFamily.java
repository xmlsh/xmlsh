package org.xmlsh.types;


/*
 * XTypeFamily defines built in identifiers for generic type "families"
 * A type family is both a hierarhy and substitution group for other type famalies.
 * All type familes are known at compile time (for now).  
 * Each type family identifies capbailities of that type family for being able to 
 * represent or convert types from its own family or other families.
 * 
 * 
 */
public enum TypeFamily {
    XTYPE(XTypeFamily.instance),                        // XML Xtypes (XValue, XList .. .
    XDM(XDMTypeFamily.instance),                        // XDM type system - currently only Saxon XDM types
    JSON(JSONTypeFamily.instance),                       // JSON specific types - currently only Jackson implenetion tpyes
    JAVA(JavaTypeFamily.instance)                        // Generic JAVA type fmaily - substitutable for all other types but less specific
    ;
    private ITypeFamily instance;
    private TypeFamily(ITypeFamily f) {
        instance=f;
    }
    public ITypeFamily  instance() {
        return instance ;
    }

}
