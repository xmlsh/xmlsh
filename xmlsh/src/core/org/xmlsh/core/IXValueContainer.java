package org.xmlsh.core;

import org.xmlsh.sh.shell.SerializeOpts;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Set;


public interface  IXValueContainer<T extends IXValueContainer<T > >
{
	public abstract int size();
	public abstract boolean isEmpty();
	public abstract boolean isMap();
	public abstract boolean isList();
	public abstract boolean isAtomic();
	public abstract XValue put(String key , XValue value) ;
	public abstract XValue get(String name);
	public abstract void removeAll();
	public abstract Set<String>  keySet();     // Ordered set of keys
	public abstract Collection<XValue> values();   // Ordered set of values
	public abstract void serialize(OutputStream out, SerializeOpts opts) throws IOException;
	public abstract boolean add( XValue arg );
}