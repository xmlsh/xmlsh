package org.xmlsh.modules.types.map;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.annotations.Function;
import org.xmlsh.core.AbstractBuiltinFunction;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.XValue;
import org.xmlsh.modules.types.Types;
import org.xmlsh.sh.module.ModuleConfig;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.types.TypeFamily;
import org.xmlsh.types.XTypeUtils;
import org.xmlsh.types.xtypes.IXValueMap;
import org.xmlsh.util.Util;

@org.xmlsh.annotations.Module(name = "types.map")
public class MapModule extends Types {
    static Logger mLogger = LogManager.getLogger();

    public MapModule(ModuleConfig config) throws CoreException {
        super(config);
        mLogger.entry(config);
    }

    @Function(name = "new", names = { "map" })
    public static class _new extends AbstractBuiltinFunction {

        @Override
        public XValue run(Shell shell, List<XValue> args) throws Exception {
            return XValue.newXValue(TypeFamily.XTYPE, XTypeUtils
                    .newMapFromList(args));
        }
    }

    @Function(name = "get-value", names = { "get", "value", "property" })
    public static class get extends Types.value {
        public XValue run(Shell shell, List<XValue> args) throws Exception {

            requires(args.size() == 2, "map key");
            requires(args.get(0).isInstanceOf(
                    org.xmlsh.types.xtypes.IXValueMap.class),
                    "arg0 must be a map type");
            requires(args.get(1).isAtomic(), "arg1 must be a value type");
            IXValueMap m = args.get(0).asInstanceOf(
                    org.xmlsh.types.xtypes.IXValueMap.class);
            return m.get(args.get(1).toString());
        }

    }

    @Function(name = "put", names = { "set", "set-value" })
    public static class setValue extends Types.put {
    }

    @Function("has-key")
    public static class hasKey extends Types.containsKey {

    }

    @Function("keys")
    public static class keys extends Types.keys {
    }

}
