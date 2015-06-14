package org.xmlsh.modules.datamapping;

import java.util.HashMap;
import java.util.Map;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.module.ModuleConfig;
import org.xmlsh.sh.module.PackageModule;
import org.xmlsh.util.JavaUtils;

@org.xmlsh.annotations.Module(name="java")
public class DataMappingModule extends PackageModule {
	protected static Logger mLogger = LogManager.getLogger();

	
	@SuppressWarnings("serial")
	static Map<String,Class<?>> mClassAlias = new HashMap<String,Class<?>>(){
		{
			add(java.lang.String.class,"string", "str","text" );
			add( java.lang.Integer.class, "int","integer"  );
			add( java.lang.Double.class , "double","number");
		}
		private void add( Class<?> cls , String... names){
			for( String n : names)
				put( n , cls );
		}
		
	};

	public DataMappingModule(ModuleConfig config) throws CoreException {
		super(config);
	}

	static  Class<?> newType(String name, Class<Object> baseClass,
			ClassLoader classLoader) {
	
		mLogger.entry(name,baseClass,classLoader);
		
	    DynamicType.Builder<?> b = new ByteBuddy().subclass(baseClass).name(name);
	
	    Class<?> cls = b.make()
	            .load(classLoader, ClassLoadingStrategy.Default.INJECTION)
	            .getLoaded();
	     return mLogger.exit(cls) ;
	
	}

	static Class<?> lookup(String name) {
		if( name.contains("."))
				return null ;
		Class<?> cls = JavaUtils.fromPrimativeName(name);
		if( cls != null )
			return cls ;
		// Standard synonms 
		
		return DataMappingModule.mClassAlias.get( name.toLowerCase());
		
		
	}

	static  Class<?> resolveClass(String name, 
			XValue xcls, ClassLoader classLoader) throws ClassNotFoundException,
			CoreException { 
		Class<?> cls = null;
		  if( xcls.isInstanceOf(Class.class))
	            cls = xcls.asInstanceOf(Class.class);
		  
		if( cls == null ){
			    if( xcls.isString())
			       cls = lookup( xcls.toString());
			    if( cls == null )
			    	cls =  JavaUtils.convertToClass(xcls, classLoader);
			}
		    if( cls == null ){
		    	if( name == null )
		    		name = xcls.toString();
		    	cls = newType( name , Object.class , classLoader );
		    }
		return cls;
	}

}
