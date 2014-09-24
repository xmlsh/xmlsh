package org.xmlsh.core;


public interface IReferenceCounted  extends IReleasable {

	public abstract void addRef();
	public abstract int getRefCount();

}