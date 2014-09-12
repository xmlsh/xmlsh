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
import java.util.Set;

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

public class Modules extends ManagedObject<Modules> implements
		Iterable<IModule>, Closeable {

	static class ModuleHandle extends ReferenceCountedHandle<IModule> {

		public ModuleHandle(IModule mod) {
			super(mod);
		}

	}
	private static final Logger mLogger = LogManager.getLogger();
	
	// Prefix map - 1:1 of prefixed (non default) modules
	NamedHandleMap< IModule , ModuleHandle >   mPrefixedModules = new NamedHandleMap<>();

	// All Modules with or without prefixes 
	// TODO: Convert to QName/URL 
	NamedHandleMap< IModule , ModuleHandle >       mModules = new NamedHandleMap<>();
	
	

	
	private boolean bClosed = true;

	Modules() {
		bClosed = false;
	}

	public boolean declare(Shell shell, String prefix, String name, XValue at,
			List<XValue> init) throws Exception {
		
	   
		/*
		 * Dont redeclare a module under the same prefix
		 */

		if( moduleExists( name ) )
			return false ;

		
		IModule module = ModuleFactory.createModule(shell, prefix, name, at);

		return declare(shell, prefix, module , init);
	}

	public boolean moduleExists(String name) {
		return mModules.containsKey(name);
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
	 * 
	 */
	boolean declare(Shell shell,String prefix , IModule module,  List<XValue> init)
			throws Exception {

		assert (module != null);
		assert( ! moduleExists(module.getName()) );
		module.onInit(shell, init);

		assert (!mModules.containsValue(module));
		
		
		ModuleHandle hmod = new ModuleHandle(module);
		mModules.put(module.getName(),hmod);
		
		if( ! Util.isBlank(prefix)){
		  hmod.addRef(); 
		  hmod = mPrefixedModules.put(prefix, hmod);
		  if( hmod != null ) // Old module removed from prefix list - relese
			  hmod.release();
		}
		return true;

	}

	private ModuleHandle getModuleHandleByPrefix(String prefix) {
		return mPrefixedModules.get(prefix);

	}

	public IModule getModuleByPrefix(String prefix) {
		ModuleHandle hm = getModuleHandleByPrefix(prefix);
		return hm == null ? null : hm.get();

	}
	

	public ModuleHandle getExistingModule(IModule mod) {

		return mModules.get(mod.getName());

	}
	public ModuleHandle getExistingModuleByName(String name) {

		return mModules.get(name);

	}


	Modules(Modules that) {
		mLogger.entry(that);

		for (ModuleHandle hm : that.mModules.values() ) {
			hm.addRef();
			mModules.put( hm.get().getName(), hm );
		}

	}

	/*
	 * Import a module by string value
	 * 
	 * prefix=class class
	 */
	public boolean declare(Shell shell, String m, XValue at, List<XValue> init)
			throws Exception {
		mLogger.entry(shell,m,at);

		StringPair pair = new StringPair(m, '=');
		return declare(shell, pair.getLeft(), pair.getRight(), at, init);

	}

	@Override
	public synchronized void close() throws IOException {

		mLogger.entry(bClosed);
		if (bClosed)
			return;
		
		// Move this to NamedValueMap
		
			for (ModuleHandle m : mModules.values())
				m.release();
			
			mModules.clear();
			for( ModuleHandle m : mPrefixedModules.values())
				m.release();
			mPrefixedModules.clear();
	    mModules = null ;
		mPrefixedModules = null;
		bClosed = true ;

	}

	@Override
	public Iterator<IModule> iterator() {
		return mModules.valueIterator();
	}

	boolean declarePackageModule(Shell shell, String prefix, String name,
			List<String> pkgs, String helpXML, List<XValue> init)
			throws Exception {
		
		mLogger.entry(shell,prefix,name,pkgs);
		ModuleHandle mod = getExistingModuleByName(name);
		if( mod == null ){
			mod =  new ModuleHandle(ModuleFactory.createPackageModule(shell, prefix,
					name, pkgs, helpXML));
		    mModules.put(name ,  mod );
		}
		else
			mod.addRef();
		
		mod = mPrefixedModules.put(name, mod );
		if( mod != null)
			mod.release();
        
		return true ;
	}

	public Set<String> getPrefixes() {
		// TODO Auto-generated method stub
		return mPrefixedModules.keySet();
	}

	public Iterable<IModule> getDefaultModules() {
		mLogger.entry();
		
		Set<IModule> mods = new HashSet<>(Math.min(mPrefixedModules.size(), mModules.size()) );

		mods.addAll( mModules.valueList() );
		mods.remove( mPrefixedModules.valueList());
		return mods;
		
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
