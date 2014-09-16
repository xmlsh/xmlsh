/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.core;

import java.util.EnumSet;

import org.xmlsh.util.Util;

public abstract class XDynamicVariable extends XVariable {

	public XDynamicVariable(String name,EnumSet<XVarFlag> flags) {
		super(name,flags);

	}

	/* (non-Javadoc)
	 * @see org.xmlsh.core.XVariable#getValue()
	 */
	@Override
	public abstract XValue getValue();
	
	@Override
	public XVariable clone() {
		throw new InvalidArgumentException("Cannot clone: " + getName() );

	}

	/* (non-Javadoc)
	 * @see org.xmlsh.core.XVariable#setValue(org.xmlsh.core.XValue)
	 */
	@Override
	public void setValue(XValue value) throws InvalidArgumentException {
		throw new InvalidArgumentException("Cannot set value of variable: " + getName() );
	}

}
