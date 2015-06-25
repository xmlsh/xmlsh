package org.xmlsh.aws;

import java.util.List;

import org.xmlsh.aws.util.DDBTypes;
import org.xmlsh.aws.util.DDBTypes.AttrNameExpr;
import org.xmlsh.core.AbstractBuiltinFunction;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.Shell;

public class ddbAttrNameExpr extends AbstractBuiltinFunction {

    public ddbAttrNameExpr() {
        super("ddb-attr-name-expr");
    }

    @Override
    public XValue run(Shell shell, List<XValue> args) throws Exception {
        switch( args.size() ){
        case 0 :
            throw new InvalidArgumentException("Required arguments: " + getName() + " expr ... ");
        case 1:
            return  XValue.newXValue( 
                    DDBTypes.addNamePrefix(DDBTypes.parseAttrNameExpr(args.get(0)) ));
        case 2:
            return  XValue.newXValue( 
                    DDBTypes.addNamePrefix(  new AttrNameExpr( args.get(0).toString() , args.get(1).toString() ) ));

        default :
            return  XValue.newXValue( DDBTypes.parseAttrNameExprs( args  ));

        }

    }

}
