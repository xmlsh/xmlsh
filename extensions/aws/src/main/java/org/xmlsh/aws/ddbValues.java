package org.xmlsh.aws;

import java.util.ArrayList;
import java.util.List;
import org.xmlsh.aws.util.DDBTypes;
import org.xmlsh.core.AbstractBuiltinFunction;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.Shell;

public class ddbValues extends AbstractBuiltinFunction {

  public ddbValues() {
    super("ddb-values");
  }

  @Override
  public XValue run(Shell shell, List<XValue> args) throws Exception {
    List<XValue> list = new ArrayList<XValue>(args.size());
    for(XValue arg : args) {
      list.add(XValue.newXValue(DDBTypes.parseAttrValue(arg)));
    }
    return XValue.newXValue(list);
  }

}
