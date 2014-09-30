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
import org.xmlsh.core.XClassLoader;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.FileUtils;
import org.xmlsh.util.JavaUtils;
import org.xmlsh.util.StringPair;
import org.xmlsh.util.Util;

public class ModuleFactory
{
  private  final static Logger mLogger = LogManager.getLogger();


  public static Module createExternalModule( Shell shell , ModuleConfig config ) throws CoreException
  {
    return new ExternalModule( config , shell.getClassLoader( config.getClassPath()));
  }


  public static Module createJavaModule(Shell shell , ModuleConfig config ) throws CoreException
  {
    return new JavaModule( config, shell.getClassLoader( config.getClassPath())) ;
  }


  // 
  static ModuleConfig createModuleModuleConfig(Shell shell , StringPair pair , List<URL> at ) throws Exception
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
				ModuleConfig config = m.getModuleConfig(shell,pair.getRight(), at );
				if (config != null) {
					mLogger.debug("Module Class found: " , config );
					return config;
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
  /*
  public static IModule createModule(Shell shell, PName qname, List<URL> at )  throws Exception
  {
	 
	mLogger.entry(shell, qname, at);
	
	ModuleConfig config = getModuleConfig(shell, qname, at);
    if( config == null )
    	return mLogger.exit(null);
	
	return mLogger.exit(createModule( shell , config ));
	
  }
*/

public static IModule createModule(Shell shell, ModuleConfig config) throws Exception {
	
	mLogger.entry(shell, config);
	assert( config != null );

	IModule mod = null;
	
	switch( config.getType() ){
	case "java" :
		mod = createJavaModule( shell, config );
		break ;
	case "external" :
		mod = createExternalModule( shell , config );
		break ;
		
	case "script" :
		mod = createScriptModule( shell ,  config ) ;
		break ;
	case "package" : 
		mod = createPackageModule( shell ,  config ) ; 
		break; 
	default : 
		assert(true);
		mLogger.error("Unexpected module configuration type: {} " , config.getType() );
		break ;
	}
    
    if( mod != null )
      mod.onLoad(shell);
    return mLogger.exit(mod) ;
}


public static ModuleConfig getModuleConfig(Shell shell, PName qname, List<URL> at )
		throws CoreException, ClassNotFoundException, InstantiationException,
		IllegalAccessException, InvocationTargetException, Exception,
		IOException, URISyntaxException {
	
	ModuleConfig config = null ;
	
	String name = qname.getName();
	String prefix =  qname.getPrefix();
    
  
    // special scheme
    if(prefix != null && Util.isEqual(prefix, "java"))
      config = JavaModule.getConfiguration(shell, name, at) ;
    
    if( config  == null && prefix  == null ){
    	config  = getInternalModuleConfig(shell , name , at );
    	
    }
    if( config == null  ){
    	config  = createModuleModuleConfig(shell, qname , at );
    	
    }
    if( config == null )
    {
    // Try to find script source by usual means 
       ScriptSource script  = CommandFactory.getScriptSource(shell,qname ,SourceMode.IMPORT , at );
       if( script != null )
         config = ScriptModule.getConfiguration(shell ,script,  at  );
    } 
    if( config == null )
        config = ExternalModule.getConfiguration(shell, qname.toString() , at);
	return config ;
}



  /*
   * Look for a Class module in the internal packages 
   * 
   */
static ModuleConfig  getInternalModuleConfig(Shell shell, String nameuri, List<URL> at  ) throws CoreException, InvalidArgumentException, ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
	
	  ClassLoader classLoader = RootModule.getInstance().getClassLoader();
	  List<String> packages = Collections.singletonList("org.xmlsh.modules");
	mLogger.debug("trying to find internal module by name: {} " , nameuri );
	ModuleConfig config  =  getPackageModuleConfig( shell , nameuri , packages , at ,  RootModule.getInstance().getConfig().getHelpURI() );
	return config;
}



public static ModuleConfig getInternalModuleConfig(Shell shell,
		String name, List<String> packages,
		String helpXml) {
	return new ModuleConfig("packages",name,null, shell.getSerializeOpts(), packages, helpXml);
}



// ModuleConfig config = new ModuleConfig( "package" , name, null , shell.getSerializeOpts() , pkgs, helpURL);

  /*
   * Create a PackageModule or derived Module based on configuration
   */
  public static IModule createPackageModule( Shell shell, ModuleConfig config  ) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, CoreException
  {
	
	mLogger.entry(config);
	if( config== null )
		return null ;
	
	String moduleClassName = config.getModuleClass();
	XClassLoader loader = shell.getClassLoader( config.getClassPath() );

	if( ! Util.isBlank(moduleClassName)){
		Class<?> cls = JavaUtils.findClass(moduleClassName, loader );
		if( ! IModule.class.isAssignableFrom(cls) ) {
			mLogger.warn("Module class does not implement IModule" , cls );
			return null ;
		}
		mLogger.info("Creating custom package module: {} " , cls);
		return mLogger.exit((IModule) JavaUtils.newObject(cls ,  config ));
	}
	
    return mLogger.exit(new PackageModule( config  ,  loader  ));
  }
  


public static Module createScriptModule(Shell shell, ModuleConfig config ) throws CoreException, IOException
  {
    return new ScriptModule( shell , config  );
  }
  
  
  /*
   * TEMPORARY HAck - try a name to see if it is an internal module in org.xmlsh.modules by looking
   * for a sub package 
   */

	public static ModuleConfig  getPackageModuleConfig( Shell shell , String name ,  List<String> packages ,   List<URL> at, String helpURI ) throws CoreException
	{
		
		mLogger.entry( name);
		ModuleConfig config = new ModuleConfig("package"); 
		
		for( String p : packages){
			/* Look for module under modules */
			String pkgn = p +  "." + name ;
			
			
			Package pkg = Package.getPackage( pkgn );
			if( pkg == null ){
				try {
				Class<?> cls = JavaUtils.findClass(pkgn + ".package-info" , shell.getClassLoader(at) );
				  mLogger.info("found package info clas: {} " + cls.getName() );
				} catch( ClassNotFoundException e ){
					// mLogger.catching(e);
					mLogger.trace("Cant find package-info - skipping ");
				}
			    pkg = Package.getPackage( pkgn );
			   
			}
			if( pkg != null ){
				mLogger.info("Found modules package: {} " , pkg.getName() );
			   	  config.setHelpURI(helpURI );
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
