/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.sh.shell;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.ReferenceCountedHandle;
import org.xmlsh.core.XValue;
import org.xmlsh.util.ManagedObject;
import org.xmlsh.util.StringPair;
import org.xmlsh.util.Util;

/*
 * Modules are like namespaces.
 * They map a prefix to a package (instead of a URI)
 * 
 * Modules also have a list of default prefixes, (e.g. "xmlsh")
 */


public class Modules extends ManagedObject<Modules> implements Iterable<IModule > , Closeable
{

 
  static class ModuleHandle extends ReferenceCountedHandle<IModule>
  {

    public ModuleHandle(IModule mod)
    {
      super(mod);
    }
  
  }
  
  private static final Logger mLogger = LogManager.getLogger();
  private HandleList<IModule, ModuleHandle>  mModules = new HandleList<>();
  private Shell mShell ;
  private boolean bClosed = true ;
  
  Modules( Shell shell ){
    mShell = shell ;
    bClosed = false ;
  }
  



  public boolean declare(Shell shell, String prefix , String name, XValue at , List<XValue> init ) throws Exception
	{
		/*
		 * Dont redeclare a module under the same prefix
		 */
    

		for( ModuleHandle hm : mModules )
			if( Util.isEqual(hm.get().getName(),name) && Util.isEqual(hm.get().getPrefix(),prefix))
				return   false;

		IModule module = ModuleFactory.createModule(shell, prefix , name , at  );
		
		return declare(module,init);
	}


	/**
	 * Declare/Import a module
	 * If prefix is not null and already used then re-declare the module
	 * @param init 
	 * @param init 
	 * @throws Exception 
	 * 
	 */
	boolean declare(IModule module, List<XValue> init) throws Exception
	{
 
	  assert( module != null );
	  module.onInit(mShell, init);
	  
		if( ! Util.isEmpty(module.getPrefix())){
			// IF module exists by this prefix then redeclare
			ModuleHandle exists = getModuleHandleByPrefix( module.getPrefix() );
			if( exists != null )
        exists.release();
		}
		else {
			// Non prefixed modules dont import the same package
			ModuleHandle exists = getExistingModule( module );
			if( exists != null ) {
	       mLogger.trace("declare an existing non prefixed module - closing new module");
			  module.close();
				return true;
			
			}
		}

		assert(! mModules.containsValue( module ));
		ModuleHandle hmod = new ModuleHandle(module);
		mModules.add( hmod  );
		return true;

	}




	private ModuleHandle	getModuleHandleByPrefix(String prefix)
	{
		for( ModuleHandle m : mModules)
			if( Util.isEqual(m.get().getPrefix(), prefix ) )
				return m ;
		return null;

	}

	 public IModule  getModuleByPrefix(String prefix){
	   ModuleHandle hm = getModuleHandleByPrefix(prefix);
	   return hm == null ? null : hm.get();
	   
	 }

	 
	public ModuleHandle	getExistingModule(IModule mod)
	{
	  
		for( ModuleHandle m : mModules  )
			if( m.get().definesSameModule(mod ) )
				return m ;
		return null;

	}


	Modules( Shell shell , Modules that) throws IOException{
	  mShell = shell ;
    mLogger.trace("Cloning Modules - attach to all new ones");
    for(  ModuleHandle hm : that.mModules ){
      hm.addRef();
      mModules.add(hm);
    }

	}

	/*
	 * Import a module by string value
	 * 
	 * prefix=class
	 * class
	 * 
	 */
	public boolean declare(Shell shell, String m, XValue at, List<XValue> init) throws Exception {
		StringPair 	pair = new StringPair(m,'=');
		return declare(shell, pair.getLeft(), pair.getRight() ,  at , init  );

	}


  @Override
  public void close() throws IOException
  {
 
    if( bClosed )
      return ;
    synchronized( mModules ){
      for( ModuleHandle m : mModules ){
        m.release();
      }
      mModules.clear();
    }
    mModules = null ;  
    mShell = null ;
    
    
  }


  @Override
  public Iterator<IModule> iterator()
  {
   return mModules.valueIterator();
  }




boolean  declarePackageModule( String prefix,  String name,
		List<String> pkgs, String helpXML, List<XValue> init ) throws Exception {

	return declare( ModuleFactory.createPackageModule(mShell, prefix, name, pkgs, helpXML) ,
			init );
			

	
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
