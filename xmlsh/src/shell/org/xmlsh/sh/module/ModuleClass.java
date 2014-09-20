package org.xmlsh.sh.module;

import java.net.URI;

/*
 * 
 * Defines the resolved class/location and metadata for a module
 * 
 * 
 */
public class ModuleClass {

	protected URI mID;
	protected String mName;
	protected URI mLocation;
	
	
	
	public ModuleClass() {
	}
	
	// Modules ientified by a base URI - may be of any scheme
	// Anon modules may be uuid schems
	public URI getModuleId() {
		return mID;
	}

	// Frienly name for module - but may not be uniqe across all modules
	public String getName() {
		return mName;
	}
	
	// Module bound to a location
	public URI getModuleLocation() {
		return mLocation;
	} 
	

}
