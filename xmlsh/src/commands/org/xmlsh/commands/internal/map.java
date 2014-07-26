package org.xmlsh.commands.internal;

import java.util.List;

import org.xmlsh.core.BuiltinFunctionCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.core.XValueMap;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.types.TypeFamily;

public class map extends BuiltinFunctionCommand
{

    public map() {
        super("map");
    }

    @Override
    public XValue run(Shell shell, List<XValue> args) throws Exception {
        XValueMap map = new XValueMap();
        while( args.size() > 0) {
            XValue nv = args.remove(0);
            XValue v = args.isEmpty() ? new XValue() : args.remove(0);
            map.set(nv.toString(), v);
        }
        return new XValue(TypeFamily.XTYPE , map);
    }

}
