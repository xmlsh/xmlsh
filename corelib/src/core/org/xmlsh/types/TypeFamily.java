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
	XTYPE,                        // XML Xtypes (XValue, XList .. .
	XDM,                      // XDM type system - currently only Saxon XDM types
	JSON,                  // JSON specific types - currently only Jackson implenetion tpyes
	JAVA                 // Generic JAVA type fmaily - substitutable for all other types but less specific
	;

	@Override 
	public String toString() {
		return name().toLowerCase();
	}
}
