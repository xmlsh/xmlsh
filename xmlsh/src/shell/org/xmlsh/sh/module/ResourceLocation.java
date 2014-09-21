package org.xmlsh.sh.module;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class ResourceLocation {
	public static ResourceLocation opaqueLocation() {
		return new ResourceLocation();
	}
	
	private URL      mURL ;
	// Unique/Opaque location
	public ResourceLocation() {
		mURL = null ;
	}
	// Resolveable location
	public ResourceLocation(URL url) {
		mURL = url;
	}
	public boolean equals(Object obj) {
		if( this == obj )
			return true ;
		if( mURL == null )
			return false ;
		if( obj instanceof ResourceLocation )
		   return mURL.equals(((ResourceLocation)obj).mURL);
		return false ;
	}
	public URL getURL() {
		return mURL;
	}
	public int hashCode() {
		return  mURL == null ? 0 : mURL.hashCode();
	}
	public boolean isOpaque() {
		return mURL == null ;
	}
	
	public void setURL(URL url) {
		mURL = url;
	}
	
	public String toString() {
		return mURL == null ? "<opaque>" :  mURL.toString();
	}
	public URI toURI() throws URISyntaxException {
		return (mURL == null) ? ((URI)null) : mURL.toURI();
	}
	
	
	
}
