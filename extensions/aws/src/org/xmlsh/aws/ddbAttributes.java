package org.xmlsh.aws;

import java.util.List;

import org.xmlsh.aws.util.DDBTypes;
import org.xmlsh.aws.util.DDBTypes.INameAttrValueMap;
import org.xmlsh.aws.util.DDBTypes.NameAttrValueMap;
import org.xmlsh.core.BuiltinFunctionCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.Shell;

public class ddbAttributes extends BuiltinFunctionCommand {

    public ddbAttributes() {
        super("ddb-attribute");
    }

    @Override
    public XValue run(Shell shell, List<XValue> args) throws Exception {
        INameAttrValueMap attrs = new NameAttrValueMap();
        for (XValue arg : args) {
            attrs.putAll(DDBTypes.parseAttrNameValue(arg));
        }
        return new XValue( attrs );
    }

}
