package org.xmlsh.commands.internal;

import org.xmlsh.core.BuiltinFunctionCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.types.TypeFamily;
import org.xmlsh.types.XTypeUtils;

import java.util.List;

public class map extends BuiltinFunctionCommand
{

	public map() {
		super("map");
	}

	@Override
	public XValue run(Shell shell, List<XValue> args) throws Exception {
		return new XValue(TypeFamily.XTYPE ,  XTypeUtils.newMapFromList(args));
	}

}
