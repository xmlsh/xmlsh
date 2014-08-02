package org.xmlsh.types;

/*
 * Generalized type kinds  for all describeable XTypes
 */
public enum XTypeKind {
	UNKNOWN,            // not known
	NULL,               // not known because its null or a spefic null class
	CLASS,              // Description type .. (class, schema, annotation )
	ATOMIC,             // atomic (list/bool ectc) or *appears* atomic
	MAP,                 // Name/Value collection
	ARRAY,              // Indexable List (array)
	CONTAINER,           // Container - unordered list
    OBJECT              // POJO or other generic object
	;

	@Override
	public String toString() {
		return name().toLowerCase();
	}
}
