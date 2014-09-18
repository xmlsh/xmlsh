/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.sh.shell;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.builtin.commands.exit;
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

public class Modules  implements
		Iterable<IModule> ,	 Cloneable {

	private static final Logger mLogger = LogManager.getLogger();
	
	private NameValueMap<  IModule >       mModules ;
	private NameValueMap< String >       mPrefixMap ;


	 Modules() {
		super();
	    mModules   =   new NameValueMap<>();
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

	/**
	 * @param shell
	 *            TODO
	 * @param init
	 * @param init
	 * @throws Exception
	 * @returns true if init was called
	 * 
	 */
	boolean importModule(Shell shell, String prefix , IModule mod,  List<XValue> init)
			throws Exception {

		mLogger.entry(shell, prefix, mod, init);
		assert (mod != null );
		if( mod == null ){
			throw new InvalidArgumentException("Module is null: ");
		}
		
		boolean bInit=false;
		if( ! mModules.containsValue( mod) ) {
		   mLogger.debug("Initializing a new module: {}", mod );
		   mod.onInit(shell, init);
		   bInit = true ;
		   mModules.put(mod.getName(),mod);
		}
		if( prefix != null )
		   mPrefixMap.put( prefix , mod.getName()); // should chnage to UUID or UURI
		return mLogger.exit(bInit);

	}


	


	public IModule getExistingModuleByIModule(IModule mod) {
		mLogger.entry(mod);
		return mModules.getByValue(mod);
	}


	public IModule getExistingModuleByName(String name) {

		mLogger.entry(name);
		return mLogger.exit( mModules.get(name));

	}


	@Override
	public Iterator<IModule> iterator() {
		
		return mModules.values().iterator();
	}

	public boolean moduleExistsByName(String name) {
		return mModules.containsKey(name);
	}

	public IModule getExistingModuleByPrefix(String prefix) {
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
	public boolean hasAnyPrefixes(IModule mh) {
		return mPrefixMap.containsValue(mh.getName());
	}



	public  Collection<String> getPrefixesForModule(IModule hm) {
		List<String> set = new ArrayList<>();
		for(  Entry<String, String> e : mPrefixMap.entrySet() ){
			if( e.getValue().equals(hm.getName()))
					set.add(e.getValue());
			
		}
		return set ;
		
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
