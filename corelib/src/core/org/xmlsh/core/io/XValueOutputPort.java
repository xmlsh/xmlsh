package org.xmlsh.core.io;

import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.XValue;
import org.xmlsh.core.XVariable;

public class XValueOutputPort extends VariableOutputPort {

    public XValueOutputPort(XValue value) throws InvalidArgumentException {
        super(XVariable.anonymousInstance(value));
    }

}
