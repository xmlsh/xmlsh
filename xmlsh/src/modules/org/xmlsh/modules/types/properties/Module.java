package org.xmlsh.modules.types.properties;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.module.ModuleConfig;
import org.xmlsh.sh.module.PackageModule;
import org.xmlsh.sh.shell.Shell;


@org.xmlsh.annotations.Module
public class Module extends PackageModule {
	static Logger mLogger = LogManager.getLogger();

	public Module(ModuleConfig config) {
		super(config);
		mLogger.entry(config);
	}

	@Override
	public void onInit(Shell shell, List<XValue> args) throws Exception {

		
		mLogger.entry(shell, args);
		super.onInit(shell, args);
		
	}

	@Override
	public void onLoad(Shell shell) {
		
		mLogger.entry(shell);
		super.onLoad(shell);
		reflectModuleClass( shell, propertyFunctions.class );
	}
	
	

}
