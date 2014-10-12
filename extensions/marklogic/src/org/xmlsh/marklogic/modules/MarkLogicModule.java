package org.xmlsh.marklogic.modules;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.ICommand;
import org.xmlsh.core.IFunctionExpr;
import org.xmlsh.core.XClassLoader;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.module.ExternalModule;
import org.xmlsh.sh.module.Module;
import org.xmlsh.sh.module.ModuleConfig;
import org.xmlsh.sh.shell.Shell;

@org.xmlsh.annotations.Module
public class MarkLogicModule extends ExternalModule {

     static Logger mLogger = LogManager.getLogger();
	
	public MarkLogicModule(ModuleConfig config, XClassLoader loader) throws CoreException {
		super(config, loader);
	  mLogger.entry(config, loader);
	
		
	}

	@Override
	public void onInit(Shell shell, List<XValue> args) throws Exception {
		super.onInit(shell, args);
	  mLogger.entry(shell, args);
	}

	@Override
	public void onLoad(Shell shell) {
		super.onLoad(shell);
        mLogger.entry(shell);


	}


}
