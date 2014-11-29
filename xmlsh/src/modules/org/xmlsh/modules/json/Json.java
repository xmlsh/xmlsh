package org.xmlsh.modules.json;

import java.util.List;

import org.xmlsh.annotations.Function;
import org.xmlsh.core.AbstractBuiltinFunction;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.XValue;
import org.xmlsh.json.JSONUtils;
import org.xmlsh.sh.module.ModuleConfig;
import org.xmlsh.sh.module.PackageModule;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.types.TypeFamily;
import org.xmlsh.util.JavaUtils;

@org.xmlsh.annotations.Module(name="json-types")
public class Json extends PackageModule  {

	public Json(ModuleConfig config) throws CoreException {
		super(config);
		// TODO Auto-generated constructor stub
	}

		@Function( name = "getType")
		public static class getType extends AbstractBuiltinFunction
		{
		
		  @Override
		  public XValue run(Shell shell, List<XValue> args) throws Exception
		  {
			  requires(args.size() > 0 , "getType( name ) ");

			  Class<?> cls = JavaUtils.convertToClass(args.get(0), shell);
			  if( cls == null || ! (cls instanceof Class ) )
				  throw new InvalidArgumentException("Cannot convert to class");
			  
			  return XValue.newXValue( TypeFamily.JSON ,
					  JSONUtils.getJsonObjectMapper().constructType( cls ));
			  
			  
		     
		  }

}

}
