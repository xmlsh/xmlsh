package org.xmlsh.sh.module;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

public class ResourceID {

	public static URI genURI(String name, String type) throws URISyntaxException {
		return new URI("urn", type + ":" + name , null  );
	}
	private ResourceName   mName ;
	
	/*
	 * name/typ[e only 
	 */


	private URI      mURI ;
	public ResourceID(ResourceName name) {
		mName = name ;
	}
	
	public ResourceID(ResourceName name, URI uri) {
		mName = name ;
		mURI = uri ;
	}


	public ResourceID(String name) {
		mName = new ResourceName(name);
	}
	public ResourceID(String name, URI uri) {
	   this( new ResourceName(name),uri );
	}



	@Override
	public boolean equals(Object obj) {

		if( obj == this )
			return true ;
		if( obj instanceof ResourceID ){
			ResourceID that = (ResourceID) obj;
			return this.mName.equals(that.mName) &&
				   this.mURI.equals(that.mURI);
		}
		return false ;
	}

	public ResourceName getName() {
		return mName;
	}

	public URI getURI() {
		return mURI;
	}

	public void setName(ResourceName name) {
		mName = name;
	}
	
	public void setURI(URI uRI) {
		mURI = uRI;
	}
	

}
