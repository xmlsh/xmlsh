package org.xmlsh.commands.internal;

import java.util.List;

import org.xmlsh.core.BuiltinFunctionCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.core.XValueArray;
import org.xmlsh.core.XValueList;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.types.TypeFamily;

public class list extends BuiltinFunctionCommand
{

    public list() {
        super("list");
    }

    @Override
    public XValue run(Shell shell, List<XValue> args) throws Exception {
        XValueList a = new XValueList() ;
        a.addAll( args );
       return new XValue(TypeFamily.XTYPE,a);
        
    }

}
