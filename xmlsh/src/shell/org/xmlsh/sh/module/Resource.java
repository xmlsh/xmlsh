package org.xmlsh.sh.module;

import java.net.URI;
import java.net.URL;

public abstract class Resource {
	private ResourceID  mID;
	private ResourceLocation mLocation;

	public Resource(ResourceID id, ResourceLocation location) {
		mID = id;
		mLocation = location;
	}

	public ResourceID getID() {
		return mID;
	}

	public ResourceLocation getLocation() {
		return mLocation;
	}

	public ResourceName getName() {
		return mID.getName();
	}
	public URI getURI() {
		return mID.getURI();
	}
	
	public URL getURL() {
		return mLocation.getURL();
	}
	
	public int hashCode() {
		return mID.hashCode();
	}
	public boolean isOpaque() {
		return mLocation.isOpaque();
	}
	public void setID(ResourceID iD) {
		mID = iD;
	}
	public void setLocation(ResourceLocation location) {
		mLocation = location;
	}
	public String toString() {
		return mID.toString()  ;
	}
	
	

}
