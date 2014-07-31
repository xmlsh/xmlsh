package org.xmlsh.core;

import org.xmlsh.sh.shell.SerializeOpts;

import java.io.IOException;
import java.io.OutputStream;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;


public interface  XValueContainer<T extends XValueContainer<T > >  
{
    public abstract int size();
    public abstract boolean isEmpty();
    public abstract void add(XValue value);
    public abstract XValue put(String key , XValue value);
    public abstract XValue get(String name);
    public abstract void removeAll();
    public abstract Iterator<String>  keyIterator();     // Ordered set of keys
    public abstract Iterator<XValue> valueIterator();   // Ordered set of values
	public abstract void serialize(OutputStream out, SerializeOpts opts) throws IOException;
}