package org.xmlsh.types;

/*
 * Generalized type kinds  for all describeable XTypes
 */
public enum XTypeKind {
	UNKNOWN,            // not known
	NULL,               // not known because its null or a spefic null class
	CLASS,              // Description type .. (class, schema, annotation )
	ATOMIC,             // atomic (list/bool ectc) or *appears* atomic
	OBJECT,             // POJO, Bean or other object that may have fields/methods
	CONTAINER           // Container (Array,List,Map)
	;

	@Override
	public String toString() {
		return name().toLowerCase();
	}
}
