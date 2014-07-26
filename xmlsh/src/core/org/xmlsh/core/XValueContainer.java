package org.xmlsh.core;

import java.util.AbstractCollection;


public interface  XValueContainer<T extends XValueContainer<T > > 
{
    public abstract int size();
    public abstract boolean isEmpty();
    public abstract XValue get(int index);
    public abstract XValue get(String name);
    public abstract T removeAll();
}