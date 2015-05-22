package org.xmlsh.aws;

import java.util.ArrayList;
import java.util.List;

import org.xmlsh.aws.util.DDBTypes;
import org.xmlsh.core.BuiltinFunctionCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.Shell;

public class ddbValues extends BuiltinFunctionCommand {

    public ddbValues() {
        super("ddb-values");
    }

    @Override
    public XValue run(Shell shell, List<XValue> args) throws Exception {
        List<XValue> list = new ArrayList<XValue>(args.size());
        for( XValue arg : args ){
            list.add( new XValue( DDBTypes.parseAttrValue( arg )));
        }
        return new XValue(list) ;
    }


}
