package org.xmlsh.types.xtypes;

import java.util.Collection;
import java.util.Iterator;

import org.xmlsh.core.XValue;

/*
 * Type that can contain other types.
 */

public interface  IXValueContainer extends IXValue, Iterable<XValue>
{
	public int size();
	public void removeAll();
	public Collection<XValue> values();   // Ordered set of values
	public Iterator<XValue> iterator();

}