package org.xmlsh.core;

import java.io.Closeable;
import java.io.IOException;

public interface IManagedHandle<T> extends IHandle<T> {

	public abstract boolean release() throws IOException;
	public abstract void addRef();
// temp	
	public int getRefCount();


}