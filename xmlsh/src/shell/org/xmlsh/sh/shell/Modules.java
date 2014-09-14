/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.sh.shell;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.XValue;
import org.xmlsh.util.IManagable;
import org.xmlsh.util.ManagedObject;
import org.xmlsh.util.NameValueMap;

/*
 * Modules are like namespaces.
 * They map a prefix to a package (instead of a URI)
 * 
 * Modules also have a list of default prefixes, (e.g. "xmlsh")
 */

public class Modules extends ManagedObject<Modules> implements
		Iterable<ModuleHandle> , IManagable , Cloneable {

	private static final Logger mLogger = LogManager.getLogger();
	
	private NamedHandleMap< IModule ,  ModuleHandle >       mModules ;
	private NameValueMap< String >       mPrefixMap ;


	 Modules() {
		super();
	    mModules = new NamedHandleMap<>();
	    mPrefixMap = new NameValueMap<>();
	}

	private Modules(Modules that) {
		
		this();
		mLogger.entry(that);
		mModules = that.mModules.clone();
		mPrefixMap = that.mPrefixMap.clone();
		mLogger.exit();
	}
	
	@Override
	public Modules clone(){
		mLogger.entry();
		return mLogger.exit(new Modules(this));
	}

	@Override
	public synchronized void doClose() throws IOException {

		assert( ! isClosed() );
		
		mPrefixMap.clear();
		mPrefixMap = null ;
		// Move this to NamedValueMap
		mModules.release();
	    mModules = null ;

	}

	/**
	 * @param shell
	 *            TODO
	 * @param init
	 * @param init
	 * @throws Exception
	 * @returns true if init was called
	 * 
	 */
	boolean importModule(Shell shell, String prefix , ModuleHandle module,  List<XValue> init)
			throws Exception {

		mLogger.entry(shell, prefix, module, init);
		assert (module != null && ! module.isNull() );
		if( module == null || module.isNull() ){
			throw new InvalidArgumentException("Module is null: ");
		}
		
		boolean bInit=false;
		if( ! mModules.containsValue( module.get()) ) {
		   mLogger.debug("Initializing a new module: {}", module );
		   module.get().onInit(shell, init);
		   bInit = true ;
		   module.addRef();
		   mModules.put(module.getName(),module);
		}
		if( prefix != null )
		   mPrefixMap.put( prefix , module.getName()); // should chnage to UUID or UURI
		return mLogger.exit(bInit);

	}


	


	public ModuleHandle getExistingModuleByIModule(IModule mod) {
		mLogger.entry(mod);
		return mModules.getByValue(mod);
	}


	public ModuleHandle getExistingModuleByName(String name) {

		mLogger.entry(name);
		return mLogger.exit( mModules.get(name));

	}


	@Override
	public Iterator<ModuleHandle> iterator() {
		
		return mLogger.exit(mModules.handleIterator());
	}

	public boolean moduleExistsByName(String name) {
		return mModules.containsKey(name);
	}

	public ModuleHandle getExistingModuleByPrefix(String prefix) {
		if( prefix == null )
			return null ;
		mLogger.entry(prefix);
		String name = mPrefixMap.get(prefix);
		if( name != null )
			return getExistingModuleByName( name );
		else
			return null ;
	}

	// true if the module has any prefix mappings
	public boolean hasAnyPrefixes(ModuleHandle mh) {
		return mPrefixMap.containsValue(mh.getName());
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
