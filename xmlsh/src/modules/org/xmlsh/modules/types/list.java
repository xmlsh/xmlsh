package org.xmlsh.modules.types;

import java.util.List;

import org.xmlsh.core.AbstractBuiltinFunction;
import org.xmlsh.core.XValue;
import org.xmlsh.core.XValueList;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.types.TypeFamily;

public class list extends AbstractBuiltinFunction
{

	public list() {
		super("list");
	}

	@Override
	public XValue run(Shell shell, List<XValue> args) throws Exception {
		XValueList a = new XValueList() ;
		a.addAll( args );
		return XValue.newXValue(TypeFamily.XTYPE,a);

	}

}
