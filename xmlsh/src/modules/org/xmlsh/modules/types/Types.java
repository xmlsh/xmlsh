package org.xmlsh.modules.types;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.annotations.Function;
import org.xmlsh.core.AbstractBuiltinFunction;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.module.ModuleConfig;
import org.xmlsh.sh.module.PackageModule;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.types.xtypes.IXValueMap;
import org.xmlsh.types.xtypes.XValueSequence;
import org.xmlsh.util.Util;


@org.xmlsh.annotations.Module(name="types")
public class Types extends PackageModule {
	static Logger mLogger = LogManager.getLogger();

	public Types( ModuleConfig config) throws CoreException {
		super( config);
		mLogger.entry( config);
	}

	@Override
	public void onInit(Shell shell, List<XValue> args) throws Exception {

		
		mLogger.entry(shell, args);
		super.onInit(shell, args);
		
	}

	@Override
	public void onLoad(Shell shell) {
		
		mLogger.entry(shell);
		super.onLoad(shell);
	}
	
	
@Function(name="is-empty")
public static class isEmpty extends AbstractBuiltinFunction
{

  @Override
  public XValue run(Shell shell, List<XValue> args) throws Exception
  {
    for( XValue arg : args )
    	if( ! arg.isEmpty())
    		return XValue.newXValue(false) ;
    return XValue.newXValue(true) ;
  }

}

@Function(name="is-atomic")
public class isAtomic extends AbstractBuiltinFunction
{


  @Override
  public XValue run(Shell shell, List<XValue> args) throws Exception
  {
     if( args.size() != 1 ) 
    	 return XValue.newXValue(false);
     return XValue.newXValue( args.get(0).isAtomic() );
	  
  }

}

@Function( name="values")
public static class values extends AbstractBuiltinFunction
{


  @Override
  public XValue run(Shell shell, List<XValue> args) throws Exception
  {
    XValueSequence list = new XValueSequence();
    for( XValue x : args ) { 
      for( XValue v : x.getXValues() ){
            list.addValue( v );
      }
    }
    return list.asXValue();
  }
}
@Function(name="value")
public static class value extends AbstractBuiltinFunction
{


  @Override
  public XValue run(Shell shell, List<XValue> args) throws Exception
  {
	  if( args.isEmpty())
		  return XValue.nullValue();
	  if( args.size() == 1 )
		  return args.get(0); // TODO Need default value 
	  
     if( args.size() != 2 || ! args.get(1).isAtomic() ){
    	 usage(shell, "object key");
		  return XValue.nullValue();
     }
     
     
     return XValue.newXValue( args.get(0).getNamedValue( args.get(1).toString() ) );
	  
  }

}

@Function( name="keys")
public static class keys extends AbstractBuiltinFunction
{
  @Override
  public XValue run(Shell shell, List<XValue> args) throws Exception
  {
    XValueSequence list = new XValueSequence();
    for( XValue x : args ) { 
      if( x.isXType() ) {
        Object o = x.asObject();
        if( o instanceof org.xmlsh.types.xtypes.IXValueMap ) {
          IXValueMap m = (IXValueMap) o ;
          for( String keys : Util.toList(  m.keySet().iterator()) ) {
            list.addValue( XValue.newXValue(keys) );
          }
        }
      }
    }
    return list.asXValue();
  }
}

@Function( name = "contains-key")
public static class containsKey extends AbstractBuiltinFunction
{


  @Override
  public XValue run(Shell shell, List<XValue> args) throws Exception
  {
     if( args.size() != 2 || ! args.get(1).isAtomic() ){
    	 usage(shell, "container key");
    	 return XValue.newXValue(false);
     }
     return XValue.newXValue( args.get(0).getTypeMethods().hasKey( args.get(0).asObject() , args.get(1).toString() ) );
  }

}
@Function( name = "put",names={"set"})
public static class put  extends AbstractBuiltinFunction
{


  @Override
  public XValue run(Shell shell, List<XValue> args) throws Exception
  {
	 super.requires(args.size() == 3, "put( obj , key , value )");
	 super.requires(args.get(0).isXType(), "obj must be an XType");
	 requires(args.get(1).isAtomic() , "key must be atomic");
     return  args.get(0).getTypeMethods().setXValue( args.get(0), args.get(1).toString() , args.get(2)  );
  }

}





}