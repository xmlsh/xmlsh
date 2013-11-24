/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.marklogic.ui;

import java.net.URI;
import java.net.URISyntaxException;

import com.marklogic.xcc.ContentSource;
import com.marklogic.xcc.ContentSourceFactory;
import com.marklogic.xcc.SecurityOptions;

class ExplorerOptions {
	String mScheme ;
	String mHost ;
	String mUser ;
	int    mPort ;
	String mPassword ; 
	String mDatabase ;
	
	
	int    mBatchSize = 10;  // PUT batch size max
	int    mMaxRows   = 1000;
	SecurityOptions mOptions;
	
	// Query options 
	String mQuery = "" ;
	
	ExplorerOptions( String connectString ) throws URISyntaxException{
		if( connectString != null ){
			URI uri = new URI( connectString );
			
		    String scheme = uri.getScheme();
	
		    mHost = uri.getHost();
		    mPort = uri.getPort();
		    
		    String userInfoStr = uri.getUserInfo();
		    String[] userInfo = (userInfoStr == null) ? (new String[0]) : userInfoStr.split(":", 2);
		    String contentBase = uri.getPath();
		
		
		
		    if (contentBase != null) {
		        if (contentBase.startsWith("/")) {
		            contentBase = contentBase.substring(1);
		        }
		
		        if (contentBase.length() == 0) {
		        	// in the case where a numeric is sent
		            contentBase = uri.getFragment(); 
		            if (contentBase != null) {
		            	contentBase = "#" + contentBase;
		            }
		        }
		    }
		    mDatabase = contentBase ;
		
		    if( userInfo.length == 2 ) {
		    	mUser = userInfo[0];
		    	mPassword = userInfo[1];
		    }
		
		}
		
	}
	
	
    public ContentSource newContentSource(){
    	return ContentSourceFactory.newContentSource( mHost , mPort , mUser , mPassword , mDatabase , mOptions );
    	
    }


}



/*
 * Copyright (C) 2008-2012 David A. Lee.
 * 
 * The contents of this file are subject to the "Simplified BSD License" (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.opensource.org/licenses/bsd-license.php 

 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied.
 * See the License for the specific language governing rights and limitations under the License.
 *
 * The Original Code is: all this file.
 *
 * The Initial Developer of the Original Code is David A. Lee
 *
 * Portions created by (your name) are Copyright (C) (your legal entity). All Rights Reserved.
 *
 * Contributor(s): David A. Lee
 * 
 */