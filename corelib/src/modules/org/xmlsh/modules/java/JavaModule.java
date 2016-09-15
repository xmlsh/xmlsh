package org.xmlsh.modules.java;

import java.util.List;

import org.xmlsh.annotations.Function;
import org.xmlsh.core.AbstractBuiltinFunction;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.XClassLoader;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.module.ModuleConfig;
import org.xmlsh.sh.module.PackageModule;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.types.TypeFamily;
import org.xmlsh.util.JavaUtils;

@org.xmlsh.annotations.Module(name="java")
public class JavaModule extends PackageModule {

	public JavaModule(ModuleConfig config, XClassLoader loader) throws CoreException {
		super(config, loader);
		// TODO Auto-generated constructor stub
	}
	

	@Function( name = "get-class" , names={"class"} )
	public static class getClass extends AbstractBuiltinFunction
	{

	  @Override
	  public XValue run(Shell shell, List<XValue> args) throws Exception
	  {
		  requires(args.size() > 0 , "( name ) ");
			return XValue.newXValue(JavaUtils.convertToClass(args.get(0), shell));
	  }
	}
	@Function( name="new" , names={"new", "jnew"} )
	public static class _new extends AbstractBuiltinFunction {

	    @Override
	    public XValue run(Shell shell, List<XValue> args) throws Exception {

	        Class<?> cls = JavaUtils.convertToClass(args.remove(0), shell);
	        XValue obj = null;
	        obj = JavaUtils.newXValue(cls, args);
	        return obj;


	    }

	}
}
