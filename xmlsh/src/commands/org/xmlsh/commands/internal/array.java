package org.xmlsh.commands.internal;

import java.util.List;

import org.xmlsh.core.BuiltinFunctionCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.core.XValueArray;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.types.TypeFamily;
import org.xmlsh.types.XTypeFamily;

public class array extends BuiltinFunctionCommand
{

    public array() {
        super("array");
    }

    @Override
    public XValue run(Shell shell, List<XValue> args) throws Exception {
       XValueArray a = new XValueArray() ;
       a.addAll( args );
       return new XValue(TypeFamily.XTYPE,a);
        
    }

}
