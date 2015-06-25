package org.xmlsh.aws;

import java.util.List;

import org.xmlsh.aws.util.DDBTypes;
import org.xmlsh.core.AbstractBuiltinFunction;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.Shell;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;

public class ddbValue extends AbstractBuiltinFunction {

    public ddbValue() {
        super("ddb-value");
    }

    @Override
    public XValue run(Shell shell, List<XValue> args) throws Exception {
        switch( args.size() ){
        case 0:
            return  XValue.newXValue( new AttributeValue() );
        case 1:
            return  XValue.newXValue( DDBTypes.parseAttrValue( args.get(0) )); 
        case 2:
            return  XValue.newXValue( DDBTypes.parseAttrValue(args.get(0),args.get(1)));
        default :
            throw new InvalidArgumentException("Unexpected arguments: Usage: " + getName() +  " [type [value]" );
        }

    }


}
