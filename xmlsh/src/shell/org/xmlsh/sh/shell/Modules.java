/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.sh.shell;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.builtin.commands.exit;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.IHandle;
import org.xmlsh.core.InvalidArgumentException;
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

public class Modules extends ManagedObject<Modules> implements
		Iterable<ModuleHandle>, Closeable , Cloneable {

	private static final Logger mLogger = LogManager.getLogger();
	
	private NamedHandleMap< IModule ,  ModuleHandle >       mModules ;
	
	private boolean bClosed = true;

	 Modules() {
		bClosed = false;
	    mModules = new NamedHandleMap<>();
	}

	private Modules(Modules that) {
		
		mLogger.entry(that);
		mModules = that.mModules.clone();
		mLogger.exit();
	}
	
	public Modules clone(){
		mLogger.entry();
		return mLogger.exit(new Modules(this));
	}

	@Override
	public synchronized void close() throws IOException {

		mLogger.entry(bClosed);
		if (bClosed)
			return;
		
		
		// Move this to NamedValueMap
		mModules.release();
	    mModules = null ;
		bClosed = true ;

	}

	/**
	 * Declare/Import a module If prefix is not null and already used then
	 * re-declare the module
	 * 
	 * @param shell
	 *            TODO
	 * @param init
	 * @param init
	 * @throws Exception
	 * @returns true if this is a new module 
	 * 
	 */
	boolean declare(Shell shell, String name , ModuleHandle module,  List<XValue> init)
			throws Exception {

		mLogger.entry(shell, name, module, init);
		assert (module != null && ! module.isNull() );
		if( module == null || module.isNull() ){
			throw new InvalidArgumentException("Module is null: ");
		}
		if( ! mModules.containsValue( module.get()) ) {
		   mLogger.debug("Initializing a new module: {}", module );
		   module.get().onInit(shell, init);
		   module.addRef();
		   return mLogger.exit( mModules.put(Shell.toModuleUri(module),module) );

		}

		
		return mLogger.exit(false);
		

	}

	public ModuleHandle importModule(Shell shell, String prefix, String name, XValue at,
			List<XValue> init) throws Exception {
		
		mLogger.entry(shell, prefix, name, at, init);
	   
		/*
		 * Dont recreate the same module - but go ahead and import it under different prefixes
		 */

		ModuleHandle exists = getExistingModuleByName(name);
		if( exists == null )
			exists = new ModuleHandle(  ModuleFactory.createModule(shell, prefix, name, at) );
		else 
			exists.addRef();
		
		boolean bReplaced = declare(shell, prefix, exists , init);
		return exists ;
	}

	
	

	ModuleHandle declarePackageModule(Shell shell, String prefix, String name,
			List<String> pkgs, String helpXML, List<XValue> init)
			throws Exception {
		
		mLogger.entry(shell,prefix,name,pkgs);
		ModuleHandle mod = getExistingModuleByName(name);
		if( mod == null ){
			mod =  new ModuleHandle(ModuleFactory.createPackageModule(shell, prefix,
					name, pkgs, helpXML));
		    mModules.put( Shell.toModuleUri(mod),  mod );
		}
		else
			mod.addRef();
		
		return mLogger.exit(mod);
		
	}


	public ModuleHandle getExistingModule(IModule mod) {

		
		mLogger.entry(mod);
	
		mLogger.error("Shoulndt be called yet");
		return mModules.getByValue(mod);
	}

	public ModuleHandle getExistingModuleByURI(String uri) {
		
          mLogger.entry(uri);
		
		return mLogger.exit( mModules.get(uri));         
	}

	public ModuleHandle getExistingModuleByName(String name) {

		
		mLogger.entry(name);
		
		return mLogger.exit( mModules.get(Shell.toModuleUri(name)));

	}


	@Override
	public Iterator<ModuleHandle> iterator() {
		
		return mLogger.exit(mModules.handleIterator());
	}

	public boolean moduleExists(String name) {
		return mModules.containsKey(name);
	}

}

//
//
// Copyright (C) 2008-2014 David A. Lee.
//
// The contents of this file are subject to the "Simplified BSD License" (the
// "License");
// you may not use this file except in compliance with the License. You may
// obtain a copy of the
// License at http://www.opensource.org/licenses/bsd-license.php
//
// Software distributed under the License is distributed on an "AS IS" basis,
// WITHOUT WARRANTY OF ANY KIND, either express or implied.
// See the License for the specific language governing rights and limitations
// under the License.
//
// The Original Code is: all this file.
//
// The Initial Developer of the Original Code is David A. Lee
//
// Portions created by (your name) are Copyright (C) (your legal entity). All
// Rights Reserved.
//
// Contributor(s): none.
//
