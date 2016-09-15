package org.xmlsh.modules.types.properties;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.annotations.Function;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.XClassLoader;
import org.xmlsh.core.XValue;
import org.xmlsh.modules.types.map.MapModule;
import org.xmlsh.sh.module.ModuleConfig;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.types.xtypes.XValueProperties;


@org.xmlsh.annotations.Module( name="types.properties")
public class PropertiesModule extends MapModule {

	public PropertiesModule(ModuleConfig config, XClassLoader loader) throws CoreException {
		super(config, loader);
		mLogger.entry(config);
	}
	
	@Function( name="keys")
	public static class keys extends MapModule.keys 
	{
	}
	
	@Function( name="values")
	public static class values extends  MapModule.values 
	{
	}

	
	@Function( name="has-key")
	public static class hasKey extends MapModule.hasKey 
	{
	}
	
	
	@Function( name="put",names={"put-value","set","set-value"} )
	public static class set extends MapModule.setValue 
	{
		@Override
		public XValue run(Shell shell, List<XValue> args) throws Exception {
			if( args.size() != 3 ||
					! args.get(0).isInstanceOf(XValueProperties.class) ){
				usage(shell, "properties key value");
				return null;
			}
			return  args.get(0).asInstanceOf(XValueProperties.class).put( args.get(1).toString() , args.get(2) );
	}
	}
	   
    @Function( name="get", names={"value" , "property","get-value"} )
    public static class get extends  MapModule.get 
    {
    }

}
