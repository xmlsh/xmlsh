package org.xmlsh.sh.shell;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.core.CommandFactory;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.ScriptCommand.SourceMode;
import org.xmlsh.core.ScriptSource;
import org.xmlsh.core.XValue;
import org.xmlsh.util.Util;

public class ModuleFactory
{
  private  final static Logger mLogger = LogManager.getLogger();


  public static IModule createModule(Shell shell, String prefix, String nameuri, XValue at )  throws Exception
  {
    
    URI nameURI = null;
    // "java:xxx
    try {
      nameURI = shell.getURI(nameuri);
  
    } catch (Exception e) {

      mLogger.trace("excpetion parsing module as URI: " + nameuri, e);
      nameURI = null;
    }
    
    IModule mod = null ;
  
    if(nameURI != null && Util.isEqual(nameURI.getScheme(), "java"))
      mod = createJavaModule(shell, prefix, nameURI, at  );
    else 
    {
    // Try to find script source by usual means 
       ScriptSource script  = CommandFactory.getScriptSource(shell,nameuri ,SourceMode.IMPORT , at );
       if( script != null )
         mod = createScriptModule(shell ,prefix, script , nameuri );
    } 
    if( mod == null )
        mod = createExternalModule(shell, prefix, nameuri , nameURI,at);
    
    if( mod != null )
      mod.onLoad(shell);
    return mod ;
  }



  public static IModule createScriptModule(Shell shell, String prefix, ScriptSource script , String nameuri ) throws CoreException, IOException
  {
    return new ScriptModule(shell, prefix, script, nameuri );
  }



  public static IModule createJavaModule(Shell shell, String prefix, URI nameURI, XValue at) throws CoreException
  {
    return new JavaModule(shell, prefix, nameURI, at);
  }

  public static IModule createExternalModule(Shell shell, String prefix, String nameuri, URI nameURI, XValue at) throws CoreException
  {
    return new ExternalModule( shell, prefix,nameuri, nameURI , at );
  }

  
  
  public static IModule createPackageModule(Shell shell, String prefix, String name, List<String> pkgs, String helpURL)
  {
    return new PackageModule(shell, name, pkgs, helpURL);
  }

}
