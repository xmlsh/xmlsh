package org.xmlsh.modules.types.array;

import java.util.List;

import org.xmlsh.annotations.Function;
import org.xmlsh.core.AbstractBuiltinFunction;
import org.xmlsh.core.XValue;
import org.xmlsh.core.XValueArray;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.types.TypeFamily;

@Function( name="new" )
public class array extends AbstractBuiltinFunction
{

	public array() {
		super("new");
	}

	@Override
	public XValue run(Shell shell, List<XValue> args) throws Exception {
		XValueArray a = new XValueArray() ;
		a.addAll( args );
		return XValue.newXValue(TypeFamily.XTYPE,a);

	}

}
