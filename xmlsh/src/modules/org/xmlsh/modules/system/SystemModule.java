package org.xmlsh.modules.system;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.xmlsh.annotations.Command;
import org.xmlsh.annotations.Function;
import org.xmlsh.core.AbstractBuiltinFunction;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.ICommand;
import org.xmlsh.core.XClassLoader;
import org.xmlsh.core.XValue;
import org.xmlsh.modules.types.map.MapModule;
import org.xmlsh.sh.module.IModule;
import org.xmlsh.sh.module.ModuleConfig;
import org.xmlsh.sh.module.PackageModule;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.types.xtypes.XValueList;
import org.xmlsh.types.xtypes.XValueProperties;

@org.xmlsh.annotations.Module(name="system")
public class SystemModule  extends PackageModule  {
    public SystemModule(ModuleConfig config, XClassLoader loader) throws CoreException {
        super(config, loader);
    }
    

    static XValueProperties _p() {
    	return XValueProperties.fromJavaProperties( System.getProperties() );
    }

    @Function( name="properties" , names={"sysprops"} )
	public static class properties extends AbstractBuiltinFunction {

		@Override
		public XValue run(Shell shell, List<XValue> args) throws Exception {
			return  _p().asXValue();
		}  
    }
    
    @Function( name="keys" , names={"property-names","names"} )
	public static class keys extends MapModule.keys {

		@Override
		public XValue run(Shell shell, List<XValue> args) throws Exception {
			return super.run( shell , Collections.singletonList( _p().asXValue() ) );
			
		}  
    }

    
    @Function( name="property" , names={ "get" , "value"}) 
    public static class property extends AbstractBuiltinFunction {
		@Override
		public XValue run(Shell shell, List<XValue> args) throws Exception {
			requires( args.size() == 1, "System property key" );
			return XValue.newXValue( System.getProperty(args.get(0).toString()));
		}
    
    }
    
    @Function( name="isWindows") 
    @Command( name="is-windows")
    public static class isWindows extends AbstractBuiltinFunction {
		@Override
		public XValue run(Shell shell, List<XValue> args) throws Exception {
			requires( args.isEmpty() , "no arguments");
			return XValue.newXValue( System.getProperty("os.name").toLowerCase().contains("win") );
		}
    }
    
    @Function( name="isUnix" , names={ "is-unix" }) 
    public static class isUnix extends AbstractBuiltinFunction {
		@Override
		public XValue run(Shell shell, List<XValue> args) throws Exception {
			requires( args.isEmpty() , "no arguments");
	return XValue.newXValue( System.getProperty("os.name").toLowerCase().contains("linux") ||
			 System.getProperty("os.name").toLowerCase().contains("Mac") );
      }
    }
    
    @Function( name="isLinux" , names={ "is-linux" }) 
    public static class isLinux extends AbstractBuiltinFunction {
		@Override
		public XValue run(Shell shell, List<XValue> args) throws Exception {
			requires( args.isEmpty() , "no arguments");
			return XValue.newXValue( System.getProperty("os.name").toLowerCase().contains("linux") );
		}
    }
    @Function( name="isMac" , names={ "is-mac" , "is-macos" }) 
    public static class isMac extends AbstractBuiltinFunction {
		@Override
		public XValue run(Shell shell, List<XValue> args) throws Exception {
			requires( args.isEmpty() , "no arguments");
			return XValue.newXValue( System.getProperty("os.name").toLowerCase().contains("Mac") );
		}
    }
    
}
