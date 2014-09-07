package org.xmlsh.type.functions;

import org.xmlsh.core.AbstractBuiltinFunction;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.types.TypeFamily;
import org.xmlsh.types.XTypeUtils;

import java.util.List;

public class map extends AbstractBuiltinFunction
{

	public map() {
		super("map");
	}

	@Override
	public XValue run(Shell shell, List<XValue> args) throws Exception {
		return XValue.newXValue(TypeFamily.XTYPE ,  XTypeUtils.newMapFromList(args));
	}

}
