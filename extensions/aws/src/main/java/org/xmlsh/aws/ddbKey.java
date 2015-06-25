package org.xmlsh.aws;

import java.util.List;

import org.xmlsh.aws.util.DDBTypes;
import org.xmlsh.core.AbstractBuiltinFunction;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.Shell;

public class ddbKey extends AbstractBuiltinFunction {

    public ddbKey() {
        super("ddb-key");
    }

    @Override
    public XValue run(Shell shell, List<XValue> args) throws Exception {
        switch( args.size() ){
        case 1:
            return  XValue.newXValue( DDBTypes.parseKey(args.get(0) ));
        case 2:
            return  XValue.newXValue( DDBTypes.parseKey( args.get(0) , args.get(1) ));
        default :
            throw new InvalidArgumentException("Unexpected arguments: Usage: " + getName() +  "name=value | name [value]" );

        }

    }


}
