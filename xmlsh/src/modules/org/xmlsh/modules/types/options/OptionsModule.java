package org.xmlsh.modules.types.options;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.annotations.Function;
import org.xmlsh.core.AbstractBuiltinFunction;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.Options.OptionDef;
import org.xmlsh.core.UnknownOption;
import org.xmlsh.core.XValue;
import org.xmlsh.modules.types.Types;
import org.xmlsh.sh.module.ModuleConfig;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.types.TypeFamily;
import org.xmlsh.util.JavaUtils;


@org.xmlsh.annotations.Module( name="types.options")
public class OptionsModule extends Types {
	public OptionsModule(ModuleConfig config) throws CoreException {
		super(config);
	}

	static Logger mLogger = LogManager.getLogger();

	
	@Function( name="option-defs")
	public static class optionDefs extends AbstractBuiltinFunction {
	  @Override
	  public XValue run(Shell shell, List<XValue> args) throws Exception
	  {
			List<OptionDef> defs = parseDefs(args);
			return XValue.newXValue(TypeFamily.JAVA, defs);
		}
	}
	
	
	@Function( name="options")
	public static class options extends AbstractBuiltinFunction {
		@Override
		public XValue run(Shell shell, List<XValue> args) throws UnknownOption, InvalidArgumentException
		{
			if( args.isEmpty()){
		 	  usage(shell, "options( option-defs [args])");
		 	  return null ;
			}
	    	requires(! args.isEmpty() , "Usage: options( option-defs [args])");
            XValue xdefs = args.remove(0);
            List<OptionDef> defs = parseDefs( xdefs );
		
			Options opts = new Options( defs  );
			opts.parse(args);
			return XValue.newXValue(TypeFamily.JAVA,opts);
		}
    }
   protected static List<OptionDef> parseDefs( List<XValue> args) {
		List<OptionDef> defs = new ArrayList<>();
		for( XValue arg : args )
			defs.addAll( parseDefs(arg ) );
		return defs;
   }

	protected static List<OptionDef> parseDefs(XValue args) {
		List<OptionDef> defs = new ArrayList<>();
		for( XValue arg : args ){
			if( arg.isInstanceOf( OptionDef.class ))
				defs.add( arg.asInstanceOf(OptionDef.class));
			else
			    defs.addAll( parseDefs( arg.asObject()));
		}
		return defs;
	}
	
	protected static List<OptionDef> parseDefs(Object obj) {
		List<OptionDef> defs = new ArrayList<>();
		if( obj instanceof OptionDef )
			defs.add( (OptionDef) obj );
		else
	    if( JavaUtils.isArrayOf(obj,OptionDef.class  ) )
	    	defs.addAll(  Arrays.asList( (OptionDef[]) obj ));
	    else
	    if( obj instanceof List )
	    	for( Object o : (List<?>)obj )
	    		defs.addAll( parseDefs( o ));
	    else
	    	defs.addAll( Options.parseDefs( obj.toString() ));
		return defs;

	}

	@Function( name="has-opt")
	public static class hasOpt extends AbstractBuiltinFunction
	{

	  @Override
	  public XValue run(Shell shell, List<XValue> args) throws Exception
	  {
		  if( args.size() !=2 ||! args.get(0).isInstanceOf( Options.class)){
		    	usage(shell, "has-opt( options option-name");
			    return XValue.newInstance(false);
		    }
		    Options opts = args.get(0).asInstanceOf(Options.class );   
		    String name = args.get(1).toString();
		    return XValue.newInstance( opts.hasOpt(name));
	  }

	}
	

	@Function( name="get-opt",names={"option","flag","value"	})
	public static class getOpt extends AbstractBuiltinFunction
	{
	
	  @Override
	  public XValue run(Shell shell, List<XValue> args) throws Exception
	  {
		    if( args.size() < 2 ||! args.get(0).isInstanceOf( Options.class)){
		    	usage(shell, "options option [default]");
			    return XValue.nullValue();
		    }
		    
		    Options opts = args.get(0).asInstanceOf(Options.class );   
		    String name = args.get(1).toString();
		    XValue defaultValue  = args.size() > 2 ? args.get(2) :  null ;
		    
			OptionDef def = opts.getOptDef(name);
			if( def == null )
				return XValue.nullValue();
			if( def.isFlag()) 
			   return  
					     XValue.newXValue( opts.getOptFlag(name, defaultValue == null ?  true  : defaultValue.toBoolean() ) );
		    
		    XValue v = opts.getOptValue(name);
		    if( v == null )
		    	v = defaultValue ;
		    return v ;
	  }
	
	}
	
	@Function( name="get-args" , names={"args","remaining-args","remaining"})
	public static class getArgs extends AbstractBuiltinFunction
	{
	
	  @Override
	  public XValue run(Shell shell, List<XValue> args) throws Exception
	  {
		    if( args.size() != 1 ||! args.get(0).isInstanceOf( Options.class)){
		    	usage(shell, "get-args(opts)");
			    return XValue.nullValue();
		    }
		    return XValue.newXValue(((Options) args.get(0).asInstanceOf(Options.class)).getRemainingArgs());
	  }
	
	}
	

}
