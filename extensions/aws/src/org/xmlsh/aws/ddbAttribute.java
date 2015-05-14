package org.xmlsh.aws;

import java.util.List;

import org.xmlsh.aws.util.DDBTypes.AttrType;
import org.xmlsh.core.IFunction;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.core.Command;
import org.xmlsh.sh.shell.Shell;
import  org.xmlsh.core.BuiltinFunctionCommand;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;

public class ddbAttribute extends BuiltinFunctionCommand {

    public ddbAttribute() {
        super("ddb-attribute-value");
    }

    @Override
    public XValue run(Shell shell, List<XValue> args) throws Exception {
        switch( args.size() ){
        default :
            return new XValue(AttrType.parseType( args.get(0).toString() ));
        }

    }


}
