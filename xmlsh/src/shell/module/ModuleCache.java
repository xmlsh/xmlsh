package module;

import java.net.URI;

import org.xmlsh.util.NameValueMap;


/*
 * Global module type cache
 * Serves as factory class and resolver
 * 
 */
public class ModuleCache {

	// id -> ModuleClass 
	private NameValueMap<ModuleClass>  mModuleCache;
	
	 
	public ModuleCache() {
	  mModuleCache = new 	NameValueMap<>();
	}
	
	synchronized ModuleClass  findById( URI id ){
		
		return mModuleCache.get(id);
		
	}
	
	

}
