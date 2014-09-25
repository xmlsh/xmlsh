package org.xmlsh.sh.module;

import static org.xmlsh.util.Util.stringConcat;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.ScriptCommand.SourceMode;
import org.xmlsh.core.ICommand;
import org.xmlsh.core.ScriptSource;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.FileUtils;
import org.xmlsh.util.JavaUtils;
import org.xmlsh.util.StringPair;
import org.xmlsh.util.Util;

public class ModuleFactory
{
  private  final static Logger mLogger = LogManager.getLogger();


  public static Module createExternalModule(Shell shell, String nameuri, List<URL> at) throws CoreException
  {
    return new ExternalModule( shell, nameuri, at );
  }



  public static Module createJavaModule(Shell shell,String  classname, List<URL> at) throws CoreException
  {
    return new JavaModule(shell, classname, at);
  }


  // 
  static IModule createModuleModule(Shell shell , StringPair pair , List<URL> at ) throws Exception
  {


		if (pair.hasLeft()) { // prefix:name , prefix non-empty
			IModule m ;
			mLogger.trace("found prefix - trying moduel by prefix: ", pair);
			
			if( Util.isBlank(pair.getLeft()) ){
				m = shell.getModule()	;
				mLogger.trace("blank prefix - use current module",m);
 			} else {
					m = shell.getModuleByPrefix(pair.getLeft());
					mLogger.debug("Preix module : " , m );
			}
			
			// Allow C:/xxx/yyy to work
			// May look like a namespace but isnt
	
			if (m != null) {
				mLogger.trace("Found prefixed module - try getting child module" , m , pair.getRight());
				IModule mod = m.getModule(shell,pair.getRight(), at );
				if (mod != null) {
					mLogger.debug("Module Class found: " , mod );
					return mod;
				}
	
				return mLogger.exit(null);
			}
		}

		
		
		/*
		 * Try all default modules

	   mLogger.debug("Try default modules");
		for (IModule m : shell.getDefaultModules() ) {
			    assert( m != null );
			    IModule sm = shell.getModule();
			    if( sm.equals(m)){
			    	mLogger.trace("Skipping default module same as shell's module {}" , m );
			        continue; 
			    }
			    if( RootModule.isEqual( m ) ) {
			    	mLogger.trace("Skipping root module {}",m);
			    	continue;
			    }
			    
			    mLogger.trace("Trying module {} shell's module is {} " , m , sm );
				Module mod = m.getModule(shell,pair.getRight());
				if (mod != null) {
					return mLogger.exit(mod);
				}
			}
					 */
		return mLogger.exit(null);
		
  }
  public static IModule createModule(Shell shell, String name, List<URL> at )  throws Exception
  {
	  return createModule(shell, new PName(name), at);
  }
  public static IModule createModule(Shell shell, PName qname, List<URL> at )  throws Exception
  {
	 
	mLogger.entry(shell, qname, at);
	  

	
	String name = qname.getName();
	String prefix =  qname.getPrefix();
    
    IModule mod = null ;
  
    // special scheme
    if(prefix != null && Util.isEqual(prefix, "java"))
      mod = createJavaModule(shell, name, at  );
    
    if( mod == null && prefix  == null ){
    	mod = createInternalModule( shell, name );
    	
    }
    if( mod == null  ){
    	mod = createModuleModule(shell, qname , at );
    	
    }
    if( mod == null )
    {
    // Try to find script source by usual means 
       ScriptSource script  = CommandFactory.getScriptSource(shell,qname ,SourceMode.IMPORT , at );
       if( script != null )
         mod = createScriptModule(shell ,script, qname.toString() );
    } 
    if( mod == null )
        mod = createExternalModule(shell, qname.toString() , at);
    
    if( mod != null )
      mod.onLoad(shell);
    return mLogger.exit(mod) ;
  }



  /*
   * Look for a Class module in the internal packages 
   * 
   */
static IModule createInternalModule(Shell shell, String nameuri ) throws CoreException, InvalidArgumentException, ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
	
	  ClassLoader classLoader = RootModule.getInstance().getClassLoader();
	  List<String> packages = Collections.singletonList("org.xmlsh.modules");
	  IModule mod = null ;
	mLogger.debug("trying to find internal module by name: {} " , nameuri );
	ModuleConfig config  =  findPackageModule( shell , nameuri , packages , classLoader  );
	if( config != null ){
		mod = createPackageModule( config , classLoader );
		if( mod != null )
		   mLogger.debug("created internal module: ");
	}
	
	/*
	 * Look for internal scripts - Defer to  modules to find them
	 */
	return mod;
}

  public static IModule createPackageModule( Shell shell, String name, List<String> pkgs, ClassLoader classLoader , String helpURL) throws ClassNotFoundException, InvalidArgumentException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
  {
	ModuleConfig config = new ModuleConfig( name, null , shell.getSerializeOpts() , pkgs, helpURL);
    return createPackageModule( config, classLoader  );
  }

  /*
   * Create a PackageModule or derived Module based on configuration
   */
  public static IModule createPackageModule( ModuleConfig config, ClassLoader classLoader ) throws ClassNotFoundException, InvalidArgumentException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
  {
	
	mLogger.entry(config, classLoader);
	if( config== null )
		return null ;
	
	String moduleClassName = config.getModuleClass();
	if( ! Util.isBlank(moduleClassName)){
		Class<?> cls = JavaUtils.findClass(moduleClassName, classLoader);
		if( ! IModule.class.isAssignableFrom(cls) ) {
			mLogger.warn("Module class does not implement IModule" , cls );
			return null ;
		}
		mLogger.info("Creating custom package module: {} " , cls);
		return mLogger.exit((IModule) JavaUtils.newObject(cls ,  config ));
	}
	
    return mLogger.exit(new PackageModule( config  ));
  }
  
  public static Module createScriptModule(Shell shell, ScriptSource script, String nameuri ) throws CoreException, IOException
  {
    return new ScriptModule(shell, script, nameuri );
  }
  
  
  /*
   * TEMPORARY HAck - try a name to see if it is an internal module in org.xmlsh.modules by looking
   * for a sub package 
   */

	 static ModuleConfig  findPackageModule( Shell shell , String name ,  List<String> list ,  ClassLoader classLoader )
	{
		
		mLogger.entry( name);
		ModuleConfig config = new ModuleConfig(); 
		
		for( String p : list){
			/* Look for module under modules */
			String pkgn = p +  "." + name ;
			
			Package pkg = Package.getPackage( pkgn );
			if( pkg == null ){
				try {
				Class<?> cls = JavaUtils.findClass(pkgn + ".package-info" , classLoader );
				  mLogger.info("found package info clas: {} " + cls.getName() );
				  
	
				  
				  
				} catch( ClassNotFoundException e ){
					// mLogger.catching(e);
					mLogger.trace("Cant find package-info - skipping ");
				}
			    pkg = Package.getPackage( pkgn );
			   
			}
			if( pkg != null ){
				mLogger.info("Found modules package: {} " , pkg.getName() );
				 config.setName(name);
				 config.setSerialOpts(shell.getSerializeOpts() );
				 config.setPackages(Collections.singletonList(pkg.getName()));
			     reflectPackageAnnotations( pkg , config );
			     

				 return mLogger.exit( config );
			}
		}

	     return mLogger.exit(null);
		
	}



	private static void reflectPackageAnnotations(Package pkg, ModuleConfig config) {
		
	
		org.xmlsh.annotations.Module ma = pkg.getAnnotation(org.xmlsh.annotations.Module.class);
		if( ma != null ){
			if( !Util.isBlank(ma.name()))
				config.setName(ma.name());
			String moduleClass = ma.moduleClass();
			if( !Util.isBlank(moduleClass) ){
				if( moduleClass.indexOf('.') <0 )
					moduleClass = pkg.getName() + "." + moduleClass ;
				config.setModuleClass(moduleClass);
			}
		}
		
		
	}

}
