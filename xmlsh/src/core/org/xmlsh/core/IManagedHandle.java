package org.xmlsh.core;

import java.io.Closeable;
import java.io.IOException;

public interface IManagedHandle<T extends IHandleable> extends IHandle<T> {

	public abstract boolean release() throws IOException;


}