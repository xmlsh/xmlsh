package org.xmlsh.modules.types.config;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.annotations.Function;
import org.xmlsh.core.AbstractBuiltinFunction;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.XConfiguration;
import org.xmlsh.core.XValue;
import org.xmlsh.modules.types.properties.PropertiesModule;
import org.xmlsh.sh.module.ModuleConfig;
import org.xmlsh.sh.shell.Shell;


@org.xmlsh.annotations.Module( name="types.config")
public class ConfigModule extends PropertiesModule {
	static Logger mLogger = LogManager.getLogger();

	public ConfigModule(ModuleConfig config) throws CoreException {
		super(config);
		mLogger.entry(config);
	}

	@Function( name="get-section" , names={"section","properties"} )
	public static class getSection extends AbstractBuiltinFunction {
	  @Override
	  public XValue run(Shell shell, List<XValue> args) throws Exception
	  {
		mLogger.entry(shell, args);
		
		
	    if( args.size() != 2 ||! args.get(0).isInstanceOf( XConfiguration.class)){
	    	usage(shell, "config section-name");
		    return XValue.nullValue();
	    }
	    
	    XConfiguration conf = args.get(0).asInstanceOf(XConfiguration.class );   
	    String name = args.get(1).toString();
		return mLogger.exit(XValue.newXValue(conf.getSection(name)));
		
	  }
	}
	

	@Function( name="get" , names={"value" , "get-value", "property"} )
	public static class get extends PropertiesModule.get {
		@Override
	  public XValue run(Shell shell, List<XValue> args) throws Exception
	  {
	   
	    mLogger.entry(shell, args);
		
		
	    if( args.size() < 2 ||! args.get(0).isInstanceOf( XConfiguration.class))
	    	usage(shell, "config section-name key-name");
	    XConfiguration conf = args.get(0).asInstanceOf(XConfiguration.class );   

	    switch( args.size()){
	    case 2: return run( shell , conf , args.get(1).toString() );
	    case 3: return run( shell , conf , args.get(1).toString() , args.get(2).toString());
	    default: usage( shell, "config section-name key-name"); // Throws
	    }
		assert(false);
		return null;
	  }
	
		
		public XValue run( Shell shell , XConfiguration conf,  String section , String name ) throws InvalidArgumentException{
			  return XValue.newXValue(conf.getProperty(section, name));
		}
		
		public XValue run( Shell shell , XConfiguration conf,  String name ) throws InvalidArgumentException{
		    
		    return conf.get(name);
		}
	}
	
	
	@Function( name="sections" )
	public static class keys extends PropertiesModule.keys {
	}
	
   
    
	// Extend Properties module so has-key overrides/exchangable  
    @Function( name="has-key")
    public static class hasKey extends PropertiesModule.hasKey 
    {
        @Override
        public XValue run(Shell shell, List<XValue> args) throws Exception {

            if( args.size() > 0 && ! 
                args.get(0).isInstanceOf(XConfiguration.class)
            ) {
                usage(shell, "$1 must be a Configuration object");
                return null;
            }
            
            XConfiguration config = args.get(0).asInstanceOf(XConfiguration.class);
            switch(args.size())
            {
            case 2:
                return XValue.newXValue(config.containsKey(args.get(1).toString()));
            case 3:
                return XValue.newXValue(config.containsKey(args.get(1).toString(),args.get(2).toString()));
            default:
                usage(shell, "config section [key]");
                return null ;
                
            }
    }
    }
	

}


