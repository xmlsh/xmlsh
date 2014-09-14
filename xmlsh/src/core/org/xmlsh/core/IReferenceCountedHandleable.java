package org.xmlsh.core;

import org.xmlsh.util.ReferenceCounter;

public interface IReferenceCountedHandleable extends IHandleable  {
	
	ReferenceCounter getCounter();

}
