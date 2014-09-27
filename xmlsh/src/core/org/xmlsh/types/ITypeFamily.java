package org.xmlsh.types;

import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.XValue;



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
    public Object nullValue();
    public XValue nullXValue();




}
