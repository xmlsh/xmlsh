package org.xmlsh.aws;

import java.util.List;

import org.xmlsh.aws.util.DDBTypes;
import org.xmlsh.aws.util.DDBTypes.AttrNameExpr;
import org.xmlsh.aws.util.DDBTypes.IAttrNameExpr;
import org.xmlsh.core.BuiltinFunctionCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.Shell;

public class ddbAttrNameExprs extends BuiltinFunctionCommand {

    public ddbAttrNameExprs() {
        super("ddb-attr-name-exprs");
    }

    @Override
    public XValue run(Shell shell, List<XValue> args) throws Exception {
        IAttrNameExpr attrs = new AttrNameExpr();
        for (XValue arg : args) {
            attrs.putAll(DDBTypes.parseAttrNameExpr(arg));
        }
        return new XValue(DDBTypes.addNamePrefix(attrs));

    }

}
