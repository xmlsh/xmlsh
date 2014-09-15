/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.sh.shell;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;

import org.xmlsh.core.CoreException;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Path;
import org.xmlsh.core.XValue;
import org.xmlsh.util.Util;

public class ExternalModule extends PackageModule
{
  /*
   * Constructor for external modules nameuri can either be a name found in
   * XMODPATH or a full URI/filename of the module.xml file (must end in
   * ".xml")
   */
  

  ExternalModule(Shell shell, String prefix, String nameuri, URI nameURI, XValue at) throws CoreException
  {
    super(shell,nameuri);
    try {

      URL configURL;
      File modDir = null;

      if(nameuri.endsWith(".xml") ) {
        assert( nameURI != null );
        configURL = nameURI.toURL();
        if(configURL.getProtocol().equals("file"))
          modDir = new File(configURL.getPath()).getParentFile();

      }
      else {

        Path path = shell.getPath(ShellConstants.XMODPATH, true);
        modDir = path.getFirstFileInPath(shell, nameuri,false);
        if(modDir == null)
          throw new InvalidArgumentException("Cannot find module directory for : " + nameuri);

        File config = new File(modDir, "module.xml");
        if(!config.exists())
          throw new InvalidArgumentException("Cannot find module.xml in directory : " + modDir.getAbsolutePath());
        configURL = config.toURI().toURL();
      }



    XdmNode configNode;
    configNode = Util.asXdmNode(configURL);

    List<URL> classpath = new ArrayList<URL>();

    XValue xv = XValue.newXValue(configNode);
    String pkg = xv.xpath(shell, "/module/@package/string()").toString();
    // TODO: better support for multi packages

    mPackages = Collections.singletonList(pkg);
    mName = xv.xpath(shell, "/module/@name/string()").toString();
    String require = xv.xpath(shell, "/module/@require/string()").toString();
    if(!Util.isBlank(require)) {
      int ret = shell.requireVersion(mName, require);
      if(ret != 0)
        throw new InvalidArgumentException("Module " + mName + " requires version " + require);
    }

    // iterate over values
    for (XValue v : xv.xpath(shell, "/module/classpath/file") ){
      if(v.isXdmNode()) {
        String file =v.asXdmNode().getAttributeValue(new QName("url"));
        URL classurl = new URL(configURL, file);
        classpath.add(classurl);

      }

    }

    if(modDir != null)
      for (XValue v :  xv.xpath(shell, "/module/classpath/directory")) {
        if(v.isXdmNode()) {
          String dir = v.asXdmNode().getAttributeValue(new QName("url"));
          for (String file : listFiles(modDir, dir)) {
            URL classurl = new URL(configURL, file);
            classpath.add(classurl);
          }

        }

      }

    mClassLoader = getClassLoader(classpath);
    // TODO: better support for multi packages
    mHelpURL = mClassLoader.getResource(toResourceName("commands.xml", mPackages.get(0)));

  } catch (CoreException e) {
    throw e;

  }

  catch (Exception e) {
    throw new CoreException(e);
  }

}

  private List<String> listFiles(File modDir, String dir) throws IOException
  {
    List<String> files = new ArrayList<String>();
    File file = new File(modDir, dir);
    for (String f : file.list())
      if(f.endsWith(".jar"))
        files.add(f);
    return files;

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