package org.xmlsh.util;

import java.io.IOException;

public interface IReleasable {

	public abstract void release() throws IOException;

}