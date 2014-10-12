package org.xmlsh.aws.module;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.annotations.Function;
import org.xmlsh.core.AbstractBuiltinFunction;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.XClassLoader;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.module.ExternalModule;
import org.xmlsh.sh.module.ModuleConfig;
import org.xmlsh.sh.shell.Shell;

@org.xmlsh.annotations.Module
public class AWSModule extends ExternalModule {

     static Logger mLogger = LogManager.getLogger();
	
	public AWSModule(ModuleConfig config, XClassLoader loader) throws CoreException {
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

	@Function( name="ec2-client")
	public static class ec2Client extends AbstractBuiltinFunction {
		  @Override
		  public XValue run(Shell shell, List<XValue> args) throws Exception
		  {

			  return null ;
			  
		  }
		
	
	}
		


}
