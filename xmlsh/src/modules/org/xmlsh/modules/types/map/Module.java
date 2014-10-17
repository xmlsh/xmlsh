package org.xmlsh.modules.types.map;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.annotations.Function;
import org.xmlsh.core.AbstractBuiltinFunction;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.XConfiguration;
import org.xmlsh.core.XValue;
import org.xmlsh.core.XValueProperties;
import org.xmlsh.core.XValueSequence;
import org.xmlsh.modules.types.Types;
import org.xmlsh.sh.module.ModuleConfig;
import org.xmlsh.sh.module.PackageModule;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.types.TypeFamily;
import org.xmlsh.types.XTypeUtils;
import org.xmlsh.util.StringPair;
import org.xmlsh.util.Util;


@org.xmlsh.annotations.Module( name="types.map")
public class Module extends Types {
	static Logger mLogger = LogManager.getLogger();

	public Module(ModuleConfig config) throws CoreException {
		super(config);
		mLogger.entry(config);
	}

	@Function( name="new" , names={"map"})
	public static class _new extends AbstractBuiltinFunction {

		@Override
		public XValue run(Shell shell, List<XValue> args) throws Exception {
			return XValue.newXValue(TypeFamily.XTYPE ,  XTypeUtils.newMapFromList(args));
		}
	}
	@Function( name="get-value" , names={"value" , "property"} )
	public static class getValue extends  Types.value  {
	  }
	
	
	@Function( "keys" )
	public static class keys extends Types.keys {
	}

}
