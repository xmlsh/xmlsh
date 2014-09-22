package org.xmlsh.sh.module;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.core.CommandFactory;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.ScriptCommand.SourceMode;
import org.xmlsh.core.ScriptSource;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.FileUtils;
import org.xmlsh.util.Util;

public class ModuleFactory
{
  private  final static Logger mLogger = LogManager.getLogger();


  public static Module createExternalModule(Shell shell, String nameuri, URI nameURI, List<URL> at) throws CoreException
  {
    return new ExternalModule( shell, nameuri,nameURI, at );
  }



  public static Module createJavaModule(Shell shell,URI nameURI, List<URL> at) throws CoreException
  {
    return new JavaModule(shell, nameURI, at);
  }



  public static Module createModule(Shell shell, String nameuri, List<URL> at )  throws Exception
  {
	 
	mLogger.entry(shell, nameuri, at);
	  
    URI nameURI = null;
	
    try {
      nameURI =  shell.getURI(nameuri);
  
    } catch (Exception e) {

      mLogger.trace("excpetion parsing module as URI: " + nameuri, e);
      nameURI = null;
    }
    
    Module mod = null ;
  
    if(nameURI != null && Util.isEqual(nameURI.getScheme(), "java"))
      mod = createJavaModule(shell, nameURI, at  );
    else 
    {
    // Try to find script source by usual means 
       ScriptSource script  = CommandFactory.getScriptSource(shell,nameuri ,SourceMode.IMPORT , at );
       if( script != null )
         mod = createScriptModule(shell ,script, nameuri );
    } 
    if( mod == null )
        mod = createExternalModule(shell, nameuri, nameURI , at);
    
    if( mod != null )
      mod.onLoad(shell);
    return mLogger.exit(mod) ;
  }

  public static Module createPackageModule(Shell shell, String name, List<String> pkgs, String helpURL)
  {
    return new PackageModule(shell, name, pkgs, helpURL);
  }

  
  
  public static Module createScriptModule(Shell shell, ScriptSource script, String nameuri ) throws CoreException, IOException
  {
    return new ScriptModule(shell, script, nameuri );
  }

}
