package org.xmlsh.core;

import java.util.Collection;
import java.util.Iterator;


public interface  IXValueContainer<T extends IXValueContainer<T > > extends IXValue<T> , Iterable<XValue>
{
	public int size();
	public void removeAll();
	public Collection<XValue> values();   // Ordered set of values
	public Iterator<XValue> iterator();

}