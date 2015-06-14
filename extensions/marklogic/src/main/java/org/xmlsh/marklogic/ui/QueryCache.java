/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.marklogic.ui;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.xmlsh.util.Util;

public class QueryCache {

	private Map<String,String> mQueries = new HashMap<String,String>();
	private static QueryCache _instance = null;
	private ExplorerShell mShell;
	static QueryCache getInstance() 
	{
		return _instance ;
		
	}
	static void init( ExplorerShell shell ){
		_instance = new QueryCache();
		_instance.mShell = shell;
	}
	String getQuery( String name ){
		
		String q = mQueries.get(name);
		if( q == null ){
			q = getResource( name );
			mQueries.put( name, q);
		}
		return q;
		
	}
	
    private String getResource(String res)  { 
		
		URL ures = mShell.getShell().getResource("/org/xmlsh/marklogic/ui/" + res );
		if( ures == null )
			return null ;
		InputStream is = null;
		
		

		try {
	       is = ures.openStream();
	       if( is == null )
				return null ;
		   return Util.readString(is,"UTF8" );
		} catch (IOException e) {
			
			return null;
		} finally {
			try {
				is.close();
			} catch (IOException e) {
			}
		}
		
		
	}

}



/*
 * Copyright (C) 2008-2014 David A. Lee.
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