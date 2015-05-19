package org.xmlsh.aws;

import java.util.List;

import org.xmlsh.aws.util.DDBTypes;
import org.xmlsh.aws.util.DDBTypes.IAttrValueExpr;
import org.xmlsh.aws.util.DDBTypes.AttrValueExpr;
import org.xmlsh.aws.util.DDBTypes.NameAttrValueMap;
import org.xmlsh.core.BuiltinFunctionCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.Shell;

public class ddbAttrValueExprs extends BuiltinFunctionCommand {

    public ddbAttrValueExprs() {
        super("ddb-attr-value-expr");
    }

    @Override
    public XValue run(Shell shell, List<XValue> args) throws Exception {
        IAttrValueExpr attrs = new AttrValueExpr();
        for (XValue arg : args) {
            attrs.putAll(DDBTypes.parseAttrValueExpr(arg));
        }
        return new XValue(DDBTypes.addValuePrefix(attrs));
    }

}
