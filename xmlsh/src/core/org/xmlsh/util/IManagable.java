package org.xmlsh.util;

import java.io.IOException;

import org.xmlsh.core.IHandleable;

public interface IManagable extends IHandleable {
	void doClose() throws IOException;
}
