package org.xmlsh.type.functions;

import org.xmlsh.core.AbstractBuiltinFunction;
import org.xmlsh.core.XValue;
import org.xmlsh.core.XValueArray;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.types.TypeFamily;

import java.util.List;

public class array extends AbstractBuiltinFunction
{

	public array() {
		super("array");
	}

	@Override
	public XValue run(Shell shell, List<XValue> args) throws Exception {
		XValueArray a = new XValueArray() ;
		a.addAll( args );
		return XValue.newXValue(TypeFamily.XTYPE,a);

	}

}