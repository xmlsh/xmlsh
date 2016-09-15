package org.xmlsh.core.io;

import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.XValue;
import org.xmlsh.core.XVariable;

public class XValueInputPort extends VariableInputPort {

    public XValueInputPort(XValue value) throws InvalidArgumentException {
        super(XVariable.anonymousInstance(value));
    }

}
