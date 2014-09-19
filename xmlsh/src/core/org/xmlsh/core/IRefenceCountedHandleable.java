package org.xmlsh.core;

public interface IRefenceCountedHandleable<T extends IHandleable> {

	/* (non-Javadoc)
	 * @see org.xmlsh.core.IManagedHandle#addRef()
	 */
	public abstract void addRef();

	public abstract int getRefCount();

}