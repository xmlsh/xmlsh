package org.xmlsh.sh.module;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.core.CoreException;
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
  static Module createModuleModule(Shell shell , StringPair pair , List<URL> at ) throws CoreException, IOException, URISyntaxException
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
				Module mod = m.getModule(shell,pair.getRight(), at );
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

  public static Module createModule(Shell shell, String qname, List<URL> at )  throws Exception
  {
	 
	mLogger.entry(shell, qname, at);
	  

	
	// If hame has ":" it might be a schemed or prefixed module 
	StringPair pair = new StringPair(qname, ':');
	String name = pair.getRight();
	String prefix =  pair.getLeft();
    
    Module mod = null ;
  
    // special scheme
    if(prefix != null && Util.isEqual(prefix, "java"))
      mod = createJavaModule(shell, name, at  );
    
    if( mod == null && prefix  == null ){
    	mod = createInternalModule( shell, name );
    	
    }
    if( mod == null  ){
    	mod = createModuleModule(shell, pair, at );
    	
    }
    if( mod == null )
    {
    // Try to find script source by usual means 
       ScriptSource script  = CommandFactory.getScriptSource(shell,qname ,SourceMode.IMPORT , at );
       if( script != null )
         mod = createScriptModule(shell ,script, qname );
    } 
    if( mod == null )
        mod = createExternalModule(shell, qname, at);
    
    if( mod != null )
      mod.onLoad(shell);
    return mLogger.exit(mod) ;
  }



  /*
   * Look for a Class module in the internal packages 
   * 
   */
static Module createInternalModule(Shell shell, String nameuri ) throws CoreException {
	
	  ClassLoader classLoader = RootModule.getInstance().getClassLoader();

	  Module mod = null ;
	mLogger.debug("trying to find internal module by name: {} " , nameuri );
	String pkgn =  findInternalModulePackage( nameuri , classLoader  );
	if( pkgn != null ){
		mod = createPackageModule(  shell , nameuri , Collections.singletonList(pkgn), null );
		if( mod != null )
		mLogger.debug("created internal module: ");
	}
	
	/*
	 * Look for internal scripts - Defer to  modules to find them
	 */
	return mod;
}

  public static Module createPackageModule( Shell shell, String name, List<String> pkgs, String helpURL)
  {
    return new PackageModule( new ModuleConfig( name, null , shell.getSerializeOpts() , pkgs, helpURL) );
  }

  
  
  public static Module createScriptModule(Shell shell, ScriptSource script, String nameuri ) throws CoreException, IOException
  {
    return new ScriptModule(shell, script, nameuri );
  }
  
  
  /*
   * TEMPORARY HAck - try a name to see if it is an internal module in org.xmlsh.modules by looking
   * for a sub package 
   */

	private static String  findInternalModulePackage( String name ,  ClassLoader classLoader )
	{
		
		mLogger.entry( name);
		for( String p : Util.toArray("org.xmlsh.modules")){
			/* Look for module under modules */
			String pkgn = p +  "." + name ;
			
			Package pkg = Package.getPackage( pkgn );
			if( pkg == null ){
				try {
				Class<?> cls = JavaUtils.findClass(pkgn + ".package-info" , classLoader );
				  mLogger.info("found clas: {} " + cls.getName() );
				  
				} catch( ClassNotFoundException e ){
					// mLogger.catching(e);
					mLogger.trace("Cant find package-info - skipping ");
				}
			    pkg = Package.getPackage( pkgn );
			}
			if( pkg != null ){
				mLogger.info("Found modules package: {} " , pkg.getName() );
				return mLogger.exit(pkg.getName());
			}
		}

	     return mLogger.exit(null);
		
	}
	
	

}
