package org.xmlsh.sh.module;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

public class ResourceID {

	private String   mName ;
	private URI      mURI ;
	public ResourceID(String name) {
		mName = name ;
	}
	
	public ResourceID(String name, URI uri) {
		mName = name ;
		mURI = uri ;
	}


	public static URI genURI(String name, String type) throws URISyntaxException {
		return new URI("urn", type + ":" + name , null  );
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

	public String getName() {
		return mName;
	}

	public URI getURI() {
		return mURI;
	}

	public void setName(String name) {
		mName = name;
	}
	
	public void setURI(URI uRI) {
		mURI = uRI;
	}
	

}
