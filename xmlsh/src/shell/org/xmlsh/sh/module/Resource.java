package org.xmlsh.sh.module;

import java.net.URI;
import java.net.URL;

public abstract class Resource {
	private ResourceID  mID;
	private URL mLocation;

	public Resource(ResourceID id, URL location) {
		mID = id;
		mLocation = location;
	}

	public ResourceID getID() {
		return mID;
	}

	public URL getLocation() {
		return mLocation;
	}

	public String getName() {
		return mID.getName();
	}
	public URI getURI() {
		return mID.getURI();
	}
	
	public URL getURL() {
		return mLocation;
	}
	
	public int hashCode() {
		return mID.hashCode();
	}
	public boolean isOpaque() {
		return mLocation == null ;
	}
	public void setID(ResourceID iD) {
		mID = iD;
	}
	public void setLocation(URL location) {
		mLocation = location;
	}
	public String toString() {
		return mID.toString()  ;
	}
	
	

}
