package org.xmlsh.core;

/*
 * Handle to an object - get returns the object or null
 * 
 */
public interface IHandle<V> extends IReleasable {
  public V get();

  boolean isNull();

}
