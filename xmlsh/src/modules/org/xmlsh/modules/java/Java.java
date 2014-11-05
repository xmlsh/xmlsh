package org.xmlsh.modules.java;

import java.util.List;

import org.xmlsh.annotations.Function;
import org.xmlsh.core.AbstractBuiltinFunction;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.module.ModuleConfig;
import org.xmlsh.sh.module.PackageModule;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.JavaUtils;

@org.xmlsh.annotations.Module(name="java-types")
public class Java extends PackageModule {

	public Java(ModuleConfig config) throws CoreException {
		super(config);
		// TODO Auto-generated constructor stub
	}
	

	@Function( name = "getClass")
	public static class getClass extends AbstractBuiltinFunction
	{


	  @Override
	  public XValue run(Shell shell, List<XValue> args) throws Exception
	  {
		  requires(args.size() > 0 , "getClass( name ) ");
			return XValue.newXValue(JavaUtils.convertToClass(args.get(0), shell));
	  }
	}

}
