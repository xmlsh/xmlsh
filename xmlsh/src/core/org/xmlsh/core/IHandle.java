package org.xmlsh.core;


/*
 * Handle to an object - get returns the object or null
 * 
 */
public interface IHandle<T> extends IReleasable {
	public T get();
	public IHandle<T> newReference();
}
