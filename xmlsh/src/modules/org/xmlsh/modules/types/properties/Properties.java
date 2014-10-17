package org.xmlsh.modules.types.properties;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.annotations.Function;
import org.xmlsh.core.AbstractBuiltinFunction;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.XValue;
import org.xmlsh.core.XValueProperties;
import org.xmlsh.core.XValueSequence;
import org.xmlsh.modules.types.Types;
import org.xmlsh.sh.module.ModuleConfig;
import org.xmlsh.sh.module.PackageModule;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.Util;


@org.xmlsh.annotations.Module( name="types.properties")
public class Properties extends Types {
	static Logger mLogger = LogManager.getLogger();

	public Properties(ModuleConfig config) throws CoreException {
		super(config);
		mLogger.entry(config);
	}
	
	@Function( name="keys")
	public static class keys extends AbstractBuiltinFunction 
	{
		@Override
		public XValue run(Shell shell, List<XValue> args) throws Exception {
			 XValueSequence list = new XValueSequence();
			    for( XValue x : args ) { 
			      if( x.isInstanceOf(XValueProperties.class))
			          for( String keys : Util.toList(  
			        		    ((XValueProperties)x.asInstanceOf(XValueProperties.class)).keySet().iterator()) ) 
			        		{
			            list.addValue( XValue.newXValue(keys) );
			          }
			        }
			    return list.asXValue();
		}
	}
	
	@Function( name="values")
	public static class values extends AbstractBuiltinFunction 
	{
		@Override
		public XValue run(Shell shell, List<XValue> args) throws Exception {
			 XValueSequence list = new XValueSequence();
			    for( XValue x : args ) { 
			      if( x.isInstanceOf(XValueProperties.class))
			          for( XValue xv  :  ((XValueProperties)x.asInstanceOf(XValueProperties.class)).values() )
			            list.addValue( xv );
			          }
			    return list.asXValue();
		}
	}
	
	@Function( name="get")
	public static class get extends AbstractBuiltinFunction 
	{
		@Override
		public XValue run(Shell shell, List<XValue> args) throws Exception {
			if( args.size() != 2 ||
					! args.get(0).isInstanceOf(XValueProperties.class) ){
				usage(shell, "properties key");
				return null;
				
			}
			return  args.get(0).asInstanceOf(XValueProperties.class).get( args.get(1).toString() );
	}
	}
	
	
	@Function( name="has-key")
	public static class hasKey extends AbstractBuiltinFunction 
	{
		@Override
		public XValue run(Shell shell, List<XValue> args) throws Exception {
			if( args.size() != 2 ||
					! args.get(0).isInstanceOf(XValueProperties.class) ){
				usage(shell, "properties key");
				return null;
				
			}
			return  XValue.newXValue(
					args.get(0).asInstanceOf(XValueProperties.class).
					containsKey(args.get(1).toString()));
	}
	}
	
	
	@Function( name="put")
	public static class set extends AbstractBuiltinFunction 
	{
		@Override
		public XValue run(Shell shell, List<XValue> args) throws Exception {
			if( args.size() != 3 ||
					! args.get(0).isInstanceOf(XValueProperties.class) ){
				usage(shell, "properties key");
				return null;
			}
			return  args.get(0).asInstanceOf(XValueProperties.class).put( args.get(1).toString() , args.get(2) );
	}
	}
	@Function( name="get-value" , names={"value" , "property"} )
	public static class getValue extends  Types.value  {
	  }
	

}
