/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.sh.shell;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.XValue;
import org.xmlsh.util.ManagedObject;
import org.xmlsh.util.StringPair;
import org.xmlsh.util.Util;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/*
 * Modules are like namespaces.
 * They map a prefix to a package (instead of a URI)
 * 
 * Modules also have a list of default prefixes, (e.g. "xmlsh")
 */


public class Modules extends ManagedObject implements Iterable<IModule > , Closeable
{
  private static final Logger mLogger = LogManager.getLogger();
  private List<IModule> mModules = new ArrayList<>();
  private Shell mShell ;
  private boolean bClosed = true ;
  
  Modules( Shell shell ){
    mShell = shell ;
    bClosed = false ;
  }
	public Iterator<IModule> iterator()
  {
    return mModules.iterator();
  }


  public IModule declare(Shell shell, String prefix , String name, List<XValue> init ) throws CoreException, IOException
	{
		/*
		 * Dont redeclare a module under the same prefix
		 */
    
    XValue at = null;
    if( init.size() > 1 && init.get(0).isAtomic()  && Util.isEqual("at", init.get(0).toString())){
      init.remove(0);
      at = init.remove(0);
      
    }
    

		for( IModule m : mModules )
			if( Util.isEqual(m.getName(),name) && Util.isEqual(m.getPrefix(),prefix))
				return m;


		IModule module = ModuleFactory.createModule(shell, prefix , name , at  );
		
		return declare(module,init);
	}


	/**
	 * Declare/Import a module
	 * If prefix is not null and already used then re-declare the module
	 * @param init 
	 * @param init 
	 * @throws CoreException 
	 * @throws IOException 
	 * 
	 */
	public IModule declare(IModule module, List<XValue> init) throws CoreException, IOException
	{
 
	  assert( module != null );
	  module.onInit(mShell, init);
	  
		if( ! Util.isEmpty(module.getPrefix())){
			// IF module exists by this prefix then redeclare
			IModule exists = getModuleByPrefix( module.getPrefix() );
			if( exists != null )
        detachModule(exists);
		}
		else {
			// Non prefixed modules dont import the same package
			IModule exists = getExistingModule( module );
			if( exists != null ) {
			  mLogger.trace("declare an existing non prefixed module - ignoreing new module");
			  
			  module.onDetach(mShell);
			  
			  
			  
				return exists ;
			
			}
		}

		// Dont duplicate exact object
		if( mModules.contains(module)){
      mLogger.trace("declare an existing identical module object- ignoreing new module");
      module.onDetach(mShell);
			return module;
		}

		attachModule(module);

		return module ;

	}


  private void attachModule(IModule module) throws IOException
  {

   assert( module != null  );
   module.onAttach(mShell);
    mModules.add(module);
  }


  private void detachModule(IModule exists) throws IOException
  {
    if( exists != null )
      exists.onDetach(mShell);
    mModules.remove( exists );
  }



  Modules() {}


	public IModule	getModuleByPrefix(String prefix)
	{
		for( IModule m : mModules )
			if( Util.isEqual(m.getPrefix(), prefix ) )
				return m ;
		return null;

	}

	public IModule	getExistingModule(IModule mod)
	{
	  
		for( IModule m : mModules )
			if( m.definesSameModule(mod ) )
				return m ;
		return null;

	}


	Modules( Shell shell , Modules that) throws IOException{
	  mShell = shell ;
    mLogger.trace("Cloning Modules - attach to all new ones");
    for( IModule m : that ){
      attachModule(  m );
    }

	}

	/*
	 * Import a module by string value
	 * 
	 * prefix=class
	 * class
	 * 
	 */
	public IModule declare(Shell shell, String m, List<XValue> init) throws CoreException, IOException {
		StringPair 	pair = new StringPair(m,'=');
		return declare(shell, pair.getLeft(), pair.getRight() ,  init  );

	}


  @Override
  public void close() throws IOException
  {
 
    if( bClosed )
      return ;
    synchronized( mModules ){
      for( IModule m : mModules ){
        m.onDetach(mShell);
      }
      mModules.clear();
    }
    mModules = null ;  
    mShell = null ;
    
    
  }

}



//
//
//Copyright (C) 2008-2014    David A. Lee.
//
//The contents of this file are subject to the "Simplified BSD License" (the "License");
//you may not use this file except in compliance with the License. You may obtain a copy of the
//License at http://www.opensource.org/licenses/bsd-license.php 
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied.
//See the License for the specific language governing rights and limitations under the License.
//
//The Original Code is: all this file.
//
//The Initial Developer of the Original Code is David A. Lee
//
//Portions created by (your name) are Copyright (C) (your legal entity). All Rights Reserved.
//
//Contributor(s): none.
//
