package org.xmlsh.core;

import java.io.IOException;

public interface IManagedHandle<T extends IHandleable> extends IHandle<T> {

  @Override
  public abstract boolean release() throws IOException;

}
