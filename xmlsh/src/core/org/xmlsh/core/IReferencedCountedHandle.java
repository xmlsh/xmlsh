package org.xmlsh.core;

public interface IReferencedCountedHandle<T extends IHandleable >  extends IManagedHandle<T> {

	/* (non-Javadoc)
	 * @see org.xmlsh.core.IManagedHandle#addRef()
	 */
	public abstract void addRef();
	public abstract int getRefCount();

}