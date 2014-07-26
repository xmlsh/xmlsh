package org.xmlsh.types;

/*
 * Generalized type kinds  for all describeable XTypes
 */
public enum XTypeKind {
    UNKNOWN,            // not known
    NULL,               // not known because its null 
    CLASS,              // Description type .. (class, schema, annotation )
    ATOMIC,             // atomic (list/bool ectc) or *appears* atomic
    CONTAINER           // Container (Array,List,Map)
}
