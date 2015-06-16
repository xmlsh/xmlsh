/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.sh.module;

import static org.xmlsh.util.UnifiedFileAttributes.MatchFlag.DIRECTORIES;
import static org.xmlsh.util.UnifiedFileAttributes.MatchFlag.READABLE;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;

import org.xmlsh.core.CoreException;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.ScriptCommand;
import org.xmlsh.core.ScriptSource;
import org.xmlsh.core.SearchPath;
import org.xmlsh.core.XClassLoader;
import org.xmlsh.core.XValue;
import org.xmlsh.core.ScriptCommand.SourceMode;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.sh.shell.ShellConstants;
import org.xmlsh.util.FileUtils;
import org.xmlsh.util.PathMatchOptions;
import org.xmlsh.util.StreamUtils;
import org.xmlsh.util.Util;

public class ExternalModule extends PackageModule
{
  /*
   * Constructor for external modules nameuri can either be a name found in
   * XMODPATH or a full URI/filename of the module.xml file (must end in
   * ".xml")
   */
	
  private static PathMatchOptions matchDirectory = new PathMatchOptions().withFlagsMatching(DIRECTORIES,READABLE);
private String mURI;
  

  protected ExternalModule(ModuleConfig config , XClassLoader loader ) throws CoreException
  {
    super(config , loader );
  }
  
  
  


public  static ModuleConfig getConfiguration(Shell shell, String nameuri,  List<URL> at) throws CoreException
  {
	
	mLogger.entry(shell, nameuri, at);
    try {

      URL configURL;
      File modDir = null;

      
      if(nameuri.endsWith(".xml") ) {    	
    	  
    	configURL  = new URL( nameuri);
        if(configURL.getProtocol().equals("file"))
          modDir = shell.getExplicitFile(configURL.getPath(),true).getParentFile();

      }
      else {

        SearchPath path = shell.getModulePath();
        modDir = path.getFirstFileInPath(shell, nameuri, matchDirectory );
        if(modDir == null)
          throw new InvalidArgumentException("Cannot find module directory for : " + nameuri);

        File config = new File(modDir, "module.xml");
        if(!config.exists())
          throw new InvalidArgumentException("Cannot find module.xml in directory : " + modDir.getAbsolutePath());
        configURL = config.toURI().toURL();
      }

      URL modRoot =  modDir.toURI().toURL();

    XdmNode configNode;
    configNode = Util.asXdmNode(configURL);



    XValue xv = XValue.newXValue(configNode);
    
    
    List<String> packages =  xv.xpath(shell, "/module/(@package|packages/package/(.|@url))/string()").asStringList().stream()
            .collect(Collectors.toList() );
    
    final File md = modDir;
   List<URL> modpath = xv.xpath(shell, "/module/modpath/directory/(.|@url)/string()").asStringList().stream()
           .filter( Util::notBlank)
           .map( s -> safeNewUrl(configURL, s) )
           .filter( Objects::nonNull )
           .collect(Collectors.toList() );

    String name  = xv.xpath(shell, "/module/@name/string()").toString();
    String require = xv.xpath(shell, "/module/@require/string()").toString();
    if(!Util.isBlank(require)) {
      if( ! shell.requireVersion(name, require) )
        throw new InvalidArgumentException("Module " + name + " requires version " + require);
    }
    List<URL> classpath =  xv.xpath(shell, "/module/classpath/(@file|file)/string()").asStringList().stream()
            .filter( Util::notBlank )
            .map( s -> safeNewUrl(configURL, s) )
            .filter( Objects::nonNull )
            .collect(Collectors.toList() );

    mLogger.debug("modDir: {} file classpaths: ", modDir , classpath );
    if(modDir != null){


       xv.xpath(shell, "/module/classpath/directory/(.|@url)/string()").asStringList().stream()
       .filter( Util::notBlank )
       .forEach(  s -> {
           mLogger.debug("directory: {}", s  );
             try {
                 File sFile = new File( md , s );
                 if( sFile.isDirectory() ){ 
                   listJarFiles(sFile)
                   .forEach( u -> classpath.add(u)) ;
                 }
               
             } catch (IOException e){
                 mLogger.catching( e );
             }
          }
       );
    }
    String modClassName = xv.xpath(shell, "/module/main/classname/string()").toString();
    String modScriptName = xv.xpath(shell, "/module/main/scriptname/string()").toString();
    ModuleConfig config = new ModuleConfig("external", name , modClassName,  modRoot ,  classpath, modpath , shell.getSerializeOpts() , packages, "commands.xml" );

    if( ! Util.isBlank(modScriptName))
        config.setModuleScriptName(modScriptName);
    
    
    return mLogger.exit(config);
  } catch (CoreException e) {
    throw e;

  }

  catch (Exception e) {
    throw new CoreException(e);
  }


}





private static URL safeNewUrl(URL baseUrl, String s) {
    mLogger.entry( baseUrl , s );
    try {
        return mLogger.exit(new URL( baseUrl , s ));
    } catch (MalformedURLException e) {
       mLogger.catching(e);
       return null;
    }
}


private static URL safeNewUrl( java.nio.file.Path p) {
    mLogger.entry( p  );
    try {
        return mLogger.exit( p.toUri().toURL() );
    } catch (MalformedURLException e) {
       mLogger.catching(e);
       return null;
    }
}


  public URL getHelpURL() {
	return  getClassLoader().getResource(toResourceName(getPackageConfig().getHelpURI(), getPackages().get(0)));
  }
  @Override
  public String describe()
  {
    return getName() + "[ at " + mURI + " ]";
  }

  private static List<URL> listJarFiles(File dir) throws  IOException 
  {
    mLogger.entry( dir );
    return Files.list( dir.toPath() )
         .filter( (p) -> p.toString().endsWith(".jar"))
         .map( (p) -> safeNewUrl(p) )
       .filter( Objects::nonNull)
       .collect(Collectors.toList() );

  }





@Override
public void onInit(Shell shell, List<XValue> args) throws Exception {
    super.onInit(shell, args);
    ScriptSource script = getConfig().getModuleScript();
    
    // Auto run main script if any 
    if( script != null ) { 
        mLogger.debug("Running onInit module script {} "  , script );
    
        try ( Shell sh = shell.clone() ) {
          if( args != null )
             sh.setArgs(args);
             Module hThis = this ;
             ScriptCommand cmd = new ScriptCommand(
                     // Holds a refernce to module within cmd 
                     getConfig().getModuleScript()  ,  SourceMode.IMPORT, shell.getLocation() , hThis  ) ;
                 if(  cmd.run(sh, getName(), args) != 0 )
                    shell.printErr("Failed to init script:" + getName() );
                  else {
                      // Extracts a clone of the this modules shell context
                      mStaticContext = sh.getExportedContext();
                  }
        } 
    }
    
}





@Override
public void onLoad(Shell shell) {
    super.onLoad(shell);
    mLogger.entry(shell);
    
    
    String scriptName = getConfig().getModuleScriptName();
    if( Util.notBlank(scriptName ) ){

        mLogger.trace("Locating init script {}", scriptName);
        try {
            // Try to find script source by usual means 
            URL moduleRoot = getConfig().getModuleRoot();
            ScriptSource script  = CommandFactory.getScriptSource(shell, new PName(scriptName) ,
                    SourceMode.IMPORT , Collections.singletonList( moduleRoot) );
            
            if( script != null ){
                getConfig().setModuleScript(script);
                mLogger.debug("Loaded module script from {}" , moduleRoot);
            }
        } catch( IOException | CoreException | URISyntaxException e ){
            mLogger.catching(e);
        }
    }
    
}




}



/*
 * Copyright (C) 2008-2012 David A. Lee.
 * 
 * The contents of this file are subject to the "Simplified BSD License" (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.opensource.org/licenses/bsd-license.php 

 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied.
 * See the License for the specific language governing rights and limitations under the License.
 *
 * The Original Code is: all this file.
 *
 * The Initial Developer of the Original Code is David A. Lee
 *
 * Portions created by (your name) are Copyright (C) (your legal entity). All Rights Reserved.
 *
 * Contributor(s): David A. Lee
 * 
 */
